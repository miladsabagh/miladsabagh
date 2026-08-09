package ir.zarin.faktor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.zarin.faktor.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_medium, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

val ZarinTypography: Typography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = Vazirmatn),
        displayMedium = displayMedium.copy(fontFamily = Vazirmatn),
        displaySmall = displaySmall.copy(fontFamily = Vazirmatn),
        headlineLarge = headlineLarge.copy(fontFamily = Vazirmatn),
        headlineMedium = headlineMedium.copy(fontFamily = Vazirmatn),
        headlineSmall = headlineSmall.copy(fontFamily = Vazirmatn),
        titleLarge = titleLarge.copy(fontFamily = Vazirmatn),
        titleMedium = titleMedium.copy(fontFamily = Vazirmatn),
        titleSmall = titleSmall.copy(fontFamily = Vazirmatn),
        bodyLarge = bodyLarge.copy(fontFamily = Vazirmatn),
        bodyMedium = bodyMedium.copy(fontFamily = Vazirmatn),
        bodySmall = bodySmall.copy(fontFamily = Vazirmatn),
        labelLarge = labelLarge.copy(fontFamily = Vazirmatn),
        labelMedium = labelMedium.copy(fontFamily = Vazirmatn),
        labelSmall = labelSmall.copy(fontFamily = Vazirmatn),
    )
}
