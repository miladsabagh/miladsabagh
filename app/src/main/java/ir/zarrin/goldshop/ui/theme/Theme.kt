package ir.zarrin.goldshop.ui.theme

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
import ir.zarrin.goldshop.R

val Gold = Color(0xFFB8860B)
val GoldDark = Color(0xFF8A6A17)
val GoldLight = Color(0xFFE7C665)
val GoldSoft = Color(0xFFFFF0C9)
val Cream = Color(0xFFFFFBF2)
val InkBrown = Color(0xFF3A2E12)
val SuccessGreen = Color(0xFF2E7D5B)
val WarningAmber = Color(0xFFB26B00)
val DangerRed = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = GoldSoft,
    onPrimaryContainer = InkBrown,
    secondary = Color(0xFF6D5B3E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E6CE),
    onSecondaryContainer = InkBrown,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = Cream,
    onBackground = InkBrown,
    surface = Color.White,
    onSurface = InkBrown,
    surfaceVariant = Color(0xFFF5EDDD),
    onSurfaceVariant = Color(0xFF5B4E33),
    outline = Color(0xFFCBBE9E),
    outlineVariant = Color(0xFFE6DAC0),
    error = DangerRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = GoldLight,
    onPrimary = Color(0xFF3A2C00),
    primaryContainer = Color(0xFF54430F),
    onPrimaryContainer = GoldSoft,
    secondary = Color(0xFFD8C6A2),
    onSecondary = Color(0xFF3A2F17),
    secondaryContainer = Color(0xFF4C412A),
    onSecondaryContainer = Color(0xFFF3E6CE),
    tertiary = Color(0xFF7BD3AB),
    onTertiary = Color(0xFF003824),
    background = Color(0xFF17130B),
    onBackground = Color(0xFFEDE3D0),
    surface = Color(0xFF201B12),
    onSurface = Color(0xFFEDE3D0),
    surfaceVariant = Color(0xFF3A3323),
    onSurfaceVariant = Color(0xFFD5C7AB),
    outline = Color(0xFF7E7357),
    outlineVariant = Color(0xFF4A422F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val BaseTypography = Typography()

private val ZarrinTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = Vazirmatn),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = Vazirmatn),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = Vazirmatn),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = Vazirmatn),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = Vazirmatn),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = Vazirmatn),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = Vazirmatn),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = Vazirmatn),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = Vazirmatn),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = Vazirmatn),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = Vazirmatn)
)

@Composable
fun ZarrinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZarrinTypography,
        content = content
    )
}
