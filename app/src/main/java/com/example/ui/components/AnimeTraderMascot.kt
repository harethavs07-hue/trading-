package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.CurrencyMode
import com.example.model.MascotState
import com.example.ui.theme.BearishCoralRed
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnimeTraderMascotAvatar(
    state: MascotState,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val accentColor by animateColorAsState(
        targetValue = when (state) {
            MascotState.IDLE -> CyberCyan
            MascotState.DIRECTION_SELECTED -> TargetGold
            MascotState.REVEALING -> CyberCyan
            MascotState.WIN, MascotState.TRAP_SURVIVED -> BullishNeonMint
            MascotState.LOSS -> BearishCoralRed
        },
        animationSpec = tween(250),
        label = "mascotAccent"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mascotPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Glowing aura backdrop
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = pulseAlpha), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.55f
            ),
            radius = w * 0.52f,
            center = Offset(cx, cy)
        )

        // Dark cyber badge circle
        drawCircle(
            color = Color(0xFF0E1422),
            radius = w * 0.44f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.85f),
            radius = w * 0.44f,
            center = Offset(cx, cy),
            style = Stroke(width = w * 0.035f)
        )

        // Dark cyber hoodie shoulders
        val hoodiePath = Path().apply {
            moveTo(w * 0.16f, h * 0.90f)
            quadraticTo(w * 0.24f, h * 0.66f, cx, h * 0.67f)
            quadraticTo(w * 0.76f, h * 0.66f, w * 0.84f, h * 0.90f)
            close()
        }
        drawPath(hoodiePath, color = Color(0xFF1A2234))
        drawPath(
            hoodiePath,
            color = accentColor.copy(alpha = 0.65f),
            style = Stroke(width = w * 0.025f)
        )

        // Hoodie collar V-trim
        val collarPath = Path().apply {
            moveTo(w * 0.36f, h * 0.72f)
            lineTo(cx, h * 0.84f)
            lineTo(w * 0.64f, h * 0.72f)
        }
        drawPath(
            collarPath,
            color = accentColor,
            style = Stroke(width = w * 0.028f, cap = StrokeCap.Round)
        )

        // Outer anime hood silhouette
        val hoodArc = Path().apply {
            moveTo(w * 0.22f, h * 0.62f)
            quadraticTo(w * 0.18f, h * 0.18f, cx, h * 0.14f)
            quadraticTo(w * 0.82f, h * 0.18f, w * 0.78f, h * 0.62f)
            close()
        }
        drawPath(hoodArc, color = Color(0xFF151C2C))

        // Face shape (sharp anime chin)
        val facePath = Path().apply {
            moveTo(w * 0.30f, h * 0.34f)
            lineTo(w * 0.29f, h * 0.54f)
            quadraticTo(w * 0.34f, h * 0.69f, cx, h * 0.71f)
            quadraticTo(w * 0.66f, h * 0.69f, w * 0.71f, h * 0.54f)
            lineTo(w * 0.70f, h * 0.34f)
            close()
        }
        drawPath(facePath, color = Color(0xFFF6D5C4))

        // Spiky cyberpunk anime hair bangs
        val hairPath = Path().apply {
            moveTo(w * 0.24f, h * 0.45f)
            lineTo(w * 0.28f, h * 0.23f)
            lineTo(w * 0.42f, h * 0.20f)
            lineTo(cx, h * 0.17f)
            lineTo(w * 0.64f, h * 0.21f)
            lineTo(w * 0.74f, h * 0.27f)
            lineTo(w * 0.75f, h * 0.45f)
            lineTo(w * 0.66f, h * 0.34f)
            lineTo(w * 0.57f, h * 0.43f)
            lineTo(cx, h * 0.33f)
            lineTo(w * 0.41f, h * 0.44f)
            lineTo(w * 0.33f, h * 0.34f)
            close()
        }
        drawPath(hairPath, color = Color(0xFF1A1E2E))
        // Neon hair highlight streak
        drawLine(
            color = accentColor,
            start = Offset(w * 0.43f, h * 0.23f),
            end = Offset(w * 0.41f, h * 0.39f),
            strokeWidth = w * 0.025f,
            cap = StrokeCap.Round
        )

        // Gaming Headset Band & Glowing Earcups
        drawArc(
            color = Color(0xFF28334A),
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.20f, h * 0.13f),
            size = Size(w * 0.60f, h * 0.52f),
            style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
        )
        // Left & Right Neon Earcups
        drawRoundRect(
            color = Color(0xFF1F2A3E),
            topLeft = Offset(w * 0.18f, h * 0.38f),
            size = Size(w * 0.11f, h * 0.21f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(w * 0.20f, h * 0.42f),
            size = Size(w * 0.04f, h * 0.13f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )
        drawRoundRect(
            color = Color(0xFF1F2A3E),
            topLeft = Offset(w * 0.71f, h * 0.38f),
            size = Size(w * 0.11f, h * 0.21f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(w * 0.76f, h * 0.42f),
            size = Size(w * 0.04f, h * 0.13f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )
        // Boom mic
        drawLine(
            color = Color(0xFF33415C),
            start = Offset(w * 0.24f, h * 0.55f),
            end = Offset(w * 0.42f, h * 0.64f),
            strokeWidth = w * 0.024f,
            cap = StrokeCap.Round
        )
        drawCircle(color = accentColor, radius = w * 0.022f, center = Offset(w * 0.43f, h * 0.64f))

        // Dynamic Anime Eyes & Mouth
        when (state) {
            MascotState.LOSS -> {
                // Shocked > < anime eyes
                drawLine(Color(0xFF121622), Offset(w * 0.35f, h * 0.45f), Offset(w * 0.43f, h * 0.49f), w * 0.028f, StrokeCap.Round)
                drawLine(Color(0xFF121622), Offset(w * 0.43f, h * 0.49f), Offset(w * 0.35f, h * 0.53f), w * 0.028f, StrokeCap.Round)
                drawLine(Color(0xFF121622), Offset(w * 0.65f, h * 0.45f), Offset(w * 0.57f, h * 0.49f), w * 0.028f, StrokeCap.Round)
                drawLine(Color(0xFF121622), Offset(w * 0.57f, h * 0.49f), Offset(w * 0.65f, h * 0.53f), w * 0.028f, StrokeCap.Round)
                // Shocked mouth
                drawCircle(Color(0xFF992233), radius = w * 0.032f, center = Offset(cx, h * 0.62f))
            }
            MascotState.WIN, MascotState.TRAP_SURVIVED -> {
                // Sharp triumphant eyes + smirk
                drawRoundRect(
                    color = Color(0xFF121622),
                    topLeft = Offset(w * 0.34f, h * 0.45f),
                    size = Size(w * 0.11f, h * 0.075f),
                    cornerRadius = CornerRadius(w * 0.03f)
                )
                drawCircle(BullishNeonMint, radius = w * 0.026f, center = Offset(w * 0.395f, h * 0.49f))
                drawCircle(Color.White, radius = w * 0.011f, center = Offset(w * 0.385f, h * 0.475f))

                // Confident wink on right eye
                drawArc(
                    color = Color(0xFF121622),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.55f, h * 0.46f),
                    size = Size(w * 0.11f, h * 0.07f),
                    style = Stroke(width = w * 0.028f, cap = StrokeCap.Round)
                )
                // Victory grin
                drawArc(
                    color = Color(0xFF121622),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.43f, h * 0.58f),
                    size = Size(w * 0.14f, h * 0.06f),
                    style = Stroke(width = w * 0.026f, cap = StrokeCap.Round)
                )
            }
            else -> {
                // Sharp focused anime eyes with neon iris
                drawRoundRect(
                    color = Color(0xFF121622),
                    topLeft = Offset(w * 0.34f, h * 0.455f),
                    size = Size(w * 0.11f, h * 0.07f),
                    cornerRadius = CornerRadius(w * 0.025f)
                )
                drawRoundRect(
                    color = Color(0xFF121622),
                    topLeft = Offset(w * 0.55f, h * 0.455f),
                    size = Size(w * 0.11f, h * 0.07f),
                    cornerRadius = CornerRadius(w * 0.025f)
                )
                drawCircle(accentColor, radius = w * 0.025f, center = Offset(w * 0.395f, h * 0.49f))
                drawCircle(accentColor, radius = w * 0.025f, center = Offset(w * 0.605f, h * 0.49f))
                drawCircle(Color.White, radius = w * 0.01f, center = Offset(w * 0.385f, h * 0.478f))
                drawCircle(Color.White, radius = w * 0.01f, center = Offset(w * 0.595f, h * 0.478f))

                // Focused angled brows
                drawLine(Color(0xFF121622), Offset(w * 0.33f, h * 0.425f), Offset(w * 0.45f, h * 0.445f), w * 0.022f, StrokeCap.Round)
                drawLine(Color(0xFF121622), Offset(w * 0.67f, h * 0.425f), Offset(w * 0.55f, h * 0.445f), w * 0.022f, StrokeCap.Round)

                // Determined mouth
                drawLine(Color(0xFF121622), Offset(w * 0.45f, h * 0.61f), Offset(w * 0.55f, h * 0.605f), w * 0.022f, StrokeCap.Round)
            }
        }
    }
}

