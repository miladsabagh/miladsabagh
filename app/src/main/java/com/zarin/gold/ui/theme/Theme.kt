package com.zarin.gold.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// جهت بصری: شب جواهرفروشی — ذغال گرم + طلای کهربا (نه بنفش، نه کرم‌ترراکتا)
object ZarinColors {
    val Night = Color(0xFF0B0B0C)
    val NightElevated = Color(0xFF161418)
    val NightSoft = Color(0xFF1E1A1C)
    val Amber = Color(0xFFD4AF37)
    val AmberSoft = Color(0xFFE8D48B)
    val AmberDeep = Color(0xFF9A7B2F)
    val Ivory = Color(0xFFF6F0E4)
    val IvoryMuted = Color(0xFFB8AFA0)
    val RoseMetal = Color(0xFFC48B7A)
    val Success = Color(0xFF7CB89A)
    val Danger = Color(0xFFCF7A7A)
    val Line = Color(0x33D4AF37)

    val Atmosphere = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF14110F),
            Color(0xFF0B0B0C),
            Color(0xFF120E12)
        )
    )

    val GoldSheen = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8A6B28),
            Color(0xFFD4AF37),
            Color(0xFFF0E0A0),
            Color(0xFFD4AF37),
            Color(0xFF8A6B28)
        )
    )

    val CardGlow = Brush.radialGradient(
        colors = listOf(Color(0x33D4AF37), Color.Transparent)
    )
}

private val ColorScheme = darkColorScheme(
    primary = ZarinColors.Amber,
    onPrimary = ZarinColors.Night,
    secondary = ZarinColors.RoseMetal,
    onSecondary = ZarinColors.Night,
    background = ZarinColors.Night,
    onBackground = ZarinColors.Ivory,
    surface = ZarinColors.NightElevated,
    onSurface = ZarinColors.Ivory,
    surfaceVariant = ZarinColors.NightSoft,
    onSurfaceVariant = ZarinColors.IvoryMuted,
    outline = ZarinColors.Line
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        letterSpacing = 1.sp,
        color = ZarinColors.Ivory
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        color = ZarinColors.Ivory
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = ZarinColors.Ivory
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = ZarinColors.Ivory
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = ZarinColors.Ivory
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = ZarinColors.Ivory
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = ZarinColors.IvoryMuted
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp,
        color = ZarinColors.Night
    )
)

@Composable
fun ZarinTheme(content: @Composable () -> Unit) {
    // برند جواهر همیشه در فضای شب طلایی می‌ماند
    @Suppress("UNUSED_VARIABLE")
    val ignoreSystemTheme = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        content = content
    )
}
