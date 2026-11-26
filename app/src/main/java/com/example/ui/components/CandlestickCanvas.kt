package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartChallenge
import com.example.ui.theme.BearishCoralRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GridLineColor
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickCanvas(
    challenge: ChartChallenge,
    revealedCount: Int,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "mysteryFog")
    val questionPulse by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "questionPulse"
    )

    val visibleLimit = (22 + revealedCount).coerceIn(22, challenge.candles.size)
    val allCandles = challenge.candles

    // Compute scale smoothly over all candles + target + stop-loss so lines never jump offscreen
    val rawMax = max(
        allCandles.maxOf { it.high },
        max(challenge.targetPrice, challenge.stopLossPrice)
    )
    val rawMin = min(
        allCandles.minOf { it.low },
        min(challenge.targetPrice, challenge.stopLossPrice)
    )
    val span = max(rawMax - rawMin, 1e-4f)
    val paddedMin = rawMin - span * 0.08f
    val paddedMax = rawMax + span * 0.08f
    val totalSpan = max(paddedMax - paddedMin, 1e-4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0E17), Color(0xFF0E1523))
                )
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            val w = size.width
            val h = size.height
            val totalSlots = 30
            val slotWidth = w / totalSlots.toFloat()
            val bodyWidth = max(4f, slotWidth * 0.62f)

            fun priceToY(price: Float): Float {
                val norm = (price - paddedMin) / totalSpan
                return h - (norm * h)
            }

            // Subtle horizontal & vertical cyber grid
            for (g in 1..4) {
                val gy = h * (g / 5f)
                drawLine(
                    color = GridLineColor,
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1.2f
                )
            }
            for (col in 5..25 step 5) {
                val gx = col * slotWidth
                drawLine(
                    color = GridLineColor.copy(alpha = 0.6f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
            }

            // Divider line between 22 historical candles and 8 outcome zone
            val splitX = 22f * slotWidth
            drawLine(
                color = CyberCyan.copy(alpha = 0.35f),
                start = Offset(splitX, 0f),
                end = Offset(splitX, h),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            // Gold dashed TARGET line
            val targetY = priceToY(challenge.targetPrice).coerceIn(8f, h - 8f)
            drawLine(
                color = TargetGold.copy(alpha = 0.92f),
                start = Offset(0f, targetY),
                end = Offset(w, targetY),
                strokeWidth = 2.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Red dashed STOP-LOSS line
            val stopY = priceToY(challenge.stopLossPrice).coerceIn(8f, h - 8f)
            drawLine(
                color = BearishCoralRed.copy(alpha = 0.92f),
                start = Offset(0f, stopY),
                end = Offset(w, stopY),
                strokeWidth = 2.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Draw visible candles (22 historical + revealedCount)
            for (i in 0 until visibleLimit) {
                val candle = allCandles[i]
                val cx = i * slotWidth + slotWidth / 2f
                val candleColor = if (candle.isBullish) BullishNeonMint else BearishCoralRed
                val isFreshlyRevealed = i >= 22 && i == visibleLimit - 1

                val highY = priceToY(candle.high)
                val lowY = priceToY(candle.low)
                val openY = priceToY(candle.open)
                val closeY = priceToY(candle.close)

                val bodyTop = min(openY, closeY)
                val bodyHeight = max(abs(closeY - openY), 4.5f)

                // Soft neon glow behind candle body
                drawRoundRect(
                    color = candleColor.copy(alpha = if (isFreshlyRevealed) 0.40f else 0.16f),
                    topLeft = Offset(cx - bodyWidth * 0.72f, bodyTop - 3f),
                    size = Size(bodyWidth * 1.44f, bodyHeight + 6f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Vertical wick
                drawLine(
                    color = candleColor,
                    start = Offset(cx, highY),
                    end = Offset(cx, lowY),
                    strokeWidth = max(2.2f, bodyWidth * 0.18f),
                    cap = StrokeCap.Round
                )

                // Rounded candle body
                drawRoundRect(
                    color = candleColor,
                    topLeft = Offset(cx - bodyWidth / 2f, bodyTop),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Dark Mystery Fog over unrevealed outcome zone
            if (visibleLimit < 30) {
                val fogStart = visibleLimit * slotWidth
                val fogWidth = w - fogStart
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xD9070A12),
                            Color(0xF506080D)
                        ),
                        startX = fogStart,
                        endX = w
                    ),
                    topLeft = Offset(fogStart, 0f),
                    size = Size(fogWidth, h)
                )

                // Vertical neon scan border on fog edge
                drawLine(
                    color = CyberCyan.copy(alpha = 0.45f * questionPulse),
                    start = Offset(fogStart, 0f),
                    end = Offset(fogStart, h),
                    strokeWidth = 2.5f
                )
            }
        }

        // Price Level Badges (Target Gold & Stop Red)
        val targetAtTop = challenge.targetPrice >= challenge.stopLossPrice
        Box(
            modifier = Modifier
                .align(if (targetAtTop) Alignment.TopStart else Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC1B1506))
                .border(1.dp, TargetGold.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = "TARGET ${formatAssetPrice(challenge.targetPrice, challenge.asset)}",
                style = MaterialTheme.typography.labelSmall,
                color = TargetGold,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(if (targetAtTop) Alignment.BottomStart else Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC1E0910))
                .border(1.dp, BearishCoralRed.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = "STOP ${formatAssetPrice(challenge.stopLossPrice, challenge.asset)}",
                style = MaterialTheme.typography.labelSmall,
                color = BearishCoralRed,
                fontWeight = FontWeight.Bold
            )
        }

        // Glowing Question Mark inside Mystery Fog when unrevealed
        if (revealedCount == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan.copy(alpha = questionPulse),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "8 CANDLES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 54.dp)
                )
            }
        }
    }
}
