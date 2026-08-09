package com.goldjewelry.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GoldPrimary = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF5E6A3)
val GoldDark = Color(0xFFB8960C)
val NavyDark = Color(0xFF1A1A2E)
val NavyMedium = Color(0xFF16213E)
val NavyLight = Color(0xFF0F3460)
val Cream = Color(0xFFFFF8E7)
val SurfaceDark = Color(0xFF1E1E30)
val ErrorRed = Color(0xFFCF6679)

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyDark,
    primaryContainer = GoldDark,
    onPrimaryContainer = Cream,
    secondary = GoldLight,
    onSecondary = NavyDark,
    background = NavyDark,
    onBackground = Cream,
    surface = SurfaceDark,
    onSurface = Cream,
    surfaceVariant = NavyMedium,
    onSurfaceVariant = GoldLight,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = NavyDark,
    secondary = NavyLight,
    onSecondary = Color.White,
    background = Cream,
    onBackground = NavyDark,
    surface = Color.White,
    onSurface = NavyDark,
    surfaceVariant = Color(0xFFF0E6D3),
    onSurfaceVariant = NavyMedium,
    error = ErrorRed
)

@Composable
fun GoldJewelryTheme(
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
