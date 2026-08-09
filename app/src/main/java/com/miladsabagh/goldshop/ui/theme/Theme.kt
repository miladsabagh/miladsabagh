package com.miladsabagh.goldshop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Charcoal,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldDark,
    secondary = GoldDark,
    onSecondary = Color.White,
    background = CreamBackground,
    surface = Color.White,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = GoldLight,
    onPrimary = Charcoal,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldPrimary,
    background = Charcoal,
    surface = Color(0xFF242424),
    error = Color(0xFFFF8A80)
)

@Composable
fun GoldShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GoldShopTypography,
        content = content
    )
}
