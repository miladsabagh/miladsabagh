package ir.zarrin.goldshop.util

import java.util.Calendar
import java.util.TimeZone

/**
 * تبدیل تاریخ میلادی به شمسی (هجری خورشیدی) و بالعکس بر پایه الگوریتم بیرشک/jalaali.
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    /** خروجی مانند ۱۴۰۳/۰۵/۱۹ */
    fun formatted(persianDigits: Boolean = true): String {
        val raw = "%04d/%02d/%02d".format(year, month, day)
        return if (persianDigits) raw.toPersianDigits() else raw
    }

    /** خروجی مانند ۱۹ مرداد ۱۴۰۳ */
    fun formattedLong(persianDigits: Boolean = true): String {
        val raw = "$day ${monthName(month)} $year"
        return if (persianDigits) raw.toPersianDigits() else raw
    }

    fun toEpochMillis(hour: Int = 0, minute: Int = 0): Long {
        val g = toGregorian(this)
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.clear()
        cal.set(g.year, g.month - 1, g.day, hour, minute, 0)
        return cal.timeInMillis
    }

    companion object {
        private val MONTH_NAMES = arrayOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )

        private val WEEK_DAY_NAMES = arrayOf(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )

        private val BREAKS = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )

        fun monthName(month: Int): String = MONTH_NAMES[(month - 1).coerceIn(0, 11)]

        fun monthNames(): List<String> = MONTH_NAMES.toList()

        fun now(): JalaliDate = fromEpochMillis(System.currentTimeMillis())

        fun fromEpochMillis(millis: Long): JalaliDate {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = millis
            return fromGregorian(
                GregorianDate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            )
        }

        /** نام روز هفته برای یک زمان مشخص */
        fun weekDayName(millis: Long): String {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = millis
            // شنبه ابتدای هفته ایرانی است
            val index = (cal.get(Calendar.DAY_OF_WEEK) + 1) % 7
            return WEEK_DAY_NAMES[index]
        }

        fun fromGregorian(g: GregorianDate): JalaliDate = jdnToJalali(gregorianToJdn(g))

        fun toGregorian(j: JalaliDate): GregorianDate = jdnToGregorian(jalaliToJdn(j))

        /** تعداد روزهای ماه شمسی */
        fun monthLength(year: Int, month: Int): Int = when {
            month <= 6 -> 31
            month <= 11 -> 30
            isLeapYear(year) -> 30
            else -> 29
        }

        fun isLeapYear(year: Int): Boolean = calculate(year).leap == 0

        private fun div(a: Int, b: Int): Int = a / b

        private fun mod(a: Int, b: Int): Int = a - div(a, b) * b

        private data class YearCalc(val leap: Int, val gy: Int, val march: Int)

        private fun calculate(jy: Int): YearCalc {
            val gy = jy + 621
            var leapJ = -14
            var jp = BREAKS[0]
            require(jy >= jp && jy < BREAKS[BREAKS.size - 1]) { "سال شمسی نامعتبر است: $jy" }

            var jump = 0
            for (i in 1 until BREAKS.size) {
                val jm = BREAKS[i]
                jump = jm - jp
                if (jy < jm) break
                leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
                jp = jm
            }
            var n = jy - jp
            leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
            if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1

            val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
            val march = 20 + leapJ - leapG

            if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
            var leap = mod(mod(n + 1, 33) - 1, 4)
            if (leap == -1) leap = 4
            return YearCalc(leap, gy, march)
        }

        private fun gregorianToJdn(g: GregorianDate): Int {
            val gy = g.year
            val gm = g.month
            val gd = g.day
            var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
                div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
            d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
            return d
        }

        private fun jdnToGregorian(jdn: Int): GregorianDate {
            var j = 4 * jdn + 139361631
            j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
            val i = div(mod(j, 1461), 4) * 5 + 308
            val gd = div(mod(i, 153), 5) + 1
            val gm = mod(div(i, 153), 12) + 1
            val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
            return GregorianDate(gy, gm, gd)
        }

        private fun jalaliToJdn(j: JalaliDate): Int {
            val r = calculate(j.year)
            return gregorianToJdn(GregorianDate(r.gy, 3, r.march)) +
                (j.month - 1) * 31 - div(j.month, 7) * (j.month - 7) + j.day - 1
        }

        private fun jdnToJalali(jdn: Int): JalaliDate {
            val gy = jdnToGregorian(jdn).year
            var jy = gy - 621
            val r = calculate(jy)
            val jdn1f = gregorianToJdn(GregorianDate(gy, 3, r.march))
            var k = jdn - jdn1f
            if (k >= 0) {
                if (k <= 185) {
                    return JalaliDate(jy, 1 + div(k, 31), mod(k, 31) + 1)
                }
                k -= 186
            } else {
                jy -= 1
                k += 179
                if (r.leap == 1) k += 1
            }
            return JalaliDate(jy, 7 + div(k, 30), mod(k, 30) + 1)
        }
    }
}

data class GregorianDate(val year: Int, val month: Int, val day: Int)

/** برچسب تاریخ و ساعت شمسی برای نمایش در فاکتور */
fun Long.toJalaliDateTimeLabel(): String {
    val date = JalaliDate.fromEpochMillis(this)
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    return "${date.formatted()} - ${time.toPersianDigits()}"
}

fun Long.toJalaliDateLabel(): String = JalaliDate.fromEpochMillis(this).formatted()
