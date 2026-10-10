package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ConstructionYellowLight,
    onPrimary = NavyDark,
    primaryContainer = ConstructionYellowDark,
    onPrimaryContainer = Color.White,
    secondary = ConstructionYellow,
    onSecondary = NavyDark,
    tertiary = InfoBlue,
    background = NavyDark,
    surface = NavySurface,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = NavyLight,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = NavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ConstructionYellowDark,
    onPrimary = Color.White,
    primaryContainer = ConstructionYellowContainer,
    onPrimaryContainer = OnConstructionYellowContainer,
    secondary = NavyDark,
    onSecondary = Color.White,
    tertiary = InfoBlue,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateCard,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
