package ir.zarrin.goldshop.core

import java.util.Calendar
import java.util.TimeZone

/** A date in the Solar Hijri (Jalali) calendar. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    val monthName: String get() = PersianCalendar.MONTH_NAMES[month - 1]

    /** `1403/05/19` with Persian digits. */
    fun formatNumeric(): String =
        PersianNumbers.toPersianDigits(
            "%04d/%02d/%02d".format(year, month, day)
        )

    /** `۱۹ مرداد ۱۴۰۳` */
    fun formatLong(): String =
        "${PersianNumbers.toPersianDigits(day.toString())} $monthName ${PersianNumbers.toPersianDigits(year.toString())}"

    fun toEpochMillis(): Long = PersianCalendar.toEpochMillis(this)
}

/**
 * Conversion between the Gregorian and Solar Hijri (Jalali) calendars.
 *
 * The algorithm is the widely used Borkowski / `jalaali` implementation which is exact for the
 * years 1178–3167 of the Jalali calendar.
 */
object PersianCalendar {

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val WEEK_DAY_NAMES = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private data class YearInfo(val leap: Int, val gy: Int, val march: Int)

    private fun jalaliCal(jy: Int): YearInfo {
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        require(jy >= jp && jy < BREAKS[BREAKS.size - 1]) { "سال شمسی خارج از محدوده پشتیبانی: $jy" }

        var jump = 0
        for (i in 1 until BREAKS.size) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1

        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = (((n + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return YearInfo(leap, gy, march)
    }

    fun isLeapYear(jalaliYear: Int): Boolean = jalaliCal(jalaliYear).leap == 0

    fun monthLength(jalaliYear: Int, jalaliMonth: Int): Int = when {
        jalaliMonth <= 6 -> 31
        jalaliMonth <= 11 -> 30
        isLeapYear(jalaliYear) -> 30
        else -> 29
    }

    /** Julian Day Number for a Gregorian date. */
    fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Long {
        val y = gy.toLong()
        val m = gm.toLong()
        var d = ((y + (m - 8) / 6 + 100100) * 1461) / 4 +
            (153 * ((m + 9) % 12) + 2) / 5 + gd - 34840408
        d -= (((y + 100100 + (m - 8) / 6) / 100) * 3) / 4 - 752
        return d
    }

    fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3) / 4 * 4 - 3908
        val i = ((j % 1461) / 4) * 5 + 308
        val gd = ((i % 153) / 5) + 1
        val gm = ((i / 153) % 12) + 1
        val gy = (j / 1461) - 100100 + (8 - gm) / 6
        return Triple(gy.toInt(), gm.toInt(), gd.toInt())
    }

    fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Long {
        val info = jalaliCal(jy)
        return gregorianToJdn(info.gy, 3, info.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    fun jdnToJalali(jdn: Long): JalaliDate {
        val (gy, _, _) = jdnToGregorian(jdn)
        var jy = gy - 621
        val info = jalaliCal(jy)
        val firstDayOfYearJdn = gregorianToJdn(info.gy, 3, info.march)
        var k = jdn - firstDayOfYearJdn
        if (k >= 0) {
            if (k <= 185) {
                return JalaliDate(jy, 1 + (k / 31).toInt(), (k % 31).toInt() + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (info.leap == 1) k += 1
        }
        return JalaliDate(jy, 7 + (k / 30).toInt(), (k % 30).toInt() + 1)
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): JalaliDate = jdnToJalali(gregorianToJdn(gy, gm, gd))

    fun toGregorian(date: JalaliDate): Triple<Int, Int, Int> =
        jdnToGregorian(jalaliToJdn(date.year, date.month, date.day))

    fun fromEpochMillis(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): JalaliDate {
        val calendar = Calendar.getInstance(timeZone)
        calendar.timeInMillis = millis
        return fromGregorian(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Start of the given Jalali day, in milliseconds. */
    fun toEpochMillis(date: JalaliDate, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val (gy, gm, gd) = toGregorian(date)
        val calendar = Calendar.getInstance(timeZone)
        calendar.clear()
        calendar.set(gy, gm - 1, gd, 0, 0, 0)
        return calendar.timeInMillis
    }

    fun today(timeZone: TimeZone = TimeZone.getDefault()): JalaliDate =
        fromEpochMillis(System.currentTimeMillis(), timeZone)

    fun startOfDay(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long =
        toEpochMillis(fromEpochMillis(millis, timeZone), timeZone)

    fun startOfJalaliMonth(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val date = fromEpochMillis(millis, timeZone)
        return toEpochMillis(date.copy(day = 1), timeZone)
    }

    fun startOfJalaliYear(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val date = fromEpochMillis(millis, timeZone)
        return toEpochMillis(date.copy(month = 1, day = 1), timeZone)
    }

    /** `۱۴:۳۵` clock label for the given instant. */
    fun formatClock(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val calendar = Calendar.getInstance(timeZone)
        calendar.timeInMillis = millis
        return PersianNumbers.toPersianDigits(
            "%02d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        )
    }

    fun weekDayName(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val calendar = Calendar.getInstance(timeZone)
        calendar.timeInMillis = millis
        // Calendar.SATURDAY == 7 and the Persian week starts on Saturday.
        val index = calendar.get(Calendar.DAY_OF_WEEK) % 7
        return WEEK_DAY_NAMES[index]
    }
}
