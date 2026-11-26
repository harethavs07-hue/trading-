package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.MascotState
import com.example.model.PlayerStats
import com.example.model.RoundOutcomeSummary
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

@Composable
fun PostRoundResultSheet(
    outcome: RoundOutcomeSummary,
    stats: PlayerStats,
    onNextChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (outcome.isWin) BullishNeonMint else BearishCoralRed
    val challenge = outcome.challenge
    val patternTitle = challenge.trickySubtype?.label ?: challenge.archetype.displayName
    val mascotState = when {
        outcome.isWin && challenge.isTrickyMode -> MascotState.TRAP_SURVIVED
        outcome.isWin -> MascotState.WIN
        else -> MascotState.LOSS
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CardElevated, CardSurface)
                )
            )
            .border(1.5.dp, accent, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: Result Title + P&L Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (outcome.isWin) "TARGET SMASHED!" else "STOPPED OUT!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = accent
                )
                Text(
                    text = "${challenge.asset} • ${challenge.timeframe} • $patternTitle",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formatSignedDelta(outcome.deltaAmount, outcome.currencyMode),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Key Telemetry Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(OuterBackground.copy(alpha = 0.55f))
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TelemetryMini("YOUR PICK", outcome.selectedDirection.label, CyberCyan)
            TelemetryMini("OUTCOME", outcome.actualOutcome.label, accent)
            TelemetryMini("CAPITAL", formatCurrency(outcome.updatedCapital, outcome.currencyMode), TargetGold)
            TelemetryMini("WIN RATE", "${stats.winPercentage}%", BullishNeonMint)
        }

        // COACH BREAKDOWN Box with Anime Mascot reacting beside explanation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OuterBackground.copy(alpha = 0.72f))
                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimeTraderMascotAvatar(state = mascotState, size = 54.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "COACH BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = challenge.coachExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }

        Button(
            onClick = onNextChart,
            colors = ButtonDefaults.buttonColors(
                containerColor = BullishNeonMint,
                contentColor = OuterBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("next_chart_button")
        ) {
            Text(
                text = "NEXT CHART →",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TelemetryMini(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
