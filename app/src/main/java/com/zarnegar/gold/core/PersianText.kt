package com.zarnegar.gold.core

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * ابزارهای متنی فارسی: تبدیل ارقام، جداکننده هزارگان و تبدیل عدد به حروف.
 */
object PersianText {

    private const val PERSIAN_ZERO = '\u06F0'
    private const val THOUSANDS_SEPARATOR = '\u066C' // ARABIC THOUSANDS SEPARATOR (٬)
    private const val LTR_ISOLATE = '\u2066'
    private const val POP_ISOLATE = '\u2069'

    /**
     * متن را در یک «جزیرهٔ چپ‌به‌راست» می‌گذارد تا در متن راست‌چین جابه‌جا نشود؛
     * برای مواردی مثل شمارهٔ کارت یا علامت منفی که الگوریتم دوسویه آن‌ها را جابه‌جا می‌کند.
     */
    fun isolateLtr(value: String): String =
        if (value.isEmpty()) value else "$LTR_ISOLATE$value$POP_ISOLATE"

    /**
     * شناسه‌های عددی (شمارهٔ فاکتور، کد کالا، تلفن، کد ملی و …) را با ارقام فارسی و
     * ترتیب صحیح نمایش می‌دهد؛ بدون این کار، الگوریتم دوسویه بخش‌های عدد را جابه‌جا می‌کند.
     */
    fun formatCode(value: String): String = isolateLtr(toPersianDigits(value))

    fun toPersianDigits(value: String): String = buildString(value.length) {
        for (ch in value) {
            append(if (ch in '0'..'9') PERSIAN_ZERO + (ch - '0') else ch)
        }
    }

    fun toEnglishDigits(value: String): String = buildString(value.length) {
        for (ch in value) {
            when (ch) {
                in '\u06F0'..'\u06F9' -> append('0' + (ch - '\u06F0')) // Persian
                in '\u0660'..'\u0669' -> append('0' + (ch - '\u0660')) // Arabic-Indic
                else -> append(ch)
            }
        }
    }

    /** Adds `٬` every three digits: `1234567` → `1٬234٬567`. */
    fun groupDigits(value: Long): String {
        val negative = value < 0
        val digits = abs(value).toString()
        val sb = StringBuilder()
        for ((index, ch) in digits.withIndex()) {
            if (index > 0 && (digits.length - index) % 3 == 0) sb.append(THOUSANDS_SEPARATOR)
            sb.append(ch)
        }
        if (negative) sb.insert(0, '-')
        return sb.toString()
    }

    /** `1234567` → `۱٬۲۳۴٬۵۶۷`؛ اعداد منفی جدا می‌شوند تا علامت منفی سمت چپ بماند. */
    fun formatNumber(value: Long, persianDigits: Boolean = true): String {
        val text = if (persianDigits) toPersianDigits(groupDigits(value)) else groupDigits(value)
        return if (value < 0) isolateLtr(text) else text
    }

    /** Formats a weight in grams with up to three decimals: `4.5` → `۴٫۵۰۰ گرم`. */
    fun formatGrams(grams: Double, withUnit: Boolean = true, persianDigits: Boolean = true): String {
        val rounded = (grams * 1000).roundToLong()
        val whole = rounded / 1000
        val frac = abs(rounded % 1000)
        val raw = "${groupDigits(whole)}\u066B${"%03d".format(frac)}"
        val text = if (persianDigits) toPersianDigits(raw) else raw
        return if (withUnit) "$text گرم" else text
    }

    /** Formats a percentage, dropping a trailing `.0`: `7.0` → `۷٪`, `7.5` → `۷٫۵٪`. */
    fun formatPercent(value: Double, persianDigits: Boolean = true): String {
        val rounded = (value * 100).roundToLong()
        val whole = rounded / 100
        val frac = abs(rounded % 100)
        val raw = if (frac == 0L) "$whole" else "$whole\u066B" + "%02d".format(frac).trimEnd('0')
        return (if (persianDigits) toPersianDigits(raw) else raw) + "٪"
    }

    private val ONES = listOf(
        "", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه",
    )
    private val TEENS = listOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده",
        "شانزده", "هفده", "هجده", "نوزده",
    )
    private val TENS = listOf(
        "", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود",
    )
    private val HUNDREDS = listOf(
        "", "صد", "دویست", "سیصد", "چهارصد", "پانصد",
        "ششصد", "هفتصد", "هشتصد", "نهصد",
    )
    private val SCALES = listOf("", " هزار", " میلیون", " میلیارد", " بیلیون")

    /** `1250000` → `یک میلیون و دویست و پنجاه هزار` */
    fun numberToWords(value: Long): String {
        if (value == 0L) return "صفر"
        if (value < 0) return "منفی " + numberToWords(-value)

        val groups = mutableListOf<Int>()
        var remaining = value
        while (remaining > 0) {
            groups.add((remaining % 1000).toInt())
            remaining /= 1000
        }
        require(groups.size <= SCALES.size) { "عدد خارج از محدوده پشتیبانی‌شده است" }

        val parts = mutableListOf<String>()
        for (index in groups.indices.reversed()) {
            val group = groups[index]
            if (group == 0) continue
            parts.add(threeDigitsToWords(group) + SCALES[index])
        }
        return parts.joinToString(" و ")
    }

    private fun threeDigitsToWords(value: Int): String {
        val parts = mutableListOf<String>()
        val hundreds = value / 100
        val rest = value % 100
        if (hundreds > 0) parts.add(HUNDREDS[hundreds])
        when {
            rest == 0 -> Unit
            rest < 10 -> parts.add(ONES[rest])
            rest < 20 -> parts.add(TEENS[rest - 10])
            else -> {
                val tens = rest / 10
                val ones = rest % 10
                parts.add(if (ones == 0) TENS[tens] else "${TENS[tens]} و ${ONES[ones]}")
            }
        }
        return parts.joinToString(" و ")
    }
}
