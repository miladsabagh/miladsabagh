package com.zarnegar.gold.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val Gold40 = Color(0xFF8A6300)
val Gold80 = Color(0xFFF3C623)
val GoldContainer = Color(0xFFFFEEB2)
val DeepBrown = Color(0xFF3E2C0C)
val Emerald = Color(0xFF0F7B5F)
val Ruby = Color(0xFFB3261E)
val Sand = Color(0xFFFFFBF2)

private val LightColors = lightColorScheme(
    primary = Gold40,
    onPrimary = Color.White,
    primaryContainer = GoldContainer,
    onPrimaryContainer = DeepBrown,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F2E3),
    onSecondaryContainer = Color(0xFF00382A),
    tertiary = Color(0xFF7A5900),
    background = Sand,
    onBackground = Color(0xFF1F1B13),
    surface = Color.White,
    onSurface = Color(0xFF1F1B13),
    surfaceVariant = Color(0xFFF3EAD6),
    onSurfaceVariant = Color(0xFF4E4639),
    outline = Color(0xFF807561),
    error = Ruby,
)

private val DarkColors = darkColorScheme(
    primary = Gold80,
    onPrimary = Color(0xFF3F2E00),
    primaryContainer = Color(0xFF5B4300),
    onPrimaryContainer = Color(0xFFFFE08B),
    secondary = Color(0xFF6FD9B7),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF005140),
    onSecondaryContainer = Color(0xFFC7F2E3),
    background = Color(0xFF17140D),
    onBackground = Color(0xFFEAE1D2),
    surface = Color(0xFF201C14),
    onSurface = Color(0xFFEAE1D2),
    surfaceVariant = Color(0xFF4E4639),
    onSurfaceVariant = Color(0xFFD2C5B0),
    outline = Color(0xFF9A8F7B),
    error = Color(0xFFFFB4AB),
)

@Composable
fun ZarnegarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // کل برنامه راست‌چین است.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = ZarnegarTypography,
            content = content,
        )
    }
}
