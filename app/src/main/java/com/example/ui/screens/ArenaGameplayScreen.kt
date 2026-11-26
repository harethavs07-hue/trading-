package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.GameMode
import com.example.model.MarketCategory
import com.example.model.RoundPhase
import com.example.model.TradeDirection
import com.example.ui.components.CandlestickCanvas
import com.example.ui.components.CyberTooltipBox
import com.example.ui.components.MascotDialogueBanner
import com.example.ui.components.PostRoundResultSheet
import com.example.ui.components.TopHudBar
import com.example.ui.theme.AppShellBackground
import com.example.ui.theme.BearishCoralRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.OuterBackground
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChartArenaUiState

@Composable
fun ArenaGameplayScreen(
    uiState: ChartArenaUiState,
    onSelectCategory: (MarketCategory) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onToggleCurrency: () -> Unit,
    onOpenProModal: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSelectDirection: (TradeDirection) -> Unit,
    onLockIn: () -> Unit,
    onNextChart: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val challenge = uiState.currentChallenge
    val isRevealing = uiState.roundPhase == RoundPhase.REVEALING
    val canPickDirection = uiState.roundPhase == RoundPhase.AWAITING_PREDICTION ||
        uiState.roundPhase == RoundPhase.DIRECTION_PICKED
    val canLockIn = uiState.roundPhase == RoundPhase.DIRECTION_PICKED && uiState.selectedDirection != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OuterBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(AppShellBackground)
                .border(1.dp, CyberCyan.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopHudBar(
                stats = uiState.stats,
                selectedCategory = uiState.selectedCategory,
                gameMode = uiState.gameMode,
                isMuted = uiState.isMuted,
                isRevealing = isRevealing,
                onSelectCategory = onSelectCategory,
                onSelectGameMode = onSelectGameMode,
                onToggleCurrency = onToggleCurrency,
                onOpenProModal = onOpenProModal,
                onToggleMute = onToggleMute,
                onOpenLeaderboard = onOpenLeaderboard,
                onOpenGuide = onOpenGuide,
                onOpenAdmin = onOpenAdmin
            )

            // Asset, Timeframe & Pattern Header Strip
            if (challenge != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = challenge.asset,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("challenge_asset_text")
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCyan.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = challenge.timeframe,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (challenge.isTrickyMode) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TargetGold.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO TRAP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TargetGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        val patternText = if (uiState.roundPhase == RoundPhase.RESULT_READY) {
                            challenge.trickySubtype?.label ?: challenge.archetype.displayName
                        } else {
                            if (challenge.isTrickyMode) "Institutional Setup • Beware Fakeouts"
                            else "Price Action Setup • 22 Candles"
                        }
                        Text(
                            text = patternText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "#${uiState.uniqueChartsPlayedCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BullishNeonMint
                    )
                }
            }

            // Anime Trader Mascot Dialogue Banner
            MascotDialogueBanner(
                mascotState = uiState.mascotState,
                currencyMode = uiState.stats.currencyMode
            )

            // Main Candlestick Canvas (fills vertical arcade arena without scrolling)
            if (challenge != null) {
                CandlestickCanvas(
                    challenge = challenge,
                    revealedCount = uiState.revealedCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("candlestick_canvas")
                )
            }

            // Bottom Gameplay Controls OR Post-Round Coach Result Card
            val outcome = uiState.lastOutcome
            if (uiState.roundPhase == RoundPhase.RESULT_READY && outcome != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    PostRoundResultSheet(
                        outcome = outcome,
                        stats = uiState.stats,
                        onNextChart = onNextChart
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberTooltipBox(
                        tooltipText = if (isRevealing) "Locked for this round" else "Predict price rallies to Gold Target",
                        accentColor = BullishNeonMint,
                        modifier = Modifier.weight(1f)
                    ) {
                        TactileArcadeDirectionButton(
                            label = "BULL",
                            icon = Icons.Filled.ArrowUpward,
                            accentColor = BullishNeonMint,
                            isSelected = uiState.selectedDirection == TradeDirection.BULL,
                            enabled = canPickDirection,
                            onClick = { onSelectDirection(TradeDirection.BULL) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bull_button")
                        )
                    }

                    CyberTooltipBox(
                        tooltipText = when {
                            isRevealing -> "Revealing 8 outcome candles..."
                            canLockIn -> "Lock prediction & reveal 8 candles"
                            else -> "Pick BULL or BEAR first"
                        },
                        accentColor = if (canLockIn) TargetGold else CyberCyan,
                        modifier = Modifier.weight(1.15f)
                    ) {
                        TactileLockInButton(
                            enabled = canLockIn,
                            isRevealing = isRevealing,
                            revealedCount = uiState.revealedCount,
                            onClick = onLockIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lock_in_button")
                        )
                    }

                    CyberTooltipBox(
                        tooltipText = if (isRevealing) "Locked for this round" else "Predict price drops to Gold Target",
                        accentColor = BearishCoralRed,
                        modifier = Modifier.weight(1f)
                    ) {
                        TactileArcadeDirectionButton(
                            label = "BEAR",
                            icon = Icons.Filled.ArrowDownward,
                            accentColor = BearishCoralRed,
                            isSelected = uiState.selectedDirection == TradeDirection.BEAR,
                            enabled = canPickDirection,
                            onClick = { onSelectDirection(TradeDirection.BEAR) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bear_button")
                        )
                    }
                }
            }

            // Compact Arcade Stats Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSurface.copy(alpha = 0.85f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "W:${uiState.stats.totalWins} L:${uiState.stats.totalLosses} (${uiState.stats.winPercentage}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "BEST 🔥${uiState.stats.bestStreak} • ROUNDS ${uiState.stats.totalRounds}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TargetGold
                )
                CyberTooltipBox(
                    tooltipText = "Reset virtual capital & stats",
                    accentColor = CyberCyan
                ) {
                    IconButton(
                        onClick = onResetProgress,
                        enabled = !isRevealing,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("reset_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset virtual capital and stats",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TactileArcadeDirectionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgBrush = when {
        isSelected -> Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.34f), accentColor.copy(alpha = 0.14f)))
        isHovered && enabled -> Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.20f), Color(0xFF141C2B)))
        else -> Brush.verticalGradient(listOf(Color(0xFF1A2233), Color(0xFF101520)))
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .minimumInteractiveComponentSize()
            .shadow(if (isSelected || isHovered) 10.dp else 4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(
                width = if (isSelected || isHovered) 2.dp else 1.2.dp,
                color = if (isSelected || isHovered) accentColor else accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected || isHovered) accentColor else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TactileLockInButton(
    enabled: Boolean,
    isRevealing: Boolean,
    revealedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val activeColor = when {
        isRevealing -> CyberCyan
        enabled -> TargetGold
        isHovered -> CyberCyan.copy(alpha = 0.7f)
        else -> TextSecondary
    }
    val bgBrush = when {
        enabled -> Brush.verticalGradient(listOf(TargetGold, Color(0xFFD98200)))
        isRevealing -> Brush.verticalGradient(listOf(Color(0xFF12283A), Color(0xFF0D1B2A)))
        isHovered -> Brush.verticalGradient(listOf(Color(0xFF1E283C), Color(0xFF131926)))
        else -> Brush.verticalGradient(listOf(Color(0xFF161C29), Color(0xFF10141E)))
    }

    Box(
        modifier = modifier
            .height(62.dp)
            .minimumInteractiveComponentSize()
            .shadow(if (enabled || isHovered) 12.dp else 2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(
                width = if (enabled || isRevealing || isHovered) 2.dp else 1.dp,
                color = activeColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "Lock in prediction",
                    tint = if (enabled) OuterBackground else activeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isRevealing) "LIVE $revealedCount/8" else "LOCK IN",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) OuterBackground else activeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (isRevealing) "REVEALING" else if (enabled) "POWER CORE" else "PICK SIDE",
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) OuterBackground.copy(alpha = 0.85f) else TextSecondary
            )
        }
    }
}
