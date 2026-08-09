package com.miladsabagh.zarrin.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = map { ch ->
    if (ch in '0'..'9') persianDigits[ch - '0'] else ch
}.joinToString("")

fun Long.formatToman(withUnit: Boolean = true): String {
    val grouped = String.format(Locale.US, "%,d", this)
    val persian = grouped.toPersianDigits()
    return if (withUnit) "$persian تومان" else persian
}

fun Double.formatGram(): String {
    val text = if (this % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", this)
    } else {
        String.format(Locale.US, "%.3f", this).trimEnd('0').trimEnd('.')
    }
    return "${text.toPersianDigits()} گرم"
}

fun Long.formatDateTime(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)
    return sdf.format(Date(this)).toPersianDigits()
}

fun Long.formatInvoiceNumber(): String = "$this".toPersianDigits()

/** Parses a free-typed amount that may contain Persian/Arabic digits and separators. */
fun String.parseAmountToLong(): Long? {
    val normalized = map { ch ->
        when (ch) {
            in '۰'..'۹' -> ('0' + (ch - '۰'))
            in '٠'..'٩' -> ('0' + (ch - '٠'))
            ',', '٬', '،', ' ' -> null
            else -> ch
        }
    }.filterNotNull().joinToString("")
    return normalized.toLongOrNull()
}

fun String.parseWeightToDouble(): Double? {
    val normalized = map { ch ->
        when (ch) {
            in '۰'..'۹' -> ('0' + (ch - '۰'))
            in '٠'..'٩' -> ('0' + (ch - '٠'))
            '٫' -> '.'
            ',', '٬', '،', ' ' -> null
            else -> ch
        }
    }.filterNotNull().joinToString("")
    return normalized.toDoubleOrNull()
}
