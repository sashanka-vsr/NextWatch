package com.nextwatch.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    primaryContainer = SubtleRed,
    onPrimaryContainer = Color.White,
    secondary = NetflixRed,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceHighlight,
    onSecondaryContainer = TextPrimary,
    tertiary = DarkRedAccent,
    onTertiary = Color.White,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = PureBlack,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = DarkSurfaceHighlight,
    surfaceContainerHighest = DarkBorder,
    outline = DarkBorder,
    outlineVariant = DarkSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    secondary = DarkRedAccent,
    onSecondary = Color.White,
    background = Color(0xFFF8F8F8),
    onBackground = Color(0xFF141414),
    surface = Color.White,
    onSurface = Color(0xFF141414),
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Color(0xFF666666),
)

@Composable
fun NextWatchTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AmoledDarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}