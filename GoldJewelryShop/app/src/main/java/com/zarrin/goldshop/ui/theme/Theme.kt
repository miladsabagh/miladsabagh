package com.zarrin.goldshop.ui.theme

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

val GoldPrimary = Color(0xFFC9A227)
val GoldDeep = Color(0xFF8B6914)
val Ink = Color(0xFF1A1410)
val Parchment = Color(0xFFF7F1E6)
val Sand = Color(0xFFE8DCC8)
val Ember = Color(0xFFA65A2E)
val SoftRose = Color(0xFFD9B8A8)
val Forest = Color(0xFF2F5D50)

private val LightColors = lightColorScheme(
    primary = GoldDeep,
    onPrimary = Color.White,
    secondary = Ember,
    onSecondary = Color.White,
    tertiary = Forest,
    background = Parchment,
    onBackground = Ink,
    surface = Color(0xFFFFFBF4),
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Color(0xFF5C4E3D),
    outline = Color(0xFFB9A789)
)

private val DarkColors = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Ink,
    secondary = SoftRose,
    onSecondary = Ink,
    tertiary = Color(0xFF7DB8A5),
    background = Color(0xFF14100C),
    onBackground = Color(0xFFF3EADF),
    surface = Color(0xFF1E1812),
    onSurface = Color(0xFFF3EADF),
    surfaceVariant = Color(0xFF2C241B),
    onSurfaceVariant = Color(0xFFD4C4AE),
    outline = Color(0xFF6B5E4E)
)

private val AppTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
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
