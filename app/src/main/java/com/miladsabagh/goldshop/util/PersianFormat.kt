package com.miladsabagh.goldshop.util

import java.util.Locale

/** Formatting helpers for Persian-locale numbers, currency and dates. */
object PersianFormat {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(if (c in '0'..'9') persianDigits[c - '0'] else c)
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            val idx = persianDigits.indexOf(c)
            sb.append(if (idx >= 0) ('0' + idx) else c)
        }
        return sb.toString()
    }

    /** Formats a number with thousands separators, e.g. 1234567 -> ۱٬۲۳۴٬۵۶۷ */
    fun formatNumber(value: Long, persianDigitsOutput: Boolean = true): String {
        val formatted = String.format(Locale.US, "%,d", value).replace(",", "٬")
        return if (persianDigitsOutput) toPersianDigits(formatted) else formatted
    }

    fun formatNumber(value: Double, persianDigitsOutput: Boolean = true): String =
        formatNumber(Math.round(value), persianDigitsOutput)

    fun formatToman(value: Double, persianDigitsOutput: Boolean = true): String =
        "${formatNumber(value, persianDigitsOutput)} تومان"

    fun formatWeight(grams: Double, persianDigitsOutput: Boolean = true): String {
        val text = String.format(Locale.US, "%.3f", grams)
        return if (persianDigitsOutput) "${toPersianDigits(text)} گرم" else "$text گرم"
    }
}
