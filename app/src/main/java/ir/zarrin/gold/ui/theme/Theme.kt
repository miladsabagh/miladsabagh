package ir.zarrin.gold.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFF9A7B0A)
private val GoldContainer = Color(0xFFF6E8B8)
private val OnGoldContainer = Color(0xFF3D2F00)
private val Bronze = Color(0xFF6D5C3F)
private val BronzeContainer = Color(0xFFF7E0BB)
private val Cream = Color(0xFFFFF9EE)
private val SurfaceCream = Color(0xFFFFFDF6)

private val LightColors = lightColorScheme(
    primary = Gold,
    onPrimary = Color.White,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = Bronze,
    onSecondary = Color.White,
    secondaryContainer = BronzeContainer,
    onSecondaryContainer = Color(0xFF261904),
    background = SurfaceCream,
    onBackground = Color(0xFF1E1B13),
    surface = SurfaceCream,
    onSurface = Color(0xFF1E1B13),
    surfaceVariant = Cream,
    onSurfaceVariant = Color(0xFF4B4639),
    outline = Color(0xFF7C7767),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD9C36A),
    onPrimary = Color(0xFF383000),
    primaryContainer = Color(0xFF524700),
    onPrimaryContainer = Color(0xFFF6E8B8),
    secondary = Color(0xFFDAC3A1),
    onSecondary = Color(0xFF3C2E16),
    secondaryContainer = Color(0xFF54442A),
    onSecondaryContainer = Color(0xFFF7E0BB),
    background = Color(0xFF15130C),
    onBackground = Color(0xFFE8E2D4),
    surface = Color(0xFF15130C),
    onSurface = Color(0xFFE8E2D4),
)

@Composable
fun ZarrinGoldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZarrinTypography,
        content = content,
    )
}
