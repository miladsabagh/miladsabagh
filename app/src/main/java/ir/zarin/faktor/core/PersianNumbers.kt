package ir.zarin.faktor.core

/**
 * تبدیل و قالب‌بندی اعداد برای نمایش فارسی.
 */
object PersianNumbers {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun toPersianDigits(input: String): String = buildString(input.length) {
        for (ch in input) {
            if (ch in '0'..'9') append(PERSIAN_DIGITS[ch - '0']) else append(ch)
        }
    }

    /** ارقام فارسی و عربی را به ارقام لاتین تبدیل می‌کند تا ورودی کاربر قابل تجزیه باشد. */
    fun toLatinDigits(input: String): String = buildString(input.length) {
        for (ch in input) {
            val persianIndex = PERSIAN_DIGITS.indexOf(ch)
            val arabicIndex = ARABIC_DIGITS.indexOf(ch)
            when {
                persianIndex >= 0 -> append('0' + persianIndex)
                arabicIndex >= 0 -> append('0' + arabicIndex)
                ch == '٫' -> append('.')
                else -> append(ch)
            }
        }
    }

    /** جداکننده هزارگان برای اعداد صحیح. */
    fun group(value: Long): String {
        val negative = value < 0
        val digits = kotlin.math.abs(value).toString()
        val grouped = buildString {
            for ((index, ch) in digits.withIndex()) {
                if (index > 0 && (digits.length - index) % 3 == 0) append(',')
                append(ch)
            }
        }
        return if (negative) "-$grouped" else grouped
    }

    /** نمایش عدد اعشاری بدون صفرهای انتهایی (مناسب وزن به گرم). */
    fun decimal(value: Double, maxDecimals: Int = 3): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val rounded = String.format(java.util.Locale.US, "%.${maxDecimals}f", value)
        val trimmed = if (rounded.contains('.')) rounded.trimEnd('0').trimEnd('.') else rounded
        val parts = trimmed.split('.')
        val intPart = group(parts[0].toLongOrNull() ?: 0L)
        return if (parts.size > 1) "$intPart.${parts[1]}" else intPart
    }

    /** ورودی کاربر (با ارقام فارسی و جداکننده) را به عدد تبدیل می‌کند. */
    fun parseLong(input: String): Long? =
        toLatinDigits(input).replace(",", "").replace("،", "").trim()
            .takeIf { it.isNotEmpty() }?.toLongOrNull()

    fun parseDouble(input: String): Double? =
        toLatinDigits(input).replace(",", "").replace("،", "").trim()
            .takeIf { it.isNotEmpty() }?.toDoubleOrNull()
}
