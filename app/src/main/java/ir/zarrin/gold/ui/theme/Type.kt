package ir.zarrin.gold.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.zarrin.gold.R

val VazirmatnFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val Default = Typography()

val ZarrinTypography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = VazirmatnFamily),
    displayMedium = Default.displayMedium.copy(fontFamily = VazirmatnFamily),
    displaySmall = Default.displaySmall.copy(fontFamily = VazirmatnFamily),
    headlineLarge = Default.headlineLarge.copy(fontFamily = VazirmatnFamily),
    headlineMedium = Default.headlineMedium.copy(fontFamily = VazirmatnFamily),
    headlineSmall = Default.headlineSmall.copy(fontFamily = VazirmatnFamily),
    titleLarge = Default.titleLarge.copy(fontFamily = VazirmatnFamily),
    titleMedium = Default.titleMedium.copy(fontFamily = VazirmatnFamily),
    titleSmall = Default.titleSmall.copy(fontFamily = VazirmatnFamily),
    bodyLarge = Default.bodyLarge.copy(fontFamily = VazirmatnFamily),
    bodyMedium = Default.bodyMedium.copy(fontFamily = VazirmatnFamily),
    bodySmall = Default.bodySmall.copy(fontFamily = VazirmatnFamily),
    labelLarge = Default.labelLarge.copy(fontFamily = VazirmatnFamily),
    labelMedium = Default.labelMedium.copy(fontFamily = VazirmatnFamily),
    labelSmall = Default.labelSmall.copy(fontFamily = VazirmatnFamily),
)
