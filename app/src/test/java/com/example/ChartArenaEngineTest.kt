package com.example

import com.example.engine.AntiRepetitionEngine
import com.example.engine.ChartGeneratorEngine
import com.example.model.GameMode
import com.example.model.MarketCategory
import com.example.model.TradeDirection
import com.example.model.TrickyScenarioType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class ChartArenaEngineTest {

    @Test
    fun generatesValid30CandlesAndAlignedTargetsAcrossNormalAndTricky() {
        val antiRep = AntiRepetitionEngine(maxHistorySize = 10_000)
        val generator = ChartGeneratorEngine(antiRep)

        val seenHashes = HashSet<String>()
        for (i in 0 until 50) {
            val mode = if (i % 2 == 0) GameMode.NORMAL else GameMode.TRICKY
            val challenge = generator.generateUniqueChallenge(
                category = MarketCategory.ALL_MIX,
                gameMode = mode,
                similarityThreshold = 0.85f
            )

            assertEquals("Expected 30 candles (22 hist + 8 outcome)", 30, challenge.candles.size)
            assertTrue("Unique hash required", seenHashes.add(challenge.fingerprint.structureHash))

            challenge.candles.forEach { c ->
                assertTrue("High >= max(open, close)", c.high >= max(c.open, c.close) - 1e-4f)
                assertTrue("Low <= min(open, close)", c.low <= min(c.open, c.close) + 1e-4f)
            }

            val outcomeCandles = challenge.candles.drop(22)
            if (challenge.expectedDirection == TradeDirection.BULL) {
                assertTrue("BULL target must exceed stopLoss", challenge.targetPrice > challenge.stopLossPrice)
                assertTrue(
                    "BULL outcome must reach target",
                    outcomeCandles.maxOf { it.close } >= challenge.targetPrice
                )
            } else {
                assertTrue("BEAR target must be below stopLoss", challenge.targetPrice < challenge.stopLossPrice)
                assertTrue(
                    "BEAR outcome must reach target",
                    outcomeCandles.minOf { it.close } <= challenge.targetPrice
                )
            }

            // Check NFP 2-candle anti-direction whipsaw rule when NFP occurs
            if (challenge.trickySubtype == TrickyScenarioType.NFP_NEWS_WHIPSAW) {
                val anchor = challenge.candles[21].close
                val c22 = challenge.candles[22].close
                val c23 = challenge.candles[23].close
                if (challenge.expectedDirection == TradeDirection.BULL) {
                    assertTrue("NFP BULL candle 22 moves against direction", c22 < anchor)
                    assertTrue("NFP BULL candle 23 continues against direction", c23 < c22)
                } else {
                    assertTrue("NFP BEAR candle 22 moves against direction", c22 > anchor)
                    assertTrue("NFP BEAR candle 23 continues against direction", c23 > c22)
                }
            }
        }
        assertEquals(50, antiRep.size())
    }

    @Test
    fun antiRepetitionRejectsExactDuplicateFingerprint() {
        val antiRep = AntiRepetitionEngine()
        val generator = ChartGeneratorEngine(antiRep)
        val first = generator.generateUniqueChallenge(MarketCategory.CRYPTO, GameMode.NORMAL)
        assertFalse(
            "Same fingerprint must be rejected",
            antiRep.isAcceptable(first.fingerprint, 0.85f)
        )
    }
}
