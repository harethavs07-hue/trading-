package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CyberpunkColorScheme = darkColorScheme(
    primary = BullishNeonMint,
    onPrimary = OuterBackground,
    primaryContainer = CardElevated,
    onPrimaryContainer = BullishNeonMint,
    secondary = CyberCyan,
    onSecondary = OuterBackground,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = TargetGold,
    onTertiary = OuterBackground,
    background = OuterBackground,
    onBackground = TextPrimary,
    surface = AppShellBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    error = BearishCoralRed,
    onError = TextPrimary,
    outline = BorderSubtle
)

private val ChartArenaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun ChartArenaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        typography = ChartArenaTypography,
        shapes = ChartArenaShapes,
        content = content
    )
}
