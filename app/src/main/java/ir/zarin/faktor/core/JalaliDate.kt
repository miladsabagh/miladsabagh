package ir.zarin.faktor.core

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

/** یک تاریخ در گاه‌شمار هجری شمسی. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    val monthName: String get() = MONTH_NAMES[month - 1]

    /** مثال: ۱۴۰۵/۰۵/۱۸ */
    fun format(): String = String.format(java.util.Locale.US, "%04d/%02d/%02d", year, month, day)

    /** مثال: ۱۸ مرداد ۱۴۰۵ */
    fun formatLong(): String = "$day $monthName $year"

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )
    }
}

/**
 * تبدیل میلادی ↔ هجری شمسی بر پایه الگوریتم بورکوفسکی (همان الگوریتم jalaali-js).
 * برای سال‌های ۱۱۷۸ تا ۳۱۷۷ هجری شمسی معتبر است.
 */
object JalaliCalendar {

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    private fun div(a: Int, b: Int): Int = a / b

    private fun mod(a: Int, b: Int): Int = a - (a / b) * b

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCal {
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

        return JalCal(leap = leap, gy = gy, march = march)
    }

    /** شماره روز ژولیَن برای یک تاریخ میلادی. */
    private fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
        d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
        return d
    }

    private fun jdnToGregorian(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return Triple(gy, gm, gd)
    }

    private fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return gregorianToJdn(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    private fun jdnToJalali(jdn: Int): JalaliDate {
        val (gy, _, _) = jdnToGregorian(jdn)
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = gregorianToJdn(gy, 3, r.march)
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

    fun fromGregorian(gy: Int, gm: Int, gd: Int): JalaliDate = jdnToJalali(gregorianToJdn(gy, gm, gd))

    fun toGregorian(date: JalaliDate): Triple<Int, Int, Int> =
        jdnToGregorian(jalaliToJdn(date.year, date.month, date.day))

    fun isLeapYear(jy: Int): Boolean = jalCal(jy).leap == 0

    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isLeapYear(jy) -> 30
        else -> 29
    }

    fun fromEpochMillis(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): JalaliDate {
        val calendar = GregorianCalendar(timeZone)
        calendar.timeInMillis = millis
        return fromGregorian(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun toEpochMillis(date: JalaliDate, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val (gy, gm, gd) = toGregorian(date)
        val calendar = GregorianCalendar(timeZone)
        calendar.clear()
        calendar.set(gy, gm - 1, gd, 0, 0, 0)
        return calendar.timeInMillis
    }

    /** ساعت و دقیقه با ارقام لاتین، مثال: 14:05 */
    fun formatTime(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val calendar = GregorianCalendar(timeZone)
        calendar.timeInMillis = millis
        return String.format(
            java.util.Locale.US,
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
        )
    }

    /** ابتدای امروز به میلی‌ثانیه. */
    fun startOfToday(now: Long = System.currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Long {
        val calendar = GregorianCalendar(timeZone)
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** ابتدای ماه جاری شمسی به میلی‌ثانیه. */
    fun startOfJalaliMonth(now: Long = System.currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Long {
        val today = fromEpochMillis(now, timeZone)
        return toEpochMillis(JalaliDate(today.year, today.month, 1), timeZone)
    }
}
