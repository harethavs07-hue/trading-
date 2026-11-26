package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameMode
import com.example.model.MascotState
import com.example.model.TradeDirection
import com.example.ui.theme.BearishCoralRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.OuterBackground
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private enum class CandlePart(val label: String, val bullTip: String, val bearTip: String) {
    UPPER_WICK(
        "UPPER WICK",
        "Shows the highest price reached before sellers pushed price back down.",
        "Long upper wicks signal strong seller rejection at resistance (Bull Trap clue!)."
    ),
    BODY(
        "REAL BODY",
        "Green/Mint body means Close > Open — buyers won control of this candle.",
        "Red body means Close < Open — sellers dominated from open to close."
    ),
    OPEN_CLOSE(
        "OPEN / CLOSE",
        "Bottom of a green body is Open; top of the green body is Close.",
        "Top of a red body is Open; bottom of the red body is Close."
    ),
    LOWER_WICK(
        "LOWER WICK",
        "Long lower wicks show buyers aggressively absorbing dips — a Liquidity Sweep clue!",
        "Shows the lowest price tested before minor profit-taking lifted the close."
    )
}

@Composable
fun HowToPlayModal(
    onDismiss: () -> Unit,
    onJumpToMode: (GameMode) -> Unit
) {
    var activeStep by remember { mutableIntStateOf(0) } // 0, 1, 2

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .widthIn(max = 415.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardElevated, CardSurface)
                    )
                )
                .border(1.5.dp, CyberCyan.copy(alpha = 0.75f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.16f))
                            .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "Academy Guide",
                            tint = CyberCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SNIPER ACADEMY",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = "Interactive Candlestick & Arena Guide",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_how_to_play_modal")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close guide",
                        tint = TextSecondary
                    )
                }
            }

            // Interactive Step Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val titles = listOf("1. CANDLES", "2. MODES", "3. PRACTICE")
                titles.forEachIndexed { idx, label ->
                    val selected = activeStep == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) CyberCyan.copy(alpha = 0.20f) else OuterBackground.copy(alpha = 0.55f))
                            .border(
                                1.dp,
                                if (selected) CyberCyan else BorderSubtle,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeStep = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) CyberCyan else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Step Body (scrollable safely on compact screens)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (activeStep) {
                    0 -> CandleAnatomyStep()
                    1 -> ArenaModesStep(onSelectModePreview = onJumpToMode)
                    else -> PracticeDrillStep(onStartPlaying = onDismiss)
                }
            }

            // Bottom Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeStep > 0) {
                    OutlinedButton(
                        onClick = { activeStep -= 1 },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .height(48.dp)
                            .testTag("guide_prev_button")
                    ) {
                        Text("BACK", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .height(48.dp)
                            .testTag("guide_skip_button")
                    ) {
                        Text("SKIP", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }

                Button(
                    onClick = {
                        if (activeStep < 2) activeStep += 1 else onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeStep < 2) CyberCyan else BullishNeonMint,
                        contentColor = OuterBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("guide_next_button")
                ) {
                    Text(
                        text = if (activeStep < 2) "NEXT STEP →" else "ENTER ARENA ⚡",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CandleAnatomyStep() {
    var isBullish by remember { mutableStateOf(true) }
    var selectedPart by remember { mutableStateOf(CandlePart.BODY) }

    val candleColor by animateColorAsState(
        targetValue = if (isBullish) BullishNeonMint else BearishCoralRed,
        animationSpec = tween(220),
        label = "demoCandleColor"
    )

    // Toggle Bullish vs Bearish Candle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isBullish) BullishNeonMint.copy(alpha = 0.2f) else OuterBackground.copy(alpha = 0.6f))
                .border(1.dp, if (isBullish) BullishNeonMint else BorderSubtle, RoundedCornerShape(10.dp))
                .clickable { isBullish = true }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "BULLISH (MINT ↑)",
                style = MaterialTheme.typography.labelSmall,
                color = if (isBullish) BullishNeonMint else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!isBullish) BearishCoralRed.copy(alpha = 0.2f) else OuterBackground.copy(alpha = 0.6f))
                .border(1.dp, if (!isBullish) BearishCoralRed else BorderSubtle, RoundedCornerShape(10.dp))
                .clickable { isBullish = false }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "BEARISH (RED ↓)",
                style = MaterialTheme.typography.labelSmall,
                color = if (!isBullish) BearishCoralRed else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Interactive Candle Diagram Canvas
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(OuterBackground)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.34f
            val h = size.height
            val highY = h * 0.08f
            val bodyTopY = h * 0.28f
            val bodyBottomY = h * 0.72f
            val lowY = h * 0.92f
            val bodyW = 46f

            // Highlight glow for selected part
            when (selectedPart) {
                CandlePart.UPPER_WICK -> {
                    drawLine(TargetGold, Offset(cx, highY), Offset(cx, bodyTopY), strokeWidth = 10f, cap = StrokeCap.Round)
                }
                CandlePart.LOWER_WICK -> {
                    drawLine(TargetGold, Offset(cx, bodyBottomY), Offset(cx, lowY), strokeWidth = 10f, cap = StrokeCap.Round)
                }
                CandlePart.BODY -> {
                    drawRoundRect(
                        color = TargetGold.copy(alpha = 0.35f),
                        topLeft = Offset(cx - bodyW * 0.75f, bodyTopY - 6f),
                        size = Size(bodyW * 1.5f, (bodyBottomY - bodyTopY) + 12f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
                CandlePart.OPEN_CLOSE -> {
                    drawLine(
                        TargetGold,
                        Offset(cx - bodyW, bodyTopY),
                        Offset(size.width * 0.92f, bodyTopY),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    drawLine(
                        TargetGold,
                        Offset(cx - bodyW, bodyBottomY),
                        Offset(size.width * 0.92f, bodyBottomY),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }
            }

            // Wicks
            drawLine(candleColor, Offset(cx, highY), Offset(cx, lowY), strokeWidth = 5f, cap = StrokeCap.Round)
            // Body
            drawRoundRect(
                color = candleColor,
                topLeft = Offset(cx - bodyW / 2f, bodyTopY),
                size = Size(bodyW, bodyBottomY - bodyTopY),
                cornerRadius = CornerRadius(8f, 8f)
            )
            if (selectedPart == CandlePart.BODY) {
                drawRoundRect(
                    color = TargetGold,
                    topLeft = Offset(cx - bodyW / 2f, bodyTopY),
                    size = Size(bodyW, bodyBottomY - bodyTopY),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 3.5f)
                )
            }
        }

        // Right-side aligned callout labels
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.55f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "HIGH WICK (Peak Price)",
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedPart == CandlePart.UPPER_WICK) TargetGold else TextSecondary
            )
            Text(
                text = if (isBullish) "CLOSE (Buyers finished high)" else "OPEN (Sellers started high)",
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedPart == CandlePart.OPEN_CLOSE) TargetGold else TextPrimary
            )
            Text(
                text = if (isBullish) "OPEN (Started lower)" else "CLOSE (Sellers drove low)",
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedPart == CandlePart.OPEN_CLOSE) TargetGold else TextPrimary
            )
            Text(
                text = "LOW WICK (Deepest Dip)",
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedPart == CandlePart.LOWER_WICK) TargetGold else TextSecondary
            )
        }
    }

    // Tappable Anatomy Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CandlePart.entries.forEach { part ->
            val active = selectedPart == part
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) TargetGold.copy(alpha = 0.20f) else OuterBackground.copy(alpha = 0.65f))
                    .border(1.dp, if (active) TargetGold else BorderSubtle, RoundedCornerShape(8.dp))
                    .clickable { selectedPart = part }
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = part.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) TargetGold else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Dynamic Inspector Tip
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OuterBackground.copy(alpha = 0.7f))
            .border(1.dp, candleColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = if (isBullish) selectedPart.bullTip else selectedPart.bearTip,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun ArenaModesStep(
    onSelectModePreview: (GameMode) -> Unit
) {
    var inspectedMode by remember { mutableStateOf(GameMode.NORMAL) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeInspectTab(
            title = "NORMAL ARENA",
            subtitle = "FREE • CLEAN SETUPS",
            accent = BullishNeonMint,
            selected = inspectedMode == GameMode.NORMAL,
            onClick = { inspectedMode = GameMode.NORMAL },
            modifier = Modifier.weight(1f)
        )
        ModeInspectTab(
            title = "TRICKY CHARTS",
            subtitle = "PRO • WHALE TRAPS",
            accent = TargetGold,
            selected = inspectedMode == GameMode.TRICKY,
            onClick = { inspectedMode = GameMode.TRICKY },
            modifier = Modifier.weight(1f)
        )
    }

    if (inspectedMode == GameMode.NORMAL) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OuterBackground.copy(alpha = 0.72f))
                .border(1.dp, BullishNeonMint.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = BullishNeonMint, modifier = Modifier.size(18.dp))
                Text("NORMAL ARENA (UNLIMITED)", style = MaterialTheme.typography.titleSmall, color = BullishNeonMint)
            }
            Text(
                "• Classic price action: Double Bottoms/Tops, Flags, Support Bounces, Resistance Rejections & Triangle Breakouts.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                "• Switch anytime between Crypto, Indian Stocks (NIFTY/BANKNIFTY), Forex/Gold, US Stocks, or All Mix.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OuterBackground.copy(alpha = 0.72f))
                .border(1.dp, TargetGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = TargetGold, modifier = Modifier.size(18.dp))
                Text("TRICKY CHARTS (PRO TRAPS)", style = MaterialTheme.typography.titleSmall, color = TargetGold)
            }
            Text(
                "• Institutional manipulation: Stop-Loss Hunts, Liquidity Sweeps, Bull/Bear Trap Fakeouts & NFP News Whipsaws.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                "• NFP Whipsaw Warning: Price intentionally fakes AGAINST the true direction for 2 candles before snapping violently to target!",
                style = MaterialTheme.typography.bodySmall,
                color = TargetGold
            )
            OutlinedButton(
                onClick = { onSelectModePreview(GameMode.TRICKY) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OPEN TRICKY CHARTS / PRO UNLOCK", style = MaterialTheme.typography.labelSmall, color = TargetGold)
            }
        }
    }

    // Target / Stop / Mystery Fog Key
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(OuterBackground.copy(alpha = 0.6f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("CHART ZONE KEY", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
        Text("🟡 GOLD DASHED LINE = TARGET (Where your predicted move pays +₹5,000 / +$500)", style = MaterialTheme.typography.bodySmall, color = TargetGold)
        Text("🔴 RED DASHED LINE = STOP-LOSS (Invalidation boundary)", style = MaterialTheme.typography.bodySmall, color = BearishCoralRed)
        Text("❓ MYSTERY FOG = 8 hidden outcome candles revealed 1-by-1 after LOCK IN", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun ModeInspectTab(
    title: String,
    subtitle: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accent.copy(alpha = 0.16f) else OuterBackground.copy(alpha = 0.5f))
            .border(1.2.dp, if (selected) accent else BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = if (selected) accent else TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun PracticeDrillStep(
    onStartPlaying: () -> Unit
) {
    var picked by remember { mutableStateOf<TradeDirection?>(null) }
    var isLocked by remember { mutableStateOf(false) }
    var revealStep by remember { mutableIntStateOf(0) } // 0..3

    LaunchedEffect(isLocked) {
        if (isLocked && revealStep < 3) {
            for (s in 1..3) {
                delay(240L)
                revealStep = s
            }
        }
    }

    val mascotState = when {
        revealStep == 3 && picked == TradeDirection.BULL -> MascotState.WIN
        revealStep == 3 && picked == TradeDirection.BEAR -> MascotState.LOSS
        isLocked -> MascotState.DIRECTION_SELECTED
        else -> MascotState.IDLE
    }

    Text(
        text = "MINI DRILL: Ascending Triangle squeezing into resistance. Pick BULL or BEAR and hit TEST LOCK IN!",
        style = MaterialTheme.typography.bodySmall,
        color = TextPrimary
    )

    // Mini Practice Chart Canvas
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(OuterBackground)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val slotW = w / 9f

            // Target line (Gold)
            drawLine(
                color = TargetGold,
                start = Offset(0f, h * 0.14f),
                end = Offset(w, h * 0.14f),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
            // Resistance line (Cyan)
            drawLine(
                color = CyberCyan.copy(alpha = 0.55f),
                start = Offset(0f, h * 0.42f),
                end = Offset(w * 0.68f, h * 0.42f),
                strokeWidth = 2f
            )

            // 6 historical mini candles forming higher lows against flat resistance
            val hist = listOf(
                Triple(0.75f, 0.46f, true),
                Triple(0.47f, 0.66f, false),
                Triple(0.65f, 0.44f, true),
                Triple(0.45f, 0.58f, false),
                Triple(0.57f, 0.43f, true),
                Triple(0.44f, 0.41f, true)
            )
            // 3 breakout outcome candles
            val outcome = listOf(
                Triple(0.41f, 0.30f, true),
                Triple(0.30f, 0.21f, true),
                Triple(0.21f, 0.10f, true)
            )

            val all = hist + outcome.take(revealStep)
            all.forEachIndexed { idx, (openNorm, closeNorm, bull) ->
                val cx = idx * slotW + slotW / 2f
                val color = if (bull) BullishNeonMint else BearishCoralRed
                val topY = minOf(openNorm, closeNorm) * h
                val botY = maxOf(openNorm, closeNorm) * h
                drawLine(color, Offset(cx, topY - 7f), Offset(cx, botY + 7f), strokeWidth = 3f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(cx - slotW * 0.28f, topY),
                    size = Size(slotW * 0.56f, maxOf(botY - topY, 6f)),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            if (revealStep < 3) {
                val fogX = (6 + revealStep) * slotW
                drawRect(
                    color = Color(0xCC06080D),
                    topLeft = Offset(fogX, 0f),
                    size = Size(w - fogX, h)
                )
            }
        }
    }

    // Practice Controls
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { if (!isLocked) picked = TradeDirection.BULL },
            enabled = !isLocked,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("guide_drill_bull")
        ) {
            Text(
                "BULL ↑",
                color = if (picked == TradeDirection.BULL) BullishNeonMint else TextPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Button(
            onClick = { if (picked != null) isLocked = true },
            enabled = picked != null && !isLocked,
            colors = ButtonDefaults.buttonColors(containerColor = TargetGold, contentColor = OuterBackground),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1.2f)
                .testTag("guide_drill_lock")
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
            Text("TEST LOCK IN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = { if (!isLocked) picked = TradeDirection.BEAR },
            enabled = !isLocked,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("guide_drill_bear")
        ) {
            Text(
                "BEAR ↓",
                color = if (picked == TradeDirection.BEAR) BearishCoralRed else TextPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OuterBackground.copy(alpha = 0.72f))
            .border(1.dp, CyberCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimeTraderMascotAvatar(state = mascotState, size = 44.dp)
        val tip = when {
            revealStep == 3 && picked == TradeDirection.BULL ->
                "BOOM! Higher lows broke resistance straight into the Gold Target. You're ready for the Arena!"
            revealStep == 3 && picked == TradeDirection.BEAR ->
                "Stopped out! Notice the rising support squeezing buyers upward. Ready to try live charts?"
            isLocked -> "Revealing 3 practice outcome candles..."
            picked != null -> "Direction chosen! Hit TEST LOCK IN to see the breakout."
            else -> "Pick BULL ↑ or BEAR ↓ above to test your first call!"
        }
        Text(
            text = tip,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }

    if (revealStep == 3) {
        OutlinedButton(
            onClick = {
                picked = null
                isLocked = false
                revealStep = 0
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REPLAY MINI DRILL", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
        }
    }
}
