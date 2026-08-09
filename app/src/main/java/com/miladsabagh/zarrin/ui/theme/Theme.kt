package com.miladsabagh.zarrin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFC29A2E)
val GoldLight = Color(0xFFF4E3B2)
val GoldDark = Color(0xFF8C6D12)
val DeepBrown = Color(0xFF241A05)
val Cream = Color(0xFFFDF9EF)
val Charcoal = Color(0xFF1B1710)
val SurfaceDark = Color(0xFF262015)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = DeepBrown,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8ECCB),
    onSecondaryContainer = DeepBrown,
    background = Cream,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = Color(0xFFF3ECDD),
    onSurfaceVariant = Color(0xFF5C5238),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = DeepBrown,
    primaryContainer = Color(0xFF5A4712),
    onPrimaryContainer = GoldLight,
    secondary = Color(0xFFD9B759),
    onSecondary = DeepBrown,
    secondaryContainer = Color(0xFF4A3B10),
    onSecondaryContainer = GoldLight,
    background = Charcoal,
    onBackground = Color(0xFFF1E9D6),
    surface = SurfaceDark,
    onSurface = Color(0xFFF1E9D6),
    surfaceVariant = Color(0xFF3A3222),
    onSurfaceVariant = Color(0xFFCFC2A3)
)

@Composable
fun ZarrinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
