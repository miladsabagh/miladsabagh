package ir.zarrin.goldshop.core

import kotlin.math.abs
import kotlin.math.roundToLong

/** Persian digit handling, money grouping and spelling numbers out in Persian words. */
object PersianNumbers {

    private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"
    private const val ARABIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

    fun toPersianDigits(input: String): String = buildString(input.length) {
        for (character in input) {
            if (character in '0'..'9') append(PERSIAN_DIGITS[character - '0']) else append(character)
        }
    }

    /** Normalises Persian/Arabic digits and separators back to plain ASCII so input can be parsed. */
    fun toEnglishDigits(input: String): String = buildString(input.length) {
        for (character in input) {
            val persianIndex = PERSIAN_DIGITS.indexOf(character)
            val arabicIndex = ARABIC_DIGITS.indexOf(character)
            when {
                persianIndex >= 0 -> append('0' + persianIndex)
                arabicIndex >= 0 -> append('0' + arabicIndex)
                character == '٫' -> append('.')
                else -> append(character)
            }
        }
    }

    /** `1234567` -> `1,234,567` */
    fun groupDigits(value: Long): String {
        val negative = value < 0
        val digits = abs(value).toString()
        val grouped = StringBuilder()
        for ((index, digit) in digits.withIndex()) {
            if (index > 0 && (digits.length - index) % 3 == 0) grouped.append(',')
            grouped.append(digit)
        }
        return if (negative) "-$grouped" else grouped.toString()
    }

    /** `1234567` -> `۱,۲۳۴,۵۶۷` */
    fun formatAmount(value: Long): String = toPersianDigits(groupDigits(value))

    /** Trims trailing zeros so `12.500` reads `۱۲٫۵` and `3.000` reads `۳`. */
    fun formatWeight(grams: Double, decimals: Int = 3): String {
        val rounded = "%.${decimals}f".format(grams)
        val trimmed = if (rounded.contains('.')) rounded.trimEnd('0').trimEnd('.') else rounded
        return toPersianDigits(trimmed.ifEmpty { "0" }).replace('.', '٫')
    }

    fun formatPercent(value: Double): String {
        val rounded = "%.2f".format(value)
        val trimmed = if (rounded.contains('.')) rounded.trimEnd('0').trimEnd('.') else rounded
        return toPersianDigits(trimmed.ifEmpty { "0" }).replace('.', '٫')
    }

    fun parseLong(input: String): Long? =
        toEnglishDigits(input).filter { it.isDigit() || it == '-' }.toLongOrNull()

    fun parseDouble(input: String): Double? {
        val normalized = toEnglishDigits(input).replace(",", "").trim()
        if (normalized.isEmpty()) return null
        return normalized.toDoubleOrNull()
    }

    private val ONES = listOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val TEENS = listOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده",
        "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )
    private val TENS = listOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val HUNDREDS = listOf(
        "", "صد", "دویست", "سیصد", "چهارصد",
        "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"
    )
    private val SCALES = listOf("", " هزار", " میلیون", " میلیارد", " بیلیون")

    private fun threeDigitsToWords(value: Int): String {
        val parts = mutableListOf<String>()
        val hundreds = value / 100
        val remainder = value % 100
        if (hundreds > 0) parts += HUNDREDS[hundreds]
        when {
            remainder in 10..19 -> parts += TEENS[remainder - 10]
            else -> {
                val tens = remainder / 10
                val ones = remainder % 10
                if (tens > 0) parts += TENS[tens]
                if (ones > 0) parts += ONES[ones]
            }
        }
        return parts.joinToString(" و ")
    }

    /** Spells an amount out in Persian, e.g. `12500` -> `دوازده هزار و پانصد`. */
    fun toWords(value: Long): String {
        if (value == 0L) return "صفر"
        val prefix = if (value < 0) "منفی " else ""
        var remaining = abs(value)
        val groups = mutableListOf<Int>()
        while (remaining > 0) {
            groups += (remaining % 1000).toInt()
            remaining /= 1000
        }
        val words = mutableListOf<String>()
        for (index in groups.indices.reversed()) {
            val group = groups[index]
            if (group == 0) continue
            val scale = if (index < SCALES.size) SCALES[index] else ""
            words += threeDigitsToWords(group) + scale
        }
        return prefix + words.joinToString(" و ")
    }

    fun roundToLong(value: Double): Long = if (value.isFinite()) value.roundToLong() else 0L
}
