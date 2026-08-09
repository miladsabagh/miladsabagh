package com.zarin.gold.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = buildString {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') persianDigits[ch - '0'] else ch)
    }
}

fun Long.toPersianCurrency(): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(this)
    return "${formatted.toPersianDigits()} ریال"
}

fun Long.toPersianNumber(): String =
    NumberFormat.getNumberInstance(Locale.US).format(this).toPersianDigits()

fun Double.toPersianWeight(): String {
    val text = if (this % 1.0 == 0.0) this.toLong().toString() else String.format(Locale.US, "%.2f", this)
    return "${text.toPersianDigits()} گرم"
}

fun Long.toPersianDateTime(): String {
    val fmt = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US)
    return fmt.format(Date(this)).toPersianDigits()
}

fun Int.toPersian(): String = toString().toPersianDigits()
