package com.miladsabagh.goldinvoice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = GoldContainer,
    onPrimaryContainer = Charcoal,
    secondary = GoldPrimaryDark,
    onSecondary = Color.White,
    background = IvoryBackground,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = SurfaceWarm,
    onSurfaceVariant = Charcoal,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Charcoal,
    primaryContainer = GoldPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = GoldContainer,
    onSecondary = Charcoal,
    background = Color(0xFF1B1712),
    onBackground = Color(0xFFF5EEDD),
    surface = Color(0xFF251F17),
    onSurface = Color(0xFFF5EEDD),
    surfaceVariant = Color(0xFF332B1E),
    onSurfaceVariant = Color(0xFFEADFC5),
    error = Color(0xFFEF9A9A)
)

@Composable
fun GoldInvoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoldInvoiceTypography,
        content = content
    )
}
