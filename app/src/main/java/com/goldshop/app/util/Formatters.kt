package com.goldshop.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = map { ch ->
    if (ch in '0'..'9') persianDigits[ch - '0'] else ch
}.joinToString("")

fun Long.toPersianDigits(): String = toString().toPersianDigits()

fun Int.toPersianDigits(): String = toString().toPersianDigits()

fun Double.toPersianDigits(decimals: Int = 2): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val pattern = if (decimals <= 0) "#" else "#.${"#".repeat(decimals)}"
    return DecimalFormat(pattern, symbols).format(this).toPersianDigits()
}

/** نمایش مبلغ به تومان (ورودی ریال است) */
fun Long.formatToman(): String {
    val toman = this / 10
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
    val formatted = DecimalFormat("#,###", symbols).format(toman)
    return "${formatted.toPersianDigits()} تومان"
}

fun Long.formatRial(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
    val formatted = DecimalFormat("#,###", symbols).format(this)
    return "${formatted.toPersianDigits()} ریال"
}

fun Long.formatDateFa(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US)
    return sdf.format(Date(this)).toPersianDigits()
}

fun purityLabel(purity: Int): String = when (purity) {
    750 -> "۱۸ عیار"
    900 -> "۲۱ عیار"
    995, 999 -> "۲۴ عیار"
    else -> "عیار ${purity.toPersianDigits()}"
}

fun startOfTodayMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun startOfMonthMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
