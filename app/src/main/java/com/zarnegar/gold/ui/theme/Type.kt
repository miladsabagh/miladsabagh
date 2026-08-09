package com.zarnegar.gold.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zarnegar.gold.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val base = Typography()

val ZarnegarTypography = Typography(
    displayLarge = base.displayLarge.withVazir(),
    displayMedium = base.displayMedium.withVazir(),
    displaySmall = base.displaySmall.withVazir(),
    headlineLarge = base.headlineLarge.withVazir(FontWeight.Bold),
    headlineMedium = base.headlineMedium.withVazir(FontWeight.Bold),
    headlineSmall = base.headlineSmall.withVazir(FontWeight.Bold),
    titleLarge = base.titleLarge.withVazir(FontWeight.Bold, 20.sp),
    titleMedium = base.titleMedium.withVazir(FontWeight.SemiBold),
    titleSmall = base.titleSmall.withVazir(FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.withVazir(lineHeight = 26.sp),
    bodyMedium = base.bodyMedium.withVazir(lineHeight = 24.sp),
    bodySmall = base.bodySmall.withVazir(lineHeight = 20.sp),
    labelLarge = base.labelLarge.withVazir(FontWeight.SemiBold),
    labelMedium = base.labelMedium.withVazir(FontWeight.Medium),
    labelSmall = base.labelSmall.withVazir(FontWeight.Medium),
)

private fun TextStyle.withVazir(
    weight: FontWeight = fontWeight ?: FontWeight.Normal,
    size: androidx.compose.ui.unit.TextUnit = fontSize,
    lineHeight: androidx.compose.ui.unit.TextUnit = this.lineHeight,
) = copy(
    fontFamily = Vazirmatn,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
)
