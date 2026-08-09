package ir.goldshop.app.util

import java.util.Calendar
import java.util.Date

data class PersianDate(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int) {

    private val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun toDisplayString(): String {
        val time = "%02d:%02d".format(hour, minute).toPersianDigits()
        return "$day ${monthNames[month - 1]} $year - $time".toPersianDigitsPreservingWords()
    }

    fun toDateOnlyString(): String = "$day ${monthNames[month - 1]} $year".toPersianDigitsPreservingWords()

    private fun String.toPersianDigitsPreservingWords(): String {
        return this.split(" ").joinToString(" ") { part ->
            if (part.all { it.isDigit() }) part.toPersianDigits() else part
        }
    }

    companion object {
        /**
         * تبدیل تاریخ میلادی به شمسی با الگوریتم Borkowski (پایه‌ی کتابخانه‌ی معروف jalaali-js)
         * که با استفاده از شماره روز ژولیوسی (JDN) به عنوان واسط، دقت بالایی دارد.
         */
        fun fromTimestamp(timestampMillis: Long): PersianDate {
            val cal = Calendar.getInstance()
            cal.time = Date(timestampMillis)
            val gYear = cal.get(Calendar.YEAR)
            val gMonth = cal.get(Calendar.MONTH) + 1
            val gDay = cal.get(Calendar.DAY_OF_MONTH)
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            val jalaali = d2j(g2d(gYear, gMonth, gDay))
            return PersianDate(jalaali.jy, jalaali.jm, jalaali.jd, hour, minute)
        }

        private val BREAKS = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )

        private data class JalaaliDate(val jy: Int, val jm: Int, val jd: Int)
        private data class JalCalResult(val leap: Int, val gy: Int, val march: Int)

        private fun div(a: Int, b: Int): Int = a / b
        private fun mod(a: Int, b: Int): Int = a - (a / b) * b

        private fun jalCal(jy: Int): JalCalResult {
            var leapJ = -14
            var jp = BREAKS[0]
            var jm: Int
            var jump = 0

            for (i in 1 until BREAKS.size) {
                jm = BREAKS[i]
                jump = jm - jp
                if (jy < jm) break
                leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
                jp = jm
            }
            val n = jy - jp

            leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
            if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1

            val gy = jy + 621
            val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
            val march = 20 + leapJ - leapG

            var adjusted = n
            if (jump - n < 6) {
                adjusted = n - jump + div(jump + 4, 33) * 33
            }
            var leap = mod(mod(adjusted + 1, 33) - 1, 4)
            if (leap == -1) leap = 4

            return JalCalResult(leap, gy, march)
        }

        /** تبدیل تاریخ میلادی به شماره‌ی روز ژولیوسی (JDN). */
        private fun g2d(gy: Int, gm: Int, gd: Int): Int {
            var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
                div(153 * mod(gm + 9, 12) + 2, 5) +
                gd - 34840408
            d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
            return d
        }

        /** تبدیل شماره‌ی روز ژولیوسی (JDN) به تاریخ میلادی. */
        private fun d2gYear(jdn: Int): Int {
            var j = 4 * jdn + 139361631
            j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
            val i = div(mod(j, 1461), 4) * 5 + 308
            val gm = mod(div(i, 153), 12) + 1
            return div(j, 1461) - 100100 + div(8 - gm, 6)
        }

        private fun d2j(jdn: Int): JalaaliDate {
            val gy = d2gYear(jdn)
            var jy = gy - 621
            val r = jalCal(jy)
            val jdn1f = g2d(r.gy, 3, r.march)

            var k = jdn - jdn1f
            if (k >= 0) {
                if (k <= 185) {
                    return JalaaliDate(jy, 1 + div(k, 31), mod(k, 31) + 1)
                }
                k -= 186
            } else {
                jy -= 1
                k += 179
                if (r.leap == 1) k += 1
            }
            return JalaaliDate(jy, 7 + div(k, 30), mod(k, 30) + 1)
        }
    }
}
