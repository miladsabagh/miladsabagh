package ir.zarrin.goldshop.ui.theme

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
import ir.zarrin.goldshop.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

/** Extra brand colours that do not have a Material slot. */
object ZarrinColors {
    val Gold = Color(0xFFC79A29)
    val GoldSoft = Color(0xFFFFEFC6)
    val Success = Color(0xFF2E7D4F)
    val SuccessSoft = Color(0xFFD7F0E1)
    val Danger = Color(0xFFB3261E)
    val DangerSoft = Color(0xFFFFDAD6)
    val Info = Color(0xFF2D5B8A)
    val InfoSoft = Color(0xFFDCE8F7)
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A6410),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDF9E),
    onPrimaryContainer = Color(0xFF2B1D00),
    secondary = Color(0xFF6D5C3F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7E0B8),
    onSecondaryContainer = Color(0xFF251A04),
    tertiary = Color(0xFF3F6653),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC1ECD3),
    onTertiaryContainer = Color(0xFF002114),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBF3),
    onBackground = Color(0xFF1F1B13),
    surface = Color(0xFFFFFBF3),
    onSurface = Color(0xFF1F1B13),
    surfaceVariant = Color(0xFFEEE1CF),
    onSurfaceVariant = Color(0xFF4E4639),
    outline = Color(0xFF807767),
    outlineVariant = Color(0xFFD1C5B4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF7E8),
    surfaceContainer = Color(0xFFFDF1DF),
    surfaceContainerHigh = Color(0xFFF7EBD9),
    surfaceContainerHighest = Color(0xFFF1E5D4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFEFC060),
    onPrimary = Color(0xFF493100),
    primaryContainer = Color(0xFF684900),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = Color(0xFFDAC49E),
    onSecondary = Color(0xFF3C2F15),
    secondaryContainer = Color(0xFF54452A),
    onSecondaryContainer = Color(0xFFF7E0B8),
    tertiary = Color(0xFFA5D0B7),
    onTertiary = Color(0xFF0B3826),
    tertiaryContainer = Color(0xFF264E3C),
    onTertiaryContainer = Color(0xFFC1ECD3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF17130B),
    onBackground = Color(0xFFEBE1D4),
    surface = Color(0xFF17130B),
    onSurface = Color(0xFFEBE1D4),
    surfaceVariant = Color(0xFF4E4639),
    onSurfaceVariant = Color(0xFFD1C5B4),
    outline = Color(0xFF9A9080),
    outlineVariant = Color(0xFF4E4639),
    surfaceContainerLowest = Color(0xFF110E07),
    surfaceContainerLow = Color(0xFF1F1B13),
    surfaceContainer = Color(0xFF231F17),
    surfaceContainerHigh = Color(0xFF2E2921),
    surfaceContainerHighest = Color(0xFF39342B)
)

private fun persianTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Vazirmatn),
        displayMedium = base.displayMedium.copy(fontFamily = Vazirmatn),
        displaySmall = base.displaySmall.copy(fontFamily = Vazirmatn),
        headlineLarge = base.headlineLarge.copy(fontFamily = Vazirmatn),
        headlineMedium = base.headlineMedium.copy(fontFamily = Vazirmatn),
        headlineSmall = base.headlineSmall.copy(fontFamily = Vazirmatn),
        titleLarge = base.titleLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = Vazirmatn),
        bodyMedium = base.bodyMedium.copy(fontFamily = Vazirmatn),
        bodySmall = base.bodySmall.copy(fontFamily = Vazirmatn),
        labelLarge = base.labelLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = Vazirmatn),
        labelSmall = base.labelSmall.copy(fontFamily = Vazirmatn)
    )
}

@Composable
fun ZarrinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = persianTypography(),
            content = content
        )
    }
}
