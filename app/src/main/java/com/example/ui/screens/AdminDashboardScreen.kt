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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.DailyWinnerRecord
import com.example.model.OwnerConsoleData
import com.example.model.PlayerRecord
import com.example.model.TournamentEntry
import com.example.ui.theme.AppShellBackground
import com.example.ui.theme.BearishCoralRed
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.OuterBackground
import com.example.ui.theme.TargetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    ownerData: OwnerConsoleData,
    isUnlocked: Boolean,
    todayDate: String,
    uniqueChartsCount: Int,
    onUnlockWithPin: (String) -> Boolean,
    onLockAdmin: () -> Unit,
    onBack: () -> Unit,
    onUpsertPlayer: (PlayerRecord) -> Unit,
    onDeletePlayer: (String) -> Unit,
    onUpdateRevenue: (Double) -> Unit,
    onUpsertWinner: (DailyWinnerRecord) -> Unit,
    onDeleteWinner: (String) -> Unit,
    onUpsertTournamentEntry: (TournamentEntry) -> Unit,
    onDeleteTournamentEntry: (String) -> Unit,
    onUpdateTournamentMeta: (String, String, String, String) -> Unit,
    onUpdateSimilarityThreshold: (Float) -> Unit,
    onClearChartHistory: () -> Unit,
    onExportCsv: () -> String,
    onExportJsonBackup: () -> String,
    onImportJsonBackup: (String) -> Boolean,
    onToastMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeDate by remember(todayDate) { mutableStateOf(todayDate) }

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
                .border(1.dp, ElectricViolet.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Arena",
                            tint = TextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "OWNER CONTROL STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricViolet
                        )
                        Text(
                            text = "Private Operations • Prototype Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                if (isUnlocked) {
                    OutlinedButton(onClick = onLockAdmin, shape = RoundedCornerShape(8.dp)) {
                        Text("LOCK", style = MaterialTheme.typography.labelSmall, color = BearishCoralRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isUnlocked) {
                AdminPinGate(
                    onUnlock = { pin ->
                        val ok = onUnlockWithPin(pin)
                        if (!ok) onToastMessage("Invalid prototype PIN. Try 2026.")
                    }
                )
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardSurface,
                    contentColor = ElectricViolet,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricViolet
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    val tabs = listOf("PLAYERS", "WINNERS", "IG STUDIO", "ARCHIVE")
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> PlayersAndSubsTab(
                            ownerData = ownerData,
                            todayDate = todayDate,
                            onUpsertPlayer = onUpsertPlayer,
                            onDeletePlayer = onDeletePlayer,
                            onUpdateRevenue = onUpdateRevenue
                        )
                        1 -> DailyWinnersAndCupTab(
                            ownerData = ownerData,
                            selectedDate = activeDate,
                            onSelectDate = { activeDate = it },
                            onUpsertWinner = onUpsertWinner,
                            onDeleteWinner = onDeleteWinner,
                            onUpsertTournamentEntry = onUpsertTournamentEntry,
                            onDeleteTournamentEntry = onDeleteTournamentEntry,
                            onUpdateTournamentMeta = onUpdateTournamentMeta,
                            onToastMessage = onToastMessage
                        )
                        2 -> InstagramStudioTab(
                            ownerData = ownerData,
                            selectedDate = activeDate,
                            onSelectDate = { activeDate = it },
                            onToastMessage = onToastMessage
                        )
                        3 -> ArchiveAndBackupTab(
                            ownerData = ownerData,
                            uniqueChartsCount = uniqueChartsCount,
                            onOpenDateInWinners = {
                                activeDate = it
                                selectedTab = 1
                            },
                            onOpenDateInStudio = {
                                activeDate = it
                                selectedTab = 2
                            },
                            onUpdateSimilarityThreshold = onUpdateSimilarityThreshold,
                            onClearChartHistory = onClearChartHistory,
                            onExportCsv = onExportCsv,
                            onExportJsonBackup = onExportJsonBackup,
                            onImportJsonBackup = onImportJsonBackup,
                            onToastMessage = onToastMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPinGate(
    onUnlock: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, ElectricViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Security, contentDescription = null, tint = ElectricViolet)
            Text("LOCAL PROTOTYPE ACCESS", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        Text(
            text = "Important Security Notice: A client-side PIN is strictly for prototype navigation and is NOT real security. Do not rely on local PIN checks in production—connect server-side authentication and database rules before live deployment.",
            style = MaterialTheme.typography.bodySmall,
            color = TargetGold
        )

        OutlinedTextField(
            value = pinInput,
            onValueChange = { pinInput = it },
            label = { Text("Owner Access PIN (Demo: 2026)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_pin_input")
        )

        Button(
            onClick = { onUnlock(pinInput) },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet, contentColor = TextPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_unlock_button")
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("ENTER OWNER STUDIO", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PlayersAndSubsTab(
    ownerData: OwnerConsoleData,
    todayDate: String,
    onUpsertPlayer: (PlayerRecord) -> Unit,
    onDeletePlayer: (String) -> Unit,
    onUpdateRevenue: (Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingPlayer by remember { mutableStateOf<PlayerRecord?>(null) }
    var revenueInput by remember(ownerData.monthlyRevenueInr) {
        mutableStateOf(ownerData.monthlyRevenueInr.toLong().toString())
    }

    val activeSubs = ownerData.players.count { it.active && it.membershipStatus == "Pro" }
    val expiredSubs = ownerData.players.count { !it.active || it.paymentStatus.equals("Expired", ignoreCase = true) }
    val totalPro = ownerData.players.count { it.membershipStatus == "Pro" }

    val filteredPlayers = remember(ownerData.players, searchQuery) {
        if (searchQuery.isBlank()) ownerData.players
        else ownerData.players.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.instagramHandle.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetricBox("ACTIVE PRO", activeSubs.toString(), BullishNeonMint, Modifier.weight(1f))
                MetricBox("EXPIRED", expiredSubs.toString(), BearishCoralRed, Modifier.weight(1f))
                MetricBox("TOTAL PRO", totalPro.toString(), TargetGold, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = revenueInput,
                    onValueChange = { revenueInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Revenue (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onUpdateRevenue(revenueInput.toDoubleOrNull() ?: 0.0) },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SAVE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search player or @IG") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        editingPlayer = PlayerRecord(
                            id = "",
                            name = "",
                            instagramHandle = "@",
                            membershipStatus = "Pro",
                            paymentStatus = "Paid",
                            startDate = todayDate,
                            expiryDate = todayDate,
                            active = true
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = OuterBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("add_player_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add player", modifier = Modifier.size(18.dp))
                }
            }
        }

        items(filteredPlayers, key = { it.id }) { player ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(player.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        val badgeColor = if (player.membershipStatus == "Pro") TargetGold else CyberCyan
                        Text(
                            player.membershipStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${player.instagramHandle} • ${player.paymentStatus} • ${if (player.active) "Active" else "Expired"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        "${player.startDate} → ${player.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Row {
                    IconButton(onClick = { editingPlayer = player }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit player", tint = CyberCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDeletePlayer(player.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete player", tint = BearishCoralRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    editingPlayer?.let { target ->
        PlayerEditDialog(
            initial = target,
            onDismiss = { editingPlayer = null },
            onSave = { updated ->
                onUpsertPlayer(updated)
                editingPlayer = null
            }
        )
    }
}

@Composable
private fun DailyWinnersAndCupTab(
    ownerData: OwnerConsoleData,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onUpsertWinner: (DailyWinnerRecord) -> Unit,
    onDeleteWinner: (String) -> Unit,
    onUpsertTournamentEntry: (TournamentEntry) -> Unit,
    onDeleteTournamentEntry: (String) -> Unit,
    onUpdateTournamentMeta: (String, String, String, String) -> Unit,
    onToastMessage: (String) -> Unit
) {
    var editingWinner by remember { mutableStateOf<DailyWinnerRecord?>(null) }
    var editingCupEntry by remember { mutableStateOf<TournamentEntry?>(null) }
    var dateField by remember(selectedDate) { mutableStateOf(selectedDate) }

    val dayWinners = remember(ownerData.dailyWinners, selectedDate) {
        ownerData.dailyWinners.filter { it.date == selectedDate }.sortedBy { it.rank }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateField,
                    onValueChange = {
                        dateField = it
                        if (it.length == 10) onSelectDate(it)
                    },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        editingWinner = DailyWinnerRecord(
                            id = "",
                            date = selectedDate,
                            rank = dayWinners.size + 1,
                            playerName = "",
                            instagramHandle = "@",
                            score = 25_000,
                            wins = 7,
                            losses = 2,
                            streak = 4,
                            gameMode = "TRICKY CHARTS"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("add_winner_button")
                ) {
                    Text("+ WINNER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(dayWinners, key = { it.id }) { winner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, TargetGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "#${winner.rank} ${winner.playerName} (+${winner.score})",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        "${winner.wins}W-${winner.losses}L • 🔥${winner.streak} • ${winner.gameMode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (winner.rank > 1) onUpsertWinner(winner.copy(rank = winner.rank - 1))
                    }) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Move rank up", tint = BullishNeonMint, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        onUpsertWinner(winner.copy(rank = winner.rank + 1))
                    }) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Move rank down", tint = TargetGold, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { editingWinner = winner }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit winner", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteWinner(winner.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete winner", tint = BearishCoralRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onToastMessage("Daily records saved for $selectedDate!") },
                colors = ButtonDefaults.buttonColors(containerColor = TargetGold, contentColor = OuterBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE DAILY RESULTS ($selectedDate)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Monthly Chart Sniper Cup Owner Management
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MONTHLY TOURNAMENT CUP", style = MaterialTheme.typography.titleSmall, color = TargetGold)
                OutlinedButton(
                    onClick = {
                        editingCupEntry = TournamentEntry(
                            id = "",
                            rank = ownerData.tournament.entries.size + 1,
                            playerName = "",
                            instagramHandle = "@",
                            score = 100_000,
                            wins = 25,
                            proEligible = true
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ CUP PLAYER", style = MaterialTheme.typography.labelSmall, color = BullishNeonMint)
                }
            }
        }

        items(ownerData.tournament.entries.sortedBy { it.rank }, key = { it.id }) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("#${entry.rank} ${entry.playerName}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text("Score: +${entry.score} • ${entry.wins}W • ${if (entry.proEligible) "Pro Eligible" else "Free"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row {
                    IconButton(onClick = { editingCupEntry = entry }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit cup player", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteTournamentEntry(entry.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete cup player", tint = BearishCoralRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    editingWinner?.let { initial ->
        WinnerEditDialog(
            initial = initial,
            onDismiss = { editingWinner = null },
            onSave = {
                onUpsertWinner(it)
                editingWinner = null
            }
        )
    }

    editingCupEntry?.let { initial ->
        TournamentEntryDialog(
            initial = initial,
            onDismiss = { editingCupEntry = null },
            onSave = {
                onUpsertTournamentEntry(it)
                editingCupEntry = null
            }
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardSurface)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayerEditDialog(
    initial: PlayerRecord,
    onDismiss: () -> Unit,
    onSave: (PlayerRecord) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var ig by remember { mutableStateOf(initial.instagramHandle) }
    var isPro by remember { mutableStateOf(initial.membershipStatus == "Pro") }
    var payment by remember { mutableStateOf(initial.paymentStatus) }
    var startDate by remember { mutableStateOf(initial.startDate) }
    var expiryDate by remember { mutableStateOf(initial.expiryDate) }
    var active by remember { mutableStateOf(initial.active) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardElevated)
                .border(1.dp, CyberCyan, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("PLAYER & SUBSCRIPTION", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Player Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ig, onValueChange = { ig = it }, label = { Text("Instagram @username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Pro Membership", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(checked = isPro, onCheckedChange = { isPro = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Subscription Active", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(checked = active, onCheckedChange = { active = it })
            }
            OutlinedTextField(value = payment, onValueChange = { payment = it }, label = { Text("Payment Status (Paid/Expired/Trial)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Expiry") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("CANCEL") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                initial.copy(
                                    id = initial.id.ifBlank { UUID.randomUUID().toString() },
                                    name = name.trim(),
                                    instagramHandle = ig.trim(),
                                    membershipStatus = if (isPro) "Pro" else "Free",
                                    paymentStatus = payment.trim().ifBlank { "Paid" },
                                    startDate = startDate.trim(),
                                    expiryDate = expiryDate.trim(),
                                    active = active
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                    modifier = Modifier.weight(1f)
                ) { Text("SAVE") }
            }
        }
    }
}

@Composable
private fun WinnerEditDialog(
    initial: DailyWinnerRecord,
    onDismiss: () -> Unit,
    onSave: (DailyWinnerRecord) -> Unit
) {
    var name by remember { mutableStateOf(initial.playerName) }
    var ig by remember { mutableStateOf(initial.instagramHandle) }
    var rank by remember { mutableStateOf(initial.rank.toString()) }
    var score by remember { mutableStateOf(initial.score.toString()) }
    var wins by remember { mutableStateOf(initial.wins.toString()) }
    var losses by remember { mutableStateOf(initial.losses.toString()) }
    var streak by remember { mutableStateOf(initial.streak.toString()) }
    var mode by remember { mutableStateOf(initial.gameMode) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardElevated)
                .border(1.dp, TargetGold, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("DAILY WINNER RECORD", style = MaterialTheme.typography.titleMedium, color = TargetGold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Player Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ig, onValueChange = { ig = it }, label = { Text("Instagram (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rank, onValueChange = { rank = it.filter { c -> c.isDigit() } }, label = { Text("Rank") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = score, onValueChange = { score = it.filter { c -> c.isDigit() } }, label = { Text("Score") }, singleLine = true, modifier = Modifier.weight(1.5f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = wins, onValueChange = { wins = it.filter { c -> c.isDigit() } }, label = { Text("Wins") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = losses, onValueChange = { losses = it.filter { c -> c.isDigit() } }, label = { Text("Losses") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = streak, onValueChange = { streak = it.filter { c -> c.isDigit() } }, label = { Text("Streak") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = mode, onValueChange = { mode = it }, label = { Text("Game Mode") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("CANCEL") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                initial.copy(
                                    id = initial.id.ifBlank { UUID.randomUUID().toString() },
                                    playerName = name.trim(),
                                    instagramHandle = ig.trim(),
                                    rank = (rank.toIntOrNull() ?: 1).coerceAtLeast(1),
                                    score = score.toIntOrNull() ?: 0,
                                    wins = wins.toIntOrNull() ?: 0,
                                    losses = losses.toIntOrNull() ?: 0,
                                    streak = streak.toIntOrNull() ?: 0,
                                    gameMode = mode.trim().ifBlank { "NORMAL ARENA" }
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                    modifier = Modifier.weight(1f)
                ) { Text("SAVE") }
            }
        }
    }
}

@Composable
private fun TournamentEntryDialog(
    initial: TournamentEntry,
    onDismiss: () -> Unit,
    onSave: (TournamentEntry) -> Unit
) {
    var name by remember { mutableStateOf(initial.playerName) }
    var ig by remember { mutableStateOf(initial.instagramHandle) }
    var rank by remember { mutableStateOf(initial.rank.toString()) }
    var score by remember { mutableStateOf(initial.score.toString()) }
    var wins by remember { mutableStateOf(initial.wins.toString()) }
    var proEligible by remember { mutableStateOf(initial.proEligible) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardElevated)
                .border(1.dp, TargetGold, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("SNIPER CUP PLAYER", style = MaterialTheme.typography.titleMedium, color = TargetGold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Player Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ig, onValueChange = { ig = it }, label = { Text("Instagram Handle") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rank, onValueChange = { rank = it.filter { c -> c.isDigit() } }, label = { Text("Rank") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = score, onValueChange = { score = it.filter { c -> c.isDigit() } }, label = { Text("Score") }, singleLine = true, modifier = Modifier.weight(1.4f))
                OutlinedTextField(value = wins, onValueChange = { wins = it.filter { c -> c.isDigit() } }, label = { Text("Wins") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Pro Eligible", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(checked = proEligible, onCheckedChange = { proEligible = it })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("CANCEL") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                initial.copy(
                                    id = initial.id.ifBlank { UUID.randomUUID().toString() },
                                    playerName = name.trim(),
                                    instagramHandle = ig.trim(),
                                    rank = (rank.toIntOrNull() ?: 1).coerceAtLeast(1),
                                    score = score.toIntOrNull() ?: 0,
                                    wins = wins.toIntOrNull() ?: 0,
                                    proEligible = proEligible
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                    modifier = Modifier.weight(1f)
                ) { Text("SAVE") }
            }
        }
    }
}
