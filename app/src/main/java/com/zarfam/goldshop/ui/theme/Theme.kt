package com.zarfam.goldshop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.zarfam.goldshop.R

val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8D6E1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8A3),
    onPrimaryContainer = Color(0xFF2B2000),
    secondary = Color(0xFF6B5D3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E1BB),
    onSecondaryContainer = Color(0xFF241A04),
    tertiary = Color(0xFF4A6547),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF1F1B13),
    surface = Color(0xFFFFFDF6),
    onSurface = Color(0xFF1F1B13),
    surfaceVariant = Color(0xFFEBE1CF),
    onSurfaceVariant = Color(0xFF4C4639),
    outline = Color(0xFF7E7667),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE8C458),
    onPrimary = Color(0xFF3F2E00),
    primaryContainer = Color(0xFF5B4300),
    onPrimaryContainer = Color(0xFFFFE8A3),
    secondary = Color(0xFFD8C49E),
    onSecondary = Color(0xFF3A2F15),
    secondaryContainer = Color(0xFF52452A),
    onSecondaryContainer = Color(0xFFF5E1BB),
    tertiary = Color(0xFFB0CFA9),
    background = Color(0xFF17130B),
    onBackground = Color(0xFFEBE1D4),
    surface = Color(0xFF1F1A11),
    onSurface = Color(0xFFEBE1D4),
    surfaceVariant = Color(0xFF4C4639),
    onSurfaceVariant = Color(0xFFCFC5B4),
    outline = Color(0xFF989080),
)

private fun typographyWith(font: FontFamily): Typography {
    val t = Typography()
    return Typography(
        displayLarge = t.displayLarge.copy(fontFamily = font),
        displayMedium = t.displayMedium.copy(fontFamily = font),
        displaySmall = t.displaySmall.copy(fontFamily = font),
        headlineLarge = t.headlineLarge.copy(fontFamily = font),
        headlineMedium = t.headlineMedium.copy(fontFamily = font),
        headlineSmall = t.headlineSmall.copy(fontFamily = font),
        titleLarge = t.titleLarge.copy(fontFamily = font, fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontFamily = font, fontWeight = FontWeight.Medium),
        titleSmall = t.titleSmall.copy(fontFamily = font, fontWeight = FontWeight.Medium),
        bodyLarge = t.bodyLarge.copy(fontFamily = font),
        bodyMedium = t.bodyMedium.copy(fontFamily = font),
        bodySmall = t.bodySmall.copy(fontFamily = font),
        labelLarge = t.labelLarge.copy(fontFamily = font, fontWeight = FontWeight.Medium),
        labelMedium = t.labelMedium.copy(fontFamily = font),
        labelSmall = t.labelSmall.copy(fontFamily = font),
    )
}

@Composable
fun ZarFamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = typographyWith(Vazir),
        content = content,
    )
}
