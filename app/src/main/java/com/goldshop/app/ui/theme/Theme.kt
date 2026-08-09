package com.goldshop.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Gold = Color(0xFFC9A227)
val GoldSoft = Color(0xFFE8D48B)
val GoldMuted = Color(0xFF8A7340)
val Ink = Color(0xFF1A1410)
val InkSoft = Color(0xFF2C241C)
val Cream = Color(0xFFF7F1E6)
val CreamDeep = Color(0xFFEDE3D0)
val SurfaceCard = Color(0xFFFFFBF5)
val Danger = Color(0xFFB42318)
val Success = Color(0xFF1F7A4D)

private val LightColors = lightColorScheme(
    primary = Gold,
    onPrimary = Ink,
    primaryContainer = GoldSoft,
    onPrimaryContainer = Ink,
    secondary = InkSoft,
    onSecondary = Cream,
    background = Cream,
    onBackground = Ink,
    surface = SurfaceCard,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFD0C4A8),
    error = Danger,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = GoldSoft,
    onPrimary = Ink,
    primaryContainer = GoldMuted,
    onPrimaryContainer = Cream,
    secondary = Gold,
    onSecondary = Ink,
    background = Ink,
    onBackground = Cream,
    surface = InkSoft,
    onSurface = Cream,
    surfaceVariant = Color(0xFF3A3026),
    onSurfaceVariant = GoldSoft,
    outline = Color(0xFF5A4A32),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val AppTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

@Composable
fun GoldShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
