package ir.zarin.faktor.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val GoldPrimary = Color(0xFF7A5B08)
val GoldAccent = Color(0xFFD4AF37)
val GoldSoft = Color(0xFFFFE08A)
val Cream = Color(0xFFFFFBF3)
val PaidGreen = Color(0xFF2E7D32)
val UnpaidRed = Color(0xFFB3261E)
val PartialAmber = Color(0xFFB26A00)

val LightColors = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldSoft,
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF6B5D3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E1BB),
    onSecondaryContainer = Color(0xFF241A04),
    tertiary = Color(0xFF44664C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC6ECCC),
    onTertiaryContainer = Color(0xFF00210E),
    background = Cream,
    onBackground = Color(0xFF1E1B16),
    surface = Cream,
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEDE1CF),
    onSurfaceVariant = Color(0xFF4D4639),
    surfaceContainer = Color(0xFFFAF2E3),
    surfaceContainerHigh = Color(0xFFF5EBD9),
    outline = Color(0xFF7F7667),
    outlineVariant = Color(0xFFD0C5B4),
    error = UnpaidRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFEFC148),
    onPrimary = Color(0xFF402D00),
    primaryContainer = Color(0xFF5C4200),
    onPrimaryContainer = GoldSoft,
    secondary = Color(0xFFD8C5A1),
    onSecondary = Color(0xFF3A2F16),
    secondaryContainer = Color(0xFF52452A),
    onSecondaryContainer = Color(0xFFF5E1BB),
    tertiary = Color(0xFFAAD0B1),
    onTertiary = Color(0xFF143722),
    tertiaryContainer = Color(0xFF2C4E36),
    onTertiaryContainer = Color(0xFFC6ECCC),
    background = Color(0xFF16130D),
    onBackground = Color(0xFFE9E1D5),
    surface = Color(0xFF16130D),
    onSurface = Color(0xFFE9E1D5),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFD0C5B4),
    surfaceContainer = Color(0xFF231F17),
    surfaceContainerHigh = Color(0xFF2E2921),
    outline = Color(0xFF999080),
    outlineVariant = Color(0xFF4D4639),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
