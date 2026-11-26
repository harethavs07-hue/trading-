package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DailyWinnerRecord
import com.example.model.OwnerConsoleData
import com.example.ui.theme.AppShellBackground
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.OuterBackground
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardAndTournamentScreen(
    ownerData: OwnerConsoleData,
    todayDate: String,
    isProUnlocked: Boolean,
    onBack: () -> Unit,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val availableDates = remember(ownerData.dailyWinners) {
        ownerData.dailyWinners.map { it.date }.distinct().sortedDescending()
    }
    var selectedDate by remember(availableDates) {
        mutableStateOf(availableDates.firstOrNull() ?: todayDate)
    }

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
                .border(1.dp, TargetGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("leaderboard_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Arena",
                            tint = TextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "OFFICIAL ARENA RANKS",
                            style = MaterialTheme.typography.titleMedium,
                            color = TargetGold
                        )
                        Text(
                            text = "Verified & Published by Platform Owner",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Owner Verified",
                    tint = BullishNeonMint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = TargetGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TargetGold
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("OFFICIAL BOARD", style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("SNIPER CUP", style = MaterialTheme.typography.labelMedium) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Date Selector Chips
                if (availableDates.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableDates.take(4).forEach { date ->
                            val active = date == selectedDate
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) TargetGold.copy(alpha = 0.2f) else CardSurface)
                                    .border(
                                        1.dp,
                                        if (active) TargetGold else BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDate = date }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) TargetGold else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val dayRecords = ownerData.dailyWinners
                    .filter { it.date == selectedDate }
                    .sortedBy { it.rank }

                if (dayRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No official winners published for $selectedDate.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dayRecords, key = { it.id }) { winner ->
                            OfficialWinnerRow(winner = winner)
                        }
                    }
                }
            } else {
                // Monthly Chart Sniper Cup
                val tournament = ownerData.tournament
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardElevated)
                                .border(1.dp, TargetGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = TargetGold
                                )
                                Text(
                                    text = tournament.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TargetGold
                                )
                            }
                            Text(
                                text = "Dates: ${tournament.startDate} to ${tournament.endDate}",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan
                            )
                            Text(
                                text = tournament.rules,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isProUnlocked) BullishNeonMint.copy(alpha = 0.14f)
                                        else TargetGold.copy(alpha = 0.14f)
                                    )
                                    .clickable { if (!isProUnlocked) onOpenProModal() }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isProUnlocked) "PRO ELIGIBILITY: ACTIVE ✓" else "PRO ELIGIBILITY: REQUIRED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isProUnlocked) BullishNeonMint else TargetGold
                                )
                                if (!isProUnlocked) {
                                    Text(
                                        text = "UNLOCK →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TargetGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    items(tournament.entries.sortedBy { it.rank }, key = { it.id }) { entry ->
                        val rankColor = when (entry.rank) {
                            1 -> TargetGold
                            2 -> Color(0xFFD4DCE8)
                            3 -> Color(0xFFE08E55)
                            else -> TextSecondary
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "#${entry.rank}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = rankColor
                                )
                                Column {
                                    Text(
                                        text = entry.playerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${entry.instagramHandle.ifBlank { "Verified Sniper" }} • ${entry.wins}W • ${if (entry.proEligible) "PRO" else "FREE"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Text(
                                text = "+${entry.score}",
                                style = MaterialTheme.typography.titleMedium,
                                color = BullishNeonMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficialWinnerRow(winner: DailyWinnerRecord) {
    val medalColor = when (winner.rank) {
        1 -> TargetGold
        2 -> Color(0xFFD4DCE8)
        3 -> Color(0xFFE08E55)
        else -> CyberCyan
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .border(1.dp, medalColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(medalColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${winner.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = medalColor
                )
            }
            Column {
                Text(
                    text = winner.playerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = "${winner.wins}W-${winner.losses}L • 🔥${winner.streak} • ${winner.gameMode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${winner.score}",
                style = MaterialTheme.typography.titleMedium,
                color = BullishNeonMint,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = winner.date,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
