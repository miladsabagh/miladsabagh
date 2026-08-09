package com.miladsabagh.goldinvoice.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** Converts every ASCII digit in this string to its Persian (Farsi) equivalent. */
fun String.toPersianDigits(): String {
    val builder = StringBuilder(length)
    for (char in this) {
        val index = englishDigits.indexOf(char)
        builder.append(if (index >= 0) persianDigits[index] else char)
    }
    return builder.toString()
}

/** Converts Persian/Arabic-Indic digits typed by the user back to ASCII digits for parsing. */
fun String.toEnglishDigits(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder(length)
    for (char in this) {
        val persianIndex = persianDigits.indexOf(char)
        val arabicIndex = arabicDigits.indexOf(char)
        when {
            persianIndex >= 0 -> builder.append(englishDigits[persianIndex])
            arabicIndex >= 0 -> builder.append(englishDigits[arabicIndex])
            else -> builder.append(char)
        }
    }
    return builder.toString()
}

private val groupedIntegerFormat = DecimalFormat("#,###")

/** Formats a currency amount (Toman) with thousands separators and Persian digits, e.g. "۱,۲۵۰,۰۰۰". */
fun formatCurrency(amount: Double): String =
    groupedIntegerFormat.format(amount.roundToLong()).toPersianDigits()

/** Formats a plain number (no currency) with Persian digits and thousands separators. */
fun formatNumber(amount: Double, maxDecimals: Int = 3): String {
    val pattern = if (maxDecimals > 0) "#,##0.${"#".repeat(maxDecimals)}" else "#,##0"
    return DecimalFormat(pattern).format(amount).toPersianDigits()
}

fun formatWeight(grams: Double): String = formatNumber(grams, maxDecimals = 3)

fun formatPercent(value: Double): String = formatNumber(value, maxDecimals = 2)

private val invoiceDateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)

fun formatInvoiceDate(epochMillis: Long): String =
    invoiceDateFormat.format(Date(epochMillis)).toPersianDigits()

/** Parses a user-entered numeric string (possibly with Persian digits/commas) to a Double, or null. */
fun parseLocalizedDouble(text: String): Double? =
    text.toEnglishDigits().replace(",", "").trim().toDoubleOrNull()

fun parseLocalizedInt(text: String): Int? =
    text.toEnglishDigits().replace(",", "").trim().toIntOrNull()