@Composable
fun MascotDialogueBanner(
    mascotState: MascotState,
    currencyMode: CurrencyMode,
    modifier: Modifier = Modifier
) {
    val (speakerTag, message, accent) = when (mascotState) {
        MascotState.IDLE -> Triple("SNIPER AI", "NEXT MOVE? BULL OR BEAR?", CyberCyan)
        MascotState.DIRECTION_SELECTED -> Triple("LOCKED", "LOCKED IN! HIT THE POWER CORE ⚡", TargetGold)
        MascotState.REVEALING -> Triple("LIVE FEED", "REVEALING LIQUIDITY CANDLES...", CyberCyan)
        MascotState.WIN -> {
            val prize = if (currencyMode == CurrencyMode.INR) "+₹5,000" else "+$500"
            Triple("SNIPE HIT", "CLEAN SNIPE! $prize 💰", BullishNeonMint)
        }
        MascotState.TRAP_SURVIVED -> Triple("PRO READ", "SURVIVED THE LIQUIDITY SWEEP! 🔥", BullishNeonMint)
        MascotState.LOSS -> Triple("STOPPED", "LIQUIDATED! WATCH THAT WICK! 😱", BearishCoralRed)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimeTraderMascotAvatar(state = mascotState, size = 44.dp)
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = speakerTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
