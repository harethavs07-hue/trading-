package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.export.ContentExportManager
import com.example.model.OwnerConsoleData
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
import kotlin.math.roundToInt

@Composable
fun InstagramStudioTab(
    ownerData: OwnerConsoleData,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onToastMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val dates = remember(ownerData.dailyWinners, selectedDate) {
        (ownerData.dailyWinners.map { it.date } + selectedDate).distinct().sortedDescending()
    }
    val dayWinners = remember(ownerData.dailyWinners, selectedDate) {
        ownerData.dailyWinners.filter { it.date == selectedDate }.sortedBy { it.rank }
    }

    var previewType by remember { mutableIntStateOf(0) } // 0 = Post 1:1, 1 = Story 9:16
    var captionText by remember(selectedDate, dayWinners) {
        mutableStateOf(ContentExportManager.buildDefaultInstagramCaption(selectedDate, dayWinners))
    }

    val postBitmap = remember(selectedDate, dayWinners) {
        ContentExportManager.renderDailyLeaderboardPostBitmap(selectedDate, dayWinners)
    }
    val storyBitmap = remember(selectedDate, dayWinners) {
        ContentExportManager.renderInstagramStoryBitmap(selectedDate, dayWinners)
    }

    LaunchedEffect(selectedDate, dayWinners) {
        captionText = ContentExportManager.buildDefaultInstagramCaption(selectedDate, dayWinners)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "INSTAGRAM CONTENT STUDIO",
                style = MaterialTheme.typography.titleMedium,
                color = TargetGold
            )
            Text(
                text = "Generated strictly from saved official records. Download & upload manually to Instagram.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(dates) { date ->
                    val active = date == selectedDate
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) TargetGold.copy(alpha = 0.2f) else CardSurface)
                            .border(1.dp, if (active) TargetGold else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onSelectDate(date) }
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
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { previewType = 0 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "POST (1:1)",
                        color = if (previewType == 0) BullishNeonMint else TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                OutlinedButton(
                    onClick = { previewType = 1 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "STORY (9:16)",
                        color = if (previewType == 1) TargetGold else TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (previewType == 0) {
                    Image(
                        bitmap = postBitmap.asImageBitmap(),
                        contentDescription = "Daily Leaderboard Instagram Post preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Image(
                        bitmap = storyBitmap.asImageBitmap(),
                        contentDescription = "Instagram Story 9:16 preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val res = ContentExportManager.saveBitmapToGalleryOrShare(
                            context = context,
                            bitmap = postBitmap,
                            fileNamePrefix = "ChartArena_Post_$selectedDate"
                        )
                        onToastMessage(res.getOrElse { "Export failed" })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullishNeonMint,
                        contentColor = OuterBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("export_post_png_button")
                ) {
                    Text("SAVE POST PNG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val res = ContentExportManager.saveBitmapToGalleryOrShare(
                            context = context,
                            bitmap = storyBitmap,
                            fileNamePrefix = "ChartArena_Story_$selectedDate"
                        )
                        onToastMessage(res.getOrElse { "Export failed" })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TargetGold,
                        contentColor = OuterBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("export_story_png_button")
                ) {
                    Text("SAVE STORY PNG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val targetBmp = if (previewType == 0) postBitmap else storyBitmap
                        val prefix = if (previewType == 0) "ChartArena_Post_$selectedDate" else "ChartArena_Story_$selectedDate"
                        ContentExportManager.shareBitmap(context, targetBmp, prefix)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SHARE IMAGE", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                }

                OutlinedButton(
                    onClick = {
                        val text = ContentExportManager.buildLeaderboardPlainText(selectedDate, dayWinners)
                        copyToClipboard(context, "Leaderboard", text)
                        onToastMessage("Leaderboard text copied!")
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("COPY BOARD TEXT", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EDITABLE INSTAGRAM CAPTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    minLines = 5
                )
                Button(
                    onClick = {
                        copyToClipboard(context, "IG Caption", captionText)
                        onToastMessage("Instagram caption copied to clipboard!")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = OuterBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("copy_caption_button")
                ) {
                    Text("COPY CAPTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ArchiveAndBackupTab(
    ownerData: OwnerConsoleData,
    uniqueChartsCount: Int,
    onOpenDateInWinners: (String) -> Unit,
    onOpenDateInStudio: (String) -> Unit,
    onUpdateSimilarityThreshold: (Float) -> Unit,
    onClearChartHistory: () -> Unit,
    onExportCsv: () -> String,
    onExportJsonBackup: () -> String,
    onImportJsonBackup: (String) -> Boolean,
    onToastMessage: (String) -> Unit
) {
    val context = LocalContext.current

    val csvCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(onExportCsv().toByteArray())
                }
                onToastMessage("CSV archive exported successfully!")
            }.onFailure {
                onToastMessage("CSV export failed.")
            }
        }
    }

    val jsonCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(onExportJsonBackup().toByteArray())
                }
                onToastMessage("JSON backup exported!")
            }.onFailure {
                onToastMessage("JSON export failed.")
            }
        }
    }

    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (text != null) {
                    onImportJsonBackup(text)
                } else {
                    onToastMessage("Could not read selected file.")
                }
            }.onFailure {
                onToastMessage("Import failed.")
            }
        }
    }

    val datesGrouped = remember(ownerData.dailyWinners) {
        ownerData.dailyWinners
            .groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardElevated)
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("EXPORTS & BACKUPS", style = MaterialTheme.typography.titleSmall, color = CyberCyan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { csvCreateLauncher.launch("chart_arena_daily_archive.csv") },
                        colors = ButtonDefaults.buttonColors(containerColor = BullishNeonMint, contentColor = OuterBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button")
                    ) {
                        Text("EXPORT CSV", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            ContentExportManager.shareTextFile(
                                context,
                                onExportCsv(),
                                "chart_arena_daily_archive.csv",
                                "text/csv"
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SHARE CSV", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { jsonCreateLauncher.launch("chart_arena_backup.json") },
                        colors = ButtonDefaults.buttonColors(containerColor = TargetGold, contentColor = OuterBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_json_button")
                    ) {
                        Text("EXPORT JSON", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { jsonImportLauncher.launch(arrayOf("application/json", "text/*")) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_json_button")
                    ) {
                        Text("IMPORT JSON", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ANTI-REPETITION ENGINE",
                    style = MaterialTheme.typography.titleSmall,
                    color = BullishNeonMint
                )
                Text(
                    text = "Unique Fingerprints Stored: $uniqueChartsCount / 10,000",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = "Similarity Rejection Threshold: ${(ownerData.similarityThreshold * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TargetGold
                )
                Slider(
                    value = ownerData.similarityThreshold,
                    onValueChange = onUpdateSimilarityThreshold,
                    valueRange = 0.65f..0.95f,
                    colors = SliderDefaults.colors(
                        thumbColor = BullishNeonMint,
                        activeTrackColor = BullishNeonMint
                    )
                )
                OutlinedButton(
                    onClick = onClearChartHistory,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLEAR CHART FINGERPRINT CACHE", style = MaterialTheme.typography.labelSmall, color = BearishCoralRed)
                }
            }
        }

        item {
            Text(
                text = "HISTORICAL DAILY ARCHIVE",
                style = MaterialTheme.typography.titleSmall,
                color = TargetGold
            )
        }

        if (datesGrouped.isEmpty()) {
            item {
                Text(
                    text = "No daily records saved yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            items(datesGrouped, key = { it.first }) { (date, records) ->
                val topWinner = records.minByOrNull { it.rank }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(date, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${records.size} winners", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                    }
                    if (topWinner != null) {
                        Text(
                            text = "🥇 #1 ${topWinner.playerName} (+${topWinner.score})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TargetGold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onOpenDateInWinners(date) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("EDIT RECORDS", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                        }
                        Button(
                            onClick = { onOpenDateInStudio(date) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = OuterBackground),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OPEN IG POST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
