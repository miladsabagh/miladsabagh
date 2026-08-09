package ir.zarrin.goldshop.util

/**
 * تبدیل عدد به حروف فارسی برای درج در فاکتور («مبلغ به حروف»).
 */
object NumberToPersianWords {

    private val ONES = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val TEENS = arrayOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده",
        "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )
    private val TENS = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val HUNDREDS = arrayOf(
        "", "صد", "دویست", "سیصد", "چهارصد",
        "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"
    )
    private val SCALES = arrayOf("", "هزار", "میلیون", "میلیارد", "بیلیون", "بیلیارد")

    fun convert(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0) return "منفی " + convert(-number)

        val groups = ArrayList<Int>()
        var remaining = number
        while (remaining > 0) {
            groups.add((remaining % 1000).toInt())
            remaining /= 1000
        }

        val parts = ArrayList<String>()
        for (index in groups.indices.reversed()) {
            val value = groups[index]
            if (value == 0) continue
            val scale = SCALES.getOrElse(index) { "" }
            // «هزار» به‌جای «یک هزار» وقتی بزرگ‌ترین بخش عدد است
            val words = if (value == 1 && index == 1 && index == groups.size - 1) {
                scale
            } else if (scale.isEmpty()) {
                threeDigitsToWords(value)
            } else {
                "${threeDigitsToWords(value)} $scale"
            }
            parts.add(words)
        }
        return parts.joinToString(" و ")
    }

    /** مبلغ به حروف همراه با واحد پول، مثال: «دوازده میلیون و پانصد هزار تومان» */
    fun amountToWords(amount: Long, currencyLabel: String): String =
        "${convert(amount)} $currencyLabel"

    private fun threeDigitsToWords(value: Int): String {
        val parts = ArrayList<String>(3)
        val hundreds = value / 100
        val rest = value % 100
        if (hundreds > 0) parts.add(HUNDREDS[hundreds])
        when {
            rest in 10..19 -> parts.add(TEENS[rest - 10])
            else -> {
                val tens = rest / 10
                val ones = rest % 10
                if (tens > 0) parts.add(TENS[tens])
                if (ones > 0) parts.add(ONES[ones])
            }
        }
        return parts.joinToString(" و ")
    }
}
