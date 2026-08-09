package com.miladsabagh.goldinvoice.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.miladsabagh.goldinvoice.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_light, FontWeight.Light),
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private fun baseStyle(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Vazirmatn,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val GoldInvoiceTypography = Typography(
    displayLarge = baseStyle(52, FontWeight.Bold, 60),
    displayMedium = baseStyle(40, FontWeight.Bold, 48),
    displaySmall = baseStyle(32, FontWeight.SemiBold, 40),
    headlineLarge = baseStyle(28, FontWeight.SemiBold, 36),
    headlineMedium = baseStyle(24, FontWeight.SemiBold, 32),
    headlineSmall = baseStyle(21, FontWeight.SemiBold, 28),
    titleLarge = baseStyle(20, FontWeight.SemiBold, 26),
    titleMedium = baseStyle(17, FontWeight.Medium, 24),
    titleSmall = baseStyle(15, FontWeight.Medium, 20),
    bodyLarge = baseStyle(16, FontWeight.Normal, 24),
    bodyMedium = baseStyle(14, FontWeight.Normal, 20),
    bodySmall = baseStyle(12, FontWeight.Normal, 16),
    labelLarge = baseStyle(15, FontWeight.Medium, 20),
    labelMedium = baseStyle(13, FontWeight.Medium, 16),
    labelSmall = baseStyle(11, FontWeight.Medium, 14)
)
