package com.goldgallery.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import com.goldgallery.app.R

val Gold = Color(0xFFC9A227)
val GoldLight = Color(0xFFE8C766)
val GoldPale = Color(0xFFF5E199)
val GoldDark = Color(0xFF8C6D1F)
val BronzeDeep = Color(0xFF2E2303)
val Cream = Color(0xFFFFFBF0)
val Sand = Color(0xFFF7EFD8)

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldPale,
    onPrimaryContainer = BronzeDeep,
    secondary = Gold,
    onSecondary = BronzeDeep,
    secondaryContainer = Color(0xFFF3E5BC),
    onSecondaryContainer = BronzeDeep,
    tertiary = Color(0xFF7D5A24),
    background = Cream,
    onBackground = BronzeDeep,
    surface = Color.White,
    onSurface = BronzeDeep,
    surfaceVariant = Sand,
    onSurfaceVariant = Color(0xFF6B5B2E),
    outline = Color(0xFFC7B68A),
)

private val DarkColors = darkColorScheme(
    primary = GoldLight,
    onPrimary = Color(0xFF3B2E05),
    primaryContainer = Color(0xFF5A470F),
    onPrimaryContainer = GoldPale,
    secondary = Gold,
    onSecondary = Color(0xFF3B2E05),
    background = Color(0xFF171208),
    onBackground = Color(0xFFEFE6CE),
    surface = Color(0xFF211A0B),
    onSurface = Color(0xFFEFE6CE),
    surfaceVariant = Color(0xFF33290F),
    onSurfaceVariant = Color(0xFFD8C89A),
    outline = Color(0xFF7A6A3E),
)

private val baseTypography = Typography()

private val AppTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = Vazirmatn),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = Vazirmatn),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = Vazirmatn),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = Vazirmatn),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = Vazirmatn),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = Vazirmatn),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = Vazirmatn),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = Vazirmatn),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = Vazirmatn),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = Vazirmatn),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = Vazirmatn),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = Vazirmatn),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = Vazirmatn),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = Vazirmatn),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = Vazirmatn),
)

@Composable
fun GoldGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
