package ir.zarin.faktor.core

/**
 * تبدیل عدد به حروف فارسی؛ برای درج «مبلغ به حروف» روی فاکتور.
 */
object NumberToPersianWords {

    private val ONES = listOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val TEENS = listOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده",
        "پانزده", "شانزده", "هفده", "هجده", "نوزده",
    )
    private val TENS = listOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val HUNDREDS = listOf(
        "", "صد", "دویست", "سیصد", "چهارصد",
        "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد",
    )
    private val SCALES = listOf("", " هزار", " میلیون", " میلیارد", " هزار میلیارد", " میلیون میلیارد")

    private const val SEPARATOR = " و "

    fun convert(value: Long): String {
        if (value == 0L) return "صفر"
        val prefix = if (value < 0) "منفی " else ""
        var remaining = kotlin.math.abs(value)

        val groups = mutableListOf<Int>()
        while (remaining > 0) {
            groups.add((remaining % 1000).toInt())
            remaining /= 1000
        }
        if (groups.size > SCALES.size) return prefix + PersianNumbers.group(kotlin.math.abs(value))

        val parts = mutableListOf<String>()
        for (index in groups.indices.reversed()) {
            val group = groups[index]
            if (group == 0) continue
            parts.add(threeDigitsToWords(group) + SCALES[index])
        }
        return prefix + parts.joinToString(SEPARATOR)
    }

    private fun threeDigitsToWords(number: Int): String {
        val parts = mutableListOf<String>()
        val hundreds = number / 100
        val remainder = number % 100
        if (hundreds > 0) parts.add(HUNDREDS[hundreds])
        when {
            remainder in 10..19 -> parts.add(TEENS[remainder - 10])
            remainder > 0 -> {
                val tens = remainder / 10
                val ones = remainder % 10
                if (tens > 0) parts.add(TENS[tens])
                if (ones > 0) parts.add(ONES[ones])
            }
        }
        return parts.joinToString(SEPARATOR)
    }
}
