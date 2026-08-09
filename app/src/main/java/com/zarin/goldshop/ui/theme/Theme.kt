package com.zarin.goldshop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFC79A2E)
val GoldLight = Color(0xFFF2C14E)
val GoldDark = Color(0xFF8A6D00)
val Charcoal = Color(0xFF0E1116)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7ECC6),
    onPrimaryContainer = Color(0xFF2A2100),
    secondary = Color(0xFF6D5E2F),
    background = Color(0xFFFBF9F3),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1ECDD),
)

private val DarkColors = darkColorScheme(
    primary = GoldLight,
    onPrimary = Charcoal,
    primaryContainer = Color(0xFF4A3B00),
    onPrimaryContainer = Color(0xFFFCE7A8),
    secondary = Color(0xFFD8C48A),
    background = Charcoal,
    surface = Color(0xFF171B22),
    surfaceVariant = Color(0xFF2A2F38),
)

@Composable
fun GoldShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
