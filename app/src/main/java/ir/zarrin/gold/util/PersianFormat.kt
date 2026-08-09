package ir.zarrin.gold.util

import java.util.Calendar
import java.util.Locale

/** ابزارهای نمایش اعداد و تاریخ به سبک فارسی. */
object PersianFormat {

    private const val PERSIAN_ZERO = '۰'

    fun toPersianDigits(input: String): String = buildString(input.length) {
        for (ch in input) {
            append(if (ch in '0'..'9') PERSIAN_ZERO + (ch - '0') else ch)
        }
    }

    fun toEnglishDigits(input: String): String = buildString(input.length) {
        for (ch in input) {
            append(
                when (ch) {
                    in '۰'..'۹' -> '0' + (ch - PERSIAN_ZERO)
                    in '٠'..'٩' -> '0' + (ch - '٠')
                    '٫', '،' -> '.'
                    else -> ch
                }
            )
        }
    }

    /** جداکننده هزارگان + رقم فارسی: 1234567 → ۱٬۲۳۴٬۵۶۷ */
    fun formatNumber(value: Long): String {
        val grouped = String.format(Locale.US, "%,d", value).replace(",", "٬")
        return toPersianDigits(grouped)
    }

    fun formatCurrency(value: Long): String = formatNumber(value) + " تومان"

    /** وزن با حداکثر ۳ رقم اعشار، بدون صفرهای اضافه. */
    fun formatWeight(grams: Double): String {
        val text = if (grams % 1.0 == 0.0) {
            grams.toLong().toString()
        } else {
            String.format(Locale.US, "%.3f", grams).trimEnd('0').trimEnd('.')
        }
        return toPersianDigits(text) + " گرم"
    }

    fun parseDouble(input: String): Double? =
        toEnglishDigits(input).replace("٬", "").replace(",", "").trim().toDoubleOrNull()

    fun parseLong(input: String): Long? =
        toEnglishDigits(input).replace("٬", "").replace(",", "").trim().toLongOrNull()
}

/** تبدیل تاریخ میلادی به هجری شمسی (جلالی). */
object JalaliDate {

    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /** خروجی: [سال، ماه، روز] */
    fun fromGregorian(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 +
            (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return intArrayOf(jy, jm, jd)
    }

    /** مثل «۱۴۰۵/۰۵/۱۸» */
    fun format(timeMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val (jy, jm, jd) = fromGregorian(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        ).let { Triple(it[0], it[1], it[2]) }
        return PersianFormat.toPersianDigits(
            String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
        )
    }

    /** مثل «۱۸ مرداد ۱۴۰۵» */
    fun formatLong(timeMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val parts = fromGregorian(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        return PersianFormat.toPersianDigits("${parts[2]} ${monthNames[parts[1] - 1]} ${parts[0]}")
    }
}
