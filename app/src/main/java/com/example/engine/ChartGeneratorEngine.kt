package com.example.engine

import com.example.model.Candle
import com.example.model.ChartChallenge
import com.example.model.GameMode
import com.example.model.MarketCategory
import com.example.model.PriceActionArchetype
import com.example.model.TradeDirection
import com.example.model.TrickyScenarioType
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ChartGeneratorEngine(
    private val antiRepetitionEngine: AntiRepetitionEngine
) {
    private val timeframes = listOf("1m", "5m", "15m", "1H", "4H")

    private val basePrices = mapOf(
        "BTC/USD" to 92_400f,
        "ETH/USD" to 3_240f,
        "SOL/USD" to 188f,
        "NIFTY 50" to 24_350f,
        "BANKNIFTY" to 52_100f,
        "RELIANCE" to 2_940f,
        "XAU/USD" to 2_655f,
        "EUR/USD" to 1.0840f,
        "NVDA" to 139.5f,
        "TSLA" to 338.0f,
        "AAPL" to 229.0f
    )

    fun generateUniqueChallenge(
        category: MarketCategory,
        gameMode: GameMode,
        similarityThreshold: Float = 0.85f
    ): ChartChallenge {
        val maxRetries = 28
        var bestCandidate: ChartChallenge? = null

        for (attempt in 0 until maxRetries) {
            val candidate = generateCandidate(
                category = category,
                gameMode = gameMode,
                attemptSeedOffset = attempt
            )
            bestCandidate = candidate
            if (antiRepetitionEngine.isAcceptable(candidate.fingerprint, similarityThreshold)) {
                antiRepetitionEngine.recordAccepted(candidate.fingerprint)
                return candidate
            }
        }

        // Fallback diversity strategy: force distinct asset/timeframe/archetype + higher noise perturbation
        val fallback = generateCandidate(
            category = category,
            gameMode = gameMode,
            attemptSeedOffset = 999,
            forceDiversityNoise = true
        )
        antiRepetitionEngine.recordAccepted(fallback.fingerprint)
        return fallback
    }

    private fun generateCandidate(
        category: MarketCategory,
        gameMode: GameMode,
        attemptSeedOffset: Int,
        forceDiversityNoise: Boolean = false
    ): ChartChallenge {
        val rng = Random(System.nanoTime() xor (attemptSeedOffset.toLong() * 7919L))

        val assetPool = category.assets
        val asset = assetPool[rng.nextInt(assetPool.size)]
        val resolvedCategory = resolveAssetCategory(asset)
        val timeframe = timeframes[rng.nextInt(timeframes.size)]

        val baseRef = basePrices[asset] ?: 1000f
        // Jitter starting anchor ±8% so same asset never starts at identical price
        val startPrice = baseRef * (0.92f + rng.nextFloat() * 0.16f)

        val volatility = (0.0045f + rng.nextFloat() * 0.011f) * if (forceDiversityNoise) 1.35f else 1.0f
        val wickRatio = 0.35f + rng.nextFloat() * 0.95f
        val reversalStrength = 0.85f + rng.nextFloat() * 0.65f
        val momentum = 0.80f + rng.nextFloat() * 0.70f
        val gapChance = if (resolvedCategory == MarketCategory.INDIAN_STOCKS || resolvedCategory == MarketCategory.US_STOCKS) 0.18f else 0.06f

        val isTricky = gameMode == GameMode.TRICKY
        val trickySubtype: TrickyScenarioType? = if (isTricky) {
            TrickyScenarioType.entries[rng.nextInt(TrickyScenarioType.entries.size)]
        } else null

        val archetype = pickArchetype(rng, isTricky, trickySubtype)
        val expectedDirection = resolveDirection(archetype, trickySubtype, rng)
        val trendBias = if (expectedDirection == TradeDirection.BULL) {
            0.10f + rng.nextFloat() * 0.30f
        } else {
            -(0.10f + rng.nextFloat() * 0.30f)
        }

        // Build 22 historical candles + 8 outcome candles (30 total)
        val closes = buildPricePath(
            rng = rng,
            startPrice = startPrice,
            volatility = volatility,
            archetype = archetype,
            trickySubtype = trickySubtype,
            expectedDirection = expectedDirection,
            reversalStrength = reversalStrength,
            momentum = momentum
        )

        val candles = ArrayList<Candle>(30)
        var prevClose = startPrice
        for (i in 0 until 30) {
            val targetClose = closes[i]
            val gap = if (i in 1..21 && rng.nextFloat() < gapChance) {
                (rng.nextFloat() - 0.5f) * volatility * startPrice * 0.75f
            } else 0f

            val open = prevClose + gap
            val bodyHigh = max(open, targetClose)
            val bodyLow = min(open, targetClose)
            val bodySize = max(abs(targetClose - open), startPrice * volatility * 0.18f)

            val isTrickyTrapCandle = isTricky && (i in 19..24)
            val wickMultiplier = if (isTrickyTrapCandle) wickRatio * 1.6f else wickRatio

            val upperWick = bodySize * (0.2f + rng.nextFloat() * wickMultiplier)
            val lowerWick = bodySize * (0.2f + rng.nextFloat() * wickMultiplier)

            val high = bodyHigh + upperWick
            val low = max(0.0001f, bodyLow - lowerWick)

            candles.add(Candle(open = open, high = high, low = low, close = targetClose))
            prevClose = targetClose
        }

        // Historical slice determines visible S/R and anchor levels
        val histCandles = candles.take(22)
        val histHigh = histCandles.maxOf { it.high }
        val histLow = histCandles.minOf { it.low }
        val histLastClose = histCandles.last().close
        val histRange = max(histHigh - histLow, startPrice * volatility * 4f)

        val supportZone = histLow + histRange * (0.10f + rng.nextFloat() * 0.12f)
        val resistanceZone = histHigh - histRange * (0.10f + rng.nextFloat() * 0.12f)

        // Align outcome candles mathematically with target & stop-loss
        val alignedCandles = alignOutcomeWithTargets(
            candles = candles,
            expectedDirection = expectedDirection,
            isTricky = isTricky,
            trickySubtype = trickySubtype,
            histLastClose = histLastClose,
            histRange = histRange,
            rng = rng
        )

        val (targetPrice, stopLossPrice) = computeTargetAndStop(
            alignedCandles = alignedCandles,
            expectedDirection = expectedDirection,
            histLastClose = histLastClose,
            histRange = histRange
        )

        val coachExplanation = buildCoachExplanation(
            archetype = archetype,
            trickySubtype = trickySubtype,
            expectedDirection = expectedDirection,
            isTricky = isTricky
        )

        val id = UUID.randomUUID().toString()
        val fingerprint = antiRepetitionEngine.buildFingerprint(
            id = id,
            asset = asset,
            timeframe = timeframe,
            archetypeName = archetype.displayName,
            trickySubtype = trickySubtype?.label ?: "Standard",
            expectedDirection = expectedDirection.name,
            candles = alignedCandles,
            wickRatio = wickRatio,
            volatility = volatility,
            trendBias = trendBias,
            supportZone = supportZone,
            resistanceZone = resistanceZone
        )

        return ChartChallenge(
            id = id,
            asset = asset,
            category = resolvedCategory,
            timeframe = timeframe,
            archetype = archetype,
            trickySubtype = trickySubtype,
            isTrickyMode = isTricky,
            candles = alignedCandles,
            targetPrice = targetPrice,
            stopLossPrice = stopLossPrice,
            expectedDirection = expectedDirection,
            supportZone = supportZone,
            resistanceZone = resistanceZone,
            volatility = volatility,
            trendBias = trendBias,
            wickRatio = wickRatio,
            coachExplanation = coachExplanation,
            fingerprint = fingerprint
        )
    }

    private fun pickArchetype(
        rng: Random,
        isTricky: Boolean,
        trickySubtype: TrickyScenarioType?
    ): PriceActionArchetype {
        if (isTricky) {
            return when (trickySubtype) {
                TrickyScenarioType.NFP_NEWS_WHIPSAW -> PriceActionArchetype.NFP_VOLATILITY_SPIKE
                TrickyScenarioType.LIQUIDITY_SWEEP -> PriceActionArchetype.LIQUIDITY_SWEEP_FAKEOUT
                TrickyScenarioType.BULL_TRAP_FAKEOUT -> PriceActionArchetype.DOUBLE_TOP_REVERSAL
                TrickyScenarioType.BEAR_TRAP_FAKEOUT -> PriceActionArchetype.DOUBLE_BOTTOM_REVERSAL
                TrickyScenarioType.STOP_LOSS_HUNT, null -> {
                    val pool = listOf(
                        PriceActionArchetype.LIQUIDITY_SWEEP_FAKEOUT,
                        PriceActionArchetype.SUPPORT_BOUNCE,
                        PriceActionArchetype.RESISTANCE_REJECTION
                    )
                    pool[rng.nextInt(pool.size)]
                }
            }
        }
        val standardPool = PriceActionArchetype.entries.filter { !it.isTrickyNative }
        return standardPool[rng.nextInt(standardPool.size)]
    }

    private fun resolveDirection(
        archetype: PriceActionArchetype,
        trickySubtype: TrickyScenarioType?,
        rng: Random
    ): TradeDirection {
        return when (trickySubtype) {
            TrickyScenarioType.BULL_TRAP_FAKEOUT -> TradeDirection.BEAR
            TrickyScenarioType.BEAR_TRAP_FAKEOUT -> TradeDirection.BULL
            TrickyScenarioType.NFP_NEWS_WHIPSAW,
            TrickyScenarioType.STOP_LOSS_HUNT,
            TrickyScenarioType.LIQUIDITY_SWEEP -> if (rng.nextBoolean()) TradeDirection.BULL else TradeDirection.BEAR
            null -> archetype.expectedDirection
        }
    }

    private fun buildPricePath(
        rng: Random,
        startPrice: Float,
        volatility: Float,
        archetype: PriceActionArchetype,
        trickySubtype: TrickyScenarioType?,
        expectedDirection: TradeDirection,
        reversalStrength: Float,
        momentum: Float
    ): FloatArray {
        val path = FloatArray(30)
        val step = startPrice * volatility
        var p = startPrice

        // Normalized shape profile across 22 historical candles (0..21)
        val histShape = FloatArray(22) { idx ->
            val t = idx / 21f
            val noise = (rng.nextFloat() - 0.5f) * 0.7f
            when (archetype) {
                PriceActionArchetype.DOUBLE_BOTTOM_REVERSAL -> {
                    when {
                        idx < 5 -> -0.6f * idx
                        idx < 9 -> -2.8f + (idx - 4) * 0.55f
                        idx < 14 -> -0.6f - (idx - 8) * 0.50f
                        else -> -3.0f + (idx - 13) * 0.40f
                    } + noise
                }
                PriceActionArchetype.DOUBLE_TOP_REVERSAL -> {
                    when {
                        idx < 5 -> 0.6f * idx
                        idx < 9 -> 2.8f - (idx - 4) * 0.55f
                        idx < 14 -> 0.6f + (idx - 8) * 0.50f
                        else -> 3.0f - (idx - 13) * 0.40f
                    } + noise
                }
                PriceActionArchetype.ASCENDING_TRIANGLE_BREAKOUT -> {
                    val lowFloor = -2.2f + t * 2.2f
                    val oscillation = if (idx % 4 < 2) 0.9f else lowFloor
                    oscillation + noise * 0.5f
                }
                PriceActionArchetype.DESCENDING_TRIANGLE_BREAKDOWN -> {
                    val highCeil = 2.2f - t * 2.2f
                    val oscillation = if (idx % 4 < 2) -0.9f else highCeil
                    oscillation + noise * 0.5f
                }
                PriceActionArchetype.HEAD_AND_SHOULDERS_DROP -> {
                    when {
                        idx < 5 -> idx * 0.45f
                        idx < 8 -> 2.0f - (idx - 4) * 0.5f
                        idx < 12 -> 0.4f + (idx - 7) * 0.75f
                        idx < 16 -> 3.4f - (idx - 11) * 0.75f
                        idx < 19 -> 0.4f + (idx - 15) * 0.55f
                        else -> 2.0f - (idx - 18) * 0.65f
                    } + noise * 0.45f
                }
                PriceActionArchetype.INVERSE_HEAD_AND_SHOULDERS -> {
                    when {
                        idx < 5 -> -idx * 0.45f
                        idx < 8 -> -2.0f + (idx - 4) * 0.5f
                        idx < 12 -> -0.4f - (idx - 7) * 0.75f
                        idx < 16 -> -3.4f + (idx - 11) * 0.75f
                        idx < 19 -> -0.4f - (idx - 15) * 0.55f
                        else -> -2.0f + (idx - 18) * 0.65f
                    } + noise * 0.45f
                }
                PriceActionArchetype.BULL_FLAG_CONTINUATION -> {
                    if (idx < 9) idx * 0.55f + noise * 0.5f
                    else 4.5f - (idx - 8) * 0.18f + noise * 0.45f
                }
                PriceActionArchetype.BEAR_FLAG_CONTINUATION -> {
                    if (idx < 9) -idx * 0.55f + noise * 0.5f
                    else -4.5f + (idx - 8) * 0.18f + noise * 0.45f
                }
                PriceActionArchetype.SUPPORT_BOUNCE -> {
                    if (idx < 13) -idx * 0.30f + noise * 0.5f
                    else -3.6f + (idx - 12) * 0.25f + noise * 0.4f
                }
                PriceActionArchetype.RESISTANCE_REJECTION -> {
                    if (idx < 13) idx * 0.30f + noise * 0.5f
                    else 3.6f - (idx - 12) * 0.25f + noise * 0.4f
                }
                PriceActionArchetype.LIQUIDITY_SWEEP_FAKEOUT -> {
                    val dirSign = if (expectedDirection == TradeDirection.BULL) 1f else -1f
                    if (idx < 16) dirSign * (idx * 0.12f) + noise * 0.6f
                    else dirSign * (1.8f - (idx - 15) * 0.55f) + noise * 0.4f
                }
                PriceActionArchetype.NFP_VOLATILITY_SPIKE -> {
                    // Tight pre-news compression box
                    ((idx % 3) - 1) * 0.45f + noise * 0.4f
                }
            }
        }

        for (i in 0 until 22) {
            p = startPrice + histShape[i] * step
            path[i] = max(0.0001f, p)
        }

        // Outcome candles (22..29)
        val anchor = path[21]
        val dir = if (expectedDirection == TradeDirection.BULL) 1f else -1f

        for (k in 0 until 8) {
            val idx = 22 + k
            val delta = when (trickySubtype) {
                TrickyScenarioType.NFP_NEWS_WHIPSAW -> {
                    // Explicitly move AGAINST eventual direction for first 2 candles, then reverse aggressively!
                    when (k) {
                        0 -> -dir * step * (1.25f + rng.nextFloat() * 0.35f)
                        1 -> -dir * step * (1.10f + rng.nextFloat() * 0.30f)
                        2 -> dir * step * (2.60f * reversalStrength)
                        3 -> dir * step * (1.85f * momentum)
                        else -> dir * step * (0.95f + rng.nextFloat() * 0.70f) * momentum
                    }
                }
                TrickyScenarioType.STOP_LOSS_HUNT,
                TrickyScenarioType.LIQUIDITY_SWEEP,
                TrickyScenarioType.BULL_TRAP_FAKEOUT,
                TrickyScenarioType.BEAR_TRAP_FAKEOUT -> {
                    // Initial trap impulse against true direction, then violent snapback
                    when (k) {
                        0 -> -dir * step * (1.15f + rng.nextFloat() * 0.35f)
                        1 -> dir * step * (2.20f * reversalStrength)
                        2 -> dir * step * (1.55f * momentum)
                        else -> dir * step * (0.85f + rng.nextFloat() * 0.65f) * momentum
                    }
                }
                null -> {
                    val pullback = if (k == 3 && rng.nextFloat() < 0.45f) -0.35f else 1.0f
                    dir * step * (0.75f + rng.nextFloat() * 0.75f) * momentum * pullback
                }
            }
            val prev = if (k == 0) anchor else path[idx - 1]
            path[idx] = max(0.0001f, prev + delta)
        }

        return path
    }

    private fun alignOutcomeWithTargets(
        candles: List<Candle>,
        expectedDirection: TradeDirection,
        isTricky: Boolean,
        trickySubtype: TrickyScenarioType?,
        histLastClose: Float,
        histRange: Float,
        rng: Random
    ): List<Candle> {
        val result = candles.toMutableList()
        val safeTargetOffset = histRange * (0.58f + rng.nextFloat() * 0.18f)
        val safeStopOffset = histRange * (0.52f + rng.nextFloat() * 0.14f)

        val targetThreshold = if (expectedDirection == TradeDirection.BULL) {
            histLastClose + safeTargetOffset
        } else {
            histLastClose - safeTargetOffset
        }
        val stopGuard = if (expectedDirection == TradeDirection.BULL) {
            histLastClose - safeStopOffset * 0.72f
        } else {
            histLastClose + safeStopOffset * 0.72f
        }

        // Ensure first 2 NFP candles never breach stopGuard while still clearly moving against eventual direction
        for (i in 22 until 30) {
            var c = result[i]
            if (expectedDirection == TradeDirection.BULL) {
                if (c.low <= stopGuard) {
                    val clampedLow = stopGuard + histRange * 0.04f
                    val clampedOpen = max(c.open, clampedLow + histRange * 0.03f)
                    val clampedClose = max(c.close, clampedLow + histRange * 0.03f)
                    c = Candle(clampedOpen, max(c.high, max(clampedOpen, clampedClose)), clampedLow, clampedClose)
                }
                if (i == 29 && c.close < targetThreshold) {
                    val boostedClose = targetThreshold + histRange * (0.08f + rng.nextFloat() * 0.10f)
                    c = Candle(c.open, max(c.high, boostedClose + histRange * 0.04f), c.low, boostedClose)
                }
            } else {
                if (c.high >= stopGuard) {
                    val clampedHigh = stopGuard - histRange * 0.04f
                    val clampedOpen = min(c.open, clampedHigh - histRange * 0.03f)
                    val clampedClose = min(c.close, clampedHigh - histRange * 0.03f)
                    c = Candle(clampedOpen, clampedHigh, min(c.low, min(clampedOpen, clampedClose)), clampedClose)
                }
                if (i == 29 && c.close > targetThreshold) {
                    val boostedClose = targetThreshold - histRange * (0.08f + rng.nextFloat() * 0.10f)
                    c = Candle(c.open, c.high, min(c.low, boostedClose - histRange * 0.04f), boostedClose)
                }
            }
            // Keep continuity from previous close
            val prevC = result[i - 1].close
            val fixedOpen = prevC
            val fixedHigh = max(c.high, max(fixedOpen, c.close))
            val fixedLow = min(c.low, min(fixedOpen, c.close))
            result[i] = Candle(fixedOpen, fixedHigh, fixedLow, c.close)
        }

        // Guarantee candle 29 reaches target beyond threshold cleanly
        if (isTricky && trickySubtype == TrickyScenarioType.NFP_NEWS_WHIPSAW) {
            // Confirm candles 22 & 23 moved against eventual direction
            val anchor = result[21].close
            val c22 = result[22]
            val c23 = result[23]
            if (expectedDirection == TradeDirection.BULL) {
                val dip1 = anchor - histRange * 0.14f
                val dip2 = dip1 - histRange * 0.12f
                result[22] = Candle(anchor, max(anchor, c22.high), min(dip1 - histRange * 0.05f, c22.low), dip1)
                result[23] = Candle(dip1, dip1 + histRange * 0.04f, max(stopGuard + histRange * 0.03f, dip2 - histRange * 0.05f), dip2)
                result[24] = Candle(dip2, dip2 + histRange * 0.42f, dip2 - histRange * 0.04f, dip2 + histRange * 0.36f)
            } else {
                val spike1 = anchor + histRange * 0.14f
                val spike2 = spike1 + histRange * 0.12f
                result[22] = Candle(anchor, max(spike1 + histRange * 0.05f, c22.high), min(anchor, c22.low), spike1)
                result[23] = Candle(spike1, min(stopGuard - histRange * 0.03f, spike2 + histRange * 0.05f), spike1 - histRange * 0.04f, spike2)
                result[24] = Candle(spike2, spike2 + histRange * 0.04f, spike2 - histRange * 0.42f, spike2 - histRange * 0.36f)
            }
            // Re-stitch candles 25..29 smoothly toward target
            for (i in 25..29) {
                val prev = result[i - 1].close
                val progress = (i - 24) / 5f
                val goal = if (expectedDirection == TradeDirection.BULL) {
                    targetThreshold + histRange * 0.12f
                } else {
                    targetThreshold - histRange * 0.12f
                }
                val nextClose = prev + (goal - prev) * (0.35f + progress * 0.45f)
                val high = max(prev, nextClose) + histRange * 0.05f
                val low = min(prev, nextClose) - histRange * 0.05f
                result[i] = Candle(prev, high, low, nextClose)
            }
        }

        return result
    }

    private fun computeTargetAndStop(
        alignedCandles: List<Candle>,
        expectedDirection: TradeDirection,
        histLastClose: Float,
        histRange: Float
    ): Pair<Float, Float> {
        val outcome = alignedCandles.drop(22)
        return if (expectedDirection == TradeDirection.BULL) {
            val outMax = outcome.maxOf { it.close }
            val outMinLow = outcome.minOf { it.low }
            val target = histLastClose + (outMax - histLastClose) * 0.72f
            val stop = min(outMinLow, histLastClose) - histRange * 0.14f
            target to stop
        } else {
            val outMin = outcome.minOf { it.close }
            val outMaxHigh = outcome.maxOf { it.high }
            val target = histLastClose - (histLastClose - outMin) * 0.72f
            val stop = max(outMaxHigh, histLastClose) + histRange * 0.14f
            target to stop
        }
    }

    private fun resolveAssetCategory(asset: String): MarketCategory {
        return when (asset) {
            "BTC/USD", "ETH/USD", "SOL/USD" -> MarketCategory.CRYPTO
            "NIFTY 50", "BANKNIFTY", "RELIANCE" -> MarketCategory.INDIAN_STOCKS
            "XAU/USD", "EUR/USD" -> MarketCategory.FOREX_GOLD
            else -> MarketCategory.US_STOCKS
        }
    }

    private fun buildCoachExplanation(
        archetype: PriceActionArchetype,
        trickySubtype: TrickyScenarioType?,
        expectedDirection: TradeDirection,
        isTricky: Boolean
    ): String {
        if (isTricky && trickySubtype != null) {
            return when (trickySubtype) {
                TrickyScenarioType.LIQUIDITY_SWEEP ->
                    "Classic Liquidity Sweep! Price pierced the key level to trigger retail stop-losses before institutional demand snapped it straight toward the target."
                TrickyScenarioType.STOP_LOSS_HUNT ->
                    "Institutional Stop-Loss Hunt detected! Smart money wicked beyond the obvious level to absorb trapped orders and launched a sharp reversal."
                TrickyScenarioType.BULL_TRAP_FAKEOUT ->
                    "Textbook Bull Trap Fakeout! Late breakout buyers got baited above resistance before aggressive sellers slammed price down to the target."
                TrickyScenarioType.BEAR_TRAP_FAKEOUT ->
                    "Classic Bear Trap Fakeout! Sellers chased the breakdown below support, only for whales to absorb supply and rip price higher."
                TrickyScenarioType.NFP_NEWS_WHIPSAW ->
                    "Brutal NFP News Whipsaw! Price faked against the true move for two violent candles to clear leveraged stops before exploding to target."
            }
        }

        return when (archetype) {
            PriceActionArchetype.DOUBLE_BOTTOM_REVERSAL ->
                "Clean Double Bottom buyers stepped in twice at the demand floor with rising momentum. Bulls reclaimed structure and drove price past the target."
            PriceActionArchetype.DOUBLE_TOP_REVERSAL ->
                "Sellers defended the Double Top ceiling twice as buying momentum faded. Bears broke the neckline and accelerated cleanly toward the target."
            PriceActionArchetype.ASCENDING_TRIANGLE_BREAKOUT ->
                "Higher lows squeezed price tightly against horizontal resistance until supply ran dry. Buyers erupted through the ceiling straight to target."
            PriceActionArchetype.DESCENDING_TRIANGLE_BREAKDOWN ->
                "Lower highs steadily compressed price into a weakening support floor. Once support cracked, sellers took full control down to the target."
            PriceActionArchetype.HEAD_AND_SHOULDERS_DROP ->
                "The right shoulder failed to match the head's peak, confirming buyer exhaustion. A crisp neckline breakdown unlocked fast bearish follow-through."
            PriceActionArchetype.INVERSE_HEAD_AND_SHOULDERS ->
                "Higher right-shoulder demand proved sellers were losing control after the deep head low. Bulls broke the neckline and powered toward target."
            PriceActionArchetype.BULL_FLAG_CONTINUATION ->
                "Controlled low-volatility flag pullback digested earlier gains without breaking structure. Trend buyers reloaded and fired the next leg up."
            PriceActionArchetype.BEAR_FLAG_CONTINUATION ->
                "Weak counter-trend bounce inside the bear flag ran straight into fresh supply. Sellers resumed the dominant downtrend toward target."
            PriceActionArchetype.SUPPORT_BOUNCE ->
                "Price bounced strongly off the key support floor with rising bullish momentum. Buyers pushed price beyond the target before sellers could react."
            PriceActionArchetype.RESISTANCE_REJECTION ->
                "Price rejected sharply off overhead resistance as wicks signaled heavy supply. Bears stepped in immediately and pressed price to the target."
            PriceActionArchetype.LIQUIDITY_SWEEP_FAKEOUT,
            PriceActionArchetype.NFP_VOLATILITY_SPIKE ->
                if (expectedDirection == TradeDirection.BULL)
                    "After absorbing early volatility, buyers reclaimed control of the structure. Momentum carried price cleanly past the target line."
                else
                    "After exhausting early buyers, sellers overwhelmed the order book. Bearish momentum drove price smoothly into the target zone."
        }
    }
}
