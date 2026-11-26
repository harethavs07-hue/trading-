package com.example.engine

import com.example.model.Candle
import com.example.model.ChartFingerprint
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AntiRepetitionEngine(
    private val maxHistorySize: Int = 10_000
) {
    private val history = ArrayList<ChartFingerprint>(256)
    private val hashSet = HashSet<String>(256)

    @Synchronized
    fun loadInitialHistory(items: List<ChartFingerprint>) {
        history.clear()
        hashSet.clear()
        val trimmed = if (items.size > maxHistorySize) items.takeLast(maxHistorySize) else items
        history.addAll(trimmed)
        trimmed.forEach { hashSet.add(it.structureHash) }
    }

    @Synchronized
    fun getHistorySnapshot(): List<ChartFingerprint> = history.toList()

    @Synchronized
    fun size(): Int = history.size

    @Synchronized
    fun clearHistory() {
        history.clear()
        hashSet.clear()
    }

    fun buildFingerprint(
        id: String,
        asset: String,
        timeframe: String,
        archetypeName: String,
        trickySubtype: String,
        expectedDirection: String,
        candles: List<Candle>,
        wickRatio: Float,
        volatility: Float,
        trendBias: Float,
        supportZone: Float,
        resistanceZone: Float
    ): ChartFingerprint {
        val minLow = candles.minOfOrNull { it.low } ?: 0f
        val maxHigh = candles.maxOfOrNull { it.high } ?: 1f
        val span = max(maxHigh - minLow, 1e-5f)

        // Normalized close + body+wick signature across all 30 candles
        val vector = ArrayList<Float>(candles.size * 2)
        candles.forEach { c ->
            vector.add(((c.close - minLow) / span).coerceIn(0f, 1f))
            vector.add(((c.high - c.low) / span).coerceIn(0f, 1f))
        }

        val supportNorm = ((supportZone - minLow) / span).coerceIn(0f, 1f)
        val resistanceNorm = ((resistanceZone - minLow) / span).coerceIn(0f, 1f)

        val quantizedSignature = buildString {
            append(asset).append('|')
            append(timeframe).append('|')
            append(archetypeName).append('|')
            append(trickySubtype).append('|')
            append(expectedDirection).append('|')
            vector.forEach { v -> append((v * 20).toInt()).append(',') }
        }
        val hash = sha256Short(quantizedSignature)

        return ChartFingerprint(
            id = id,
            asset = asset,
            timeframe = timeframe,
            archetypeName = archetypeName,
            trickySubtype = trickySubtype,
            expectedDirection = expectedDirection,
            normalizedVector = vector,
            wickRatio = wickRatio,
            volatility = volatility,
            trendBias = trendBias,
            supportNormalized = supportNorm,
            resistanceNormalized = resistanceNorm,
            structureHash = hash,
            createdAtEpochMs = System.currentTimeMillis()
        )
    }

    @Synchronized
    fun isAcceptable(
        candidate: ChartFingerprint,
        similarityThreshold: Float
    ): Boolean {
        if (hashSet.contains(candidate.structureHash)) return false

        // Prevent immediate asset + timeframe back-to-back repeats in recent 4 rounds
        val recent = history.takeLast(4)
        if (recent.any { it.asset == candidate.asset && it.timeframe == candidate.timeframe }) {
            return false
        }
        // Prevent exact same archetype + outcome in back-to-back round
        val lastOne = history.lastOrNull()
        if (lastOne != null &&
            lastOne.archetypeName == candidate.archetypeName &&
            lastOne.expectedDirection == candidate.expectedDirection &&
            lastOne.trickySubtype == candidate.trickySubtype
        ) {
            return false
        }

        // Compare against recent 150 fingerprints for fast O(N) structural similarity check,
        // plus stratified sampling if history is huge
        val comparePool = if (history.size <= 200) {
            history
        } else {
            val step = max(1, (history.size - 120) / 80)
            val sampled = ArrayList<ChartFingerprint>(200)
            var idx = 0
            while (idx < history.size - 120) {
                sampled.add(history[idx])
                idx += step
            }
            sampled.addAll(history.takeLast(120))
            sampled
        }

        for (existing in comparePool) {
            val similarity = computeSimilarity(candidate, existing)
            if (similarity >= similarityThreshold) {
                return false
            }
        }
        return true
    }

    fun computeSimilarity(a: ChartFingerprint, b: ChartFingerprint): Float {
        val len = min(a.normalizedVector.size, b.normalizedVector.size)
        if (len == 0) return 0f

        var diffSum = 0f
        for (i in 0 until len) {
            diffSum += abs(a.normalizedVector[i] - b.normalizedVector[i])
        }
        val ohlcSimilarity = (1f - (diffSum / len.toFloat())).coerceIn(0f, 1f)

        val structuralDelta =
            abs(a.wickRatio - b.wickRatio) * 0.45f +
                abs(a.volatility - b.volatility) * 4.5f +
                abs(a.trendBias - b.trendBias) * 0.8f +
                abs(a.supportNormalized - b.supportNormalized) * 0.6f +
                abs(a.resistanceNormalized - b.resistanceNormalized) * 0.6f
        val structuralSimilarity = (1f - (structuralDelta / 2.2f)).coerceIn(0f, 1f)

        var contextBonus = 0f
        if (a.archetypeName == b.archetypeName) contextBonus += 0.08f
        if (a.asset == b.asset && a.timeframe == b.timeframe) contextBonus += 0.06f
        if (a.expectedDirection == b.expectedDirection) contextBonus += 0.03f

        return (ohlcSimilarity * 0.68f + structuralSimilarity * 0.25f + contextBonus)
            .coerceIn(0f, 1f)
    }

    @Synchronized
    fun recordAccepted(fingerprint: ChartFingerprint) {
        if (history.size >= maxHistorySize) {
            val removeCount = history.size - maxHistorySize + 1
            repeat(removeCount) {
                if (history.isNotEmpty()) {
                    val removed = history.removeAt(0)
                    hashSet.remove(removed.structureHash)
                }
            }
        }
        history.add(fingerprint)
        hashSet.add(fingerprint.structureHash)
    }

    private fun sha256Short(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.take(12).joinToString("") { "%02x".format(it) }
    }
}
