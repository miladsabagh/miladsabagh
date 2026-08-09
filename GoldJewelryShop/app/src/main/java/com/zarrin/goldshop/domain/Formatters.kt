package com.zarrin.goldshop.domain

import com.zarrin.goldshop.data.ProductCategory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val persianLocale = Locale("fa", "IR")

private val amountSymbols = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ','
}

private val amountFormat = DecimalFormat("#,###", amountSymbols)
private val weightFormat = DecimalFormat("0.###", DecimalFormatSymbols(Locale.US))

fun formatToman(amount: Long): String = "${amountFormat.format(amount)} تومان"

fun formatWeight(grams: Double): String = "${weightFormat.format(grams)} گرم"

fun formatPercent(value: Double): String = "${weightFormat.format(value)}٪"

fun toPersianDigits(input: String): String {
    val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString {
        input.forEach { ch ->
            append(if (ch in '0'..'9') persian[ch - '0'] else ch)
        }
    }
}

fun formatTomanFa(amount: Long): String = toPersianDigits(formatToman(amount))

fun formatWeightFa(grams: Double): String = toPersianDigits(formatWeight(grams))

fun formatPercentFa(value: Double): String = toPersianDigits(formatPercent(value))

fun categoryLabel(category: ProductCategory): String = when (category) {
    ProductCategory.RING -> "انگشتر"
    ProductCategory.NECKLACE -> "گردنبند"
    ProductCategory.BRACELET -> "دستبند"
    ProductCategory.EARRING -> "گوشواره"
    ProductCategory.PENDANT -> "آویز"
    ProductCategory.COIN -> "سکه"
    ProductCategory.BULLION -> "شمش"
    ProductCategory.OTHER -> "سایر"
}

fun formatDateFa(epochMs: Long): String {
    val cal = Calendar.getInstance(TimeZone.getDefault(), persianLocale)
    cal.timeInMillis = epochMs
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    return toPersianDigits(
        String.format(Locale.US, "%04d/%02d/%02d  %02d:%02d", y, m, d, h, min)
    )
}

fun startOfTodayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun startOfMonthMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun parseLongOrZero(text: String): Long =
    text.filter { it.isDigit() }.toLongOrNull() ?: 0L

fun parseDoubleOrZero(text: String): Double =
    text.replace(',', '.').filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
