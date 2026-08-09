package ir.goldshop.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldShopColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldContainer,
    onPrimaryContainer = DeepMaroon,
    secondary = DeepMaroon,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = GoldContainer,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun GoldShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GoldShopColorScheme,
        typography = GoldShopTypography,
        content = content
    )
}
