package ir.zarrin.goldshop.util

import kotlin.math.abs
import kotlin.math.roundToLong

private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

const val THOUSAND_SEPARATOR = '٬'
const val DECIMAL_SEPARATOR = '٫'

/** تبدیل ارقام لاتین به ارقام فارسی */
fun String.toPersianDigits(): String {
    val sb = StringBuilder(length)
    for (ch in this) {
        sb.append(if (ch in '0'..'9') PERSIAN_DIGITS[ch - '0'] else ch)
    }
    return sb.toString()
}

/** تبدیل ارقام فارسی/عربی به ارقام لاتین (برای ورودی کاربر) */
fun String.toLatinDigits(): String {
    val sb = StringBuilder(length)
    for (ch in this) {
        val converted = when (ch) {
            in '۰'..'۹' -> '0' + (ch - '۰')
            in '٠'..'٩' -> '0' + (ch - '٠')
            else -> ch
        }
        sb.append(converted)
    }
    return sb.toString()
}

fun Long.toPersianDigits(): String = toString().toPersianDigits()

fun Int.toPersianDigits(): String = toString().toPersianDigits()

/** جداسازی سه‌رقمی عدد، مثال: ۱۲٬۵۰۰٬۰۰۰ */
fun Long.groupDigits(persianDigits: Boolean = true): String {
    val negative = this < 0
    val digits = abs(this).toString()
    val sb = StringBuilder()
    for ((index, ch) in digits.withIndex()) {
        if (index > 0 && (digits.length - index) % 3 == 0) sb.append(THOUSAND_SEPARATOR)
        sb.append(ch)
    }
    val result = if (negative) "-$sb" else sb.toString()
    return if (persianDigits) result.toPersianDigits() else result
}

/** نمایش وزن با حداکثر سه رقم اعشار، مثال: ۴٫۲۵ گرم */
fun Double.formatWeight(persianDigits: Boolean = true): String {
    val rounded = (this * 1000).roundToLong() / 1000.0
    var text = if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString().trimEnd('0').trimEnd('.')
    }
    text = text.replace('.', DECIMAL_SEPARATOR)
    return if (persianDigits) text.toPersianDigits() else text
}

/** نمایش درصد، مثال: ۷٪ */
fun Double.formatPercent(persianDigits: Boolean = true): String {
    val text = if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        ((this * 100).roundToLong() / 100.0).toString().trimEnd('0').trimEnd('.')
    }
    val withSeparator = text.replace('.', DECIMAL_SEPARATOR)
    return (if (persianDigits) withSeparator.toPersianDigits() else withSeparator) + "٪"
}

/** خواندن عدد صحیح از ورودی کاربر با پشتیبانی از ارقام فارسی و جداکننده‌ها */
fun String.parseAmountOrNull(): Long? {
    val cleaned = toLatinDigits().filter { it.isDigit() }
    if (cleaned.isEmpty()) return null
    return cleaned.toLongOrNull()
}

/** خواندن عدد اعشاری (وزن/درصد) از ورودی کاربر */
fun String.parseDecimalOrNull(): Double? {
    val cleaned = toLatinDigits()
        .replace(DECIMAL_SEPARATOR, '.')
        .replace('٬', ' ')
        .filter { it.isDigit() || it == '.' }
    if (cleaned.isEmpty() || cleaned == ".") return null
    return cleaned.toDoubleOrNull()
}
