package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
fun ProUnlockModal(
    isAlreadyUnlocked: Boolean,
    onTryDemoUnlock: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardElevated, CardSurface)
                    )
                )
                .border(1.5.dp, TargetGold, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = "Pro Arena",
                        tint = TargetGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "CHART ARENA PRO",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TargetGold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_pro_modal")) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Pro modal",
                        tint = TextSecondary
                    )
                }
            }

            Text(
                text = "Master institutional market traps and qualify for the Monthly Chart Sniper Cup.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OuterBackground.copy(alpha = 0.65f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                val benefits = listOf(
                    "Access to all Tricky Charts (Pro)",
                    "Stop-Loss Hunts, Liquidity Sweeps & Bull/Bear Traps",
                    "NFP News Whipsaw institutional scenarios",
                    "Monthly Chart Sniper Cup tournament entry",
                    "Advanced analytics & performance statistics"
                )
                benefits.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = BullishNeonMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Pricing Cards (India & Global)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TargetGold.copy(alpha = 0.12f))
                        .border(1.dp, TargetGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("INDIA", style = MaterialTheme.typography.labelSmall, color = TargetGold)
                        Text("₹199/month", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("GLOBAL", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                        Text("$14.99/month", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
            }

            Text(
                text = "Note: Subscription activation is managed directly by the platform owner in the Owner Studio. No real payment is processed in this prototype.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (!isAlreadyUnlocked) {
                Button(
                    onClick = onTryDemoUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullishNeonMint,
                        contentColor = OuterBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("try_demo_unlock_button")
                ) {
                    Text(
                        text = "TRY DEMO UNLOCK (INSTANT PRO)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "PRO DEMO ALREADY ACTIVE ✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = BullishNeonMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
