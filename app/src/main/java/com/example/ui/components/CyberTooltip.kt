package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTooltipBox(
    tooltipText: String,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState(isPersistent = false)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = 6.dp
        ),
        tooltip = {
            PlainTooltip(
                containerColor = CardElevated,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Text(
                    text = tooltipText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        },
        state = tooltipState,
        focusable = false,
        modifier = modifier,
        content = content
    )
}
