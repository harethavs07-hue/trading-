package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CurrencyMode
import com.example.model.GameMode
import com.example.model.MarketCategory
import com.example.model.PlayerStats
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopHudBar(
    stats: PlayerStats,
    selectedCategory: MarketCategory,
    gameMode: GameMode,
    isMuted: Boolean,
    isRevealing: Boolean,
    onSelectCategory: (MarketCategory) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onToggleCurrency: () -> Unit,
    onOpenProModal: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CardElevated, CardSurface)
                )
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Row 1: Logo + Streak + Action Icons (Leaderboard, Sound, Owner Admin)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BullishNeonMint.copy(alpha = 0.16f))
                        .border(1.dp, BullishNeonMint, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Chart Arena logo",
                        tint = BullishNeonMint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "CHART ARENA",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Live Winning Streak Pill
                CyberTooltipBox(
                    tooltipText = "Live consecutive win streak",
                    accentColor = TargetGold
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(TargetGold.copy(alpha = 0.14f))
                            .border(1.dp, TargetGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 ${stats.winningStreak}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TargetGold
                        )
                    }
                }

                CyberTooltipBox(
                    tooltipText = "Sniper Academy: chart & mode guide",
                    accentColor = BullishNeonMint
                ) {
                    IconButton(
                        onClick = onOpenGuide,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("how_to_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HelpOutline,
                            contentDescription = "How to play guide",
                            tint = BullishNeonMint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                CyberTooltipBox(
                    tooltipText = "Official Leaderboard & Sniper Cup",
                    accentColor = TargetGold
                ) {
                    IconButton(
                        onClick = onOpenLeaderboard,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("leaderboard_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Official Leaderboard and Tournament",
                            tint = TargetGold,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                CyberTooltipBox(
                    tooltipText = if (isMuted) "Unmute arcade sound effects" else "Mute arcade sound effects",
                    accentColor = CyberCyan
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute arcade sound" else "Mute arcade sound",
                            tint = if (isMuted) TextSecondary else CyberCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                CyberTooltipBox(
                    tooltipText = "Owner Control Studio (Prototype)",
                    accentColor = ElectricViolet
                ) {
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("owner_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Owner Admin Portal",
                            tint = ElectricViolet,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // Row 2: Virtual Capital + Currency Toggle + Market Category Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val activeCapital = if (stats.currencyMode == CurrencyMode.INR) {
                stats.capitalInr
            } else {
                stats.capitalUsd
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Text(
                        text = "CAPITAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = formatCurrency(activeCapital, stats.currencyMode),
                        style = MaterialTheme.typography.titleMedium,
                        color = BullishNeonMint,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("capital_text")
                    )
                }

                CyberTooltipBox(
                    tooltipText = "Toggle virtual capital: INR (₹) / USD ($)",
                    accentColor = CyberCyan
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A2334))
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onToggleCurrency() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("currency_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stats.currencyMode.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                    }
                }
            }

            // Market Category Dropdown
            CyberTooltipBox(
                tooltipText = "Select market asset pool",
                accentColor = CyberCyan
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF192131))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable(enabled = !isRevealing) { categoryMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("category_dropdown_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Select market category",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false },
                    modifier = Modifier.background(CardElevated)
                ) {
                    MarketCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (cat == selectedCategory) BullishNeonMint else TextPrimary
                                )
                            },
                            onClick = {
                                categoryMenuExpanded = false
                                onSelectCategory(cat)
                            }
                        )
                    }
                }
            }
        }
        }

        // Row 3: Game Mode Switcher (NORMAL ARENA vs TRICKY CHARTS) + Pro Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberTooltipBox(
                tooltipText = "Free classic breakouts & bounces",
                accentColor = BullishNeonMint,
                modifier = Modifier.weight(1f)
            ) {
                ModePill(
                    label = "NORMAL",
                    isSelected = gameMode == GameMode.NORMAL,
                    accent = BullishNeonMint,
                    locked = false,
                    enabled = !isRevealing,
                    onClick = { onSelectGameMode(GameMode.NORMAL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mode_normal_button")
                )
            }

            CyberTooltipBox(
                tooltipText = "Pro institutional traps & NFP whipsaws",
                accentColor = TargetGold,
                modifier = Modifier.weight(1f)
            ) {
                ModePill(
                    label = "TRICKY PRO",
                    isSelected = gameMode == GameMode.TRICKY,
                    accent = TargetGold,
                    locked = !stats.isProDemoUnlocked,
                    enabled = !isRevealing,
                    onClick = { onSelectGameMode(GameMode.TRICKY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mode_tricky_button")
                )
            }

            CyberTooltipBox(
                tooltipText = if (stats.isProDemoUnlocked) "Pro Demo active" else "View Pro benefits & Demo Unlock",
                accentColor = if (stats.isProDemoUnlocked) TargetGold else ElectricViolet
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (stats.isProDemoUnlocked) TargetGold.copy(alpha = 0.18f)
                            else ElectricViolet.copy(alpha = 0.18f)
                        )
                        .border(
                            1.dp,
                            if (stats.isProDemoUnlocked) TargetGold else ElectricViolet,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onOpenProModal() }
                        .padding(horizontal = 9.dp, vertical = 7.dp)
                        .testTag("pro_status_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = "Pro membership status",
                            tint = if (stats.isProDemoUnlocked) TargetGold else ElectricViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (stats.isProDemoUnlocked) "PRO" else "GET PRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (stats.isProDemoUnlocked) TargetGold else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePill(
    label: String,
    isSelected: Boolean,
    accent: Color,
    locked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> accent.copy(alpha = 0.18f)
                    isHovered -> accent.copy(alpha = 0.09f)
                    else -> Color(0xFF141A26)
                }
            )
            .border(
                width = if (isSelected || isHovered) 1.5.dp else 1.dp,
                color = if (isSelected) accent else if (isHovered) accent.copy(alpha = 0.65f) else BorderSubtle,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked Pro Mode",
                tint = TargetGold,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected || isHovered) accent else TextSecondary,
            maxLines = 1
        )
    }
}
