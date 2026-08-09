package com.zarnegar.gold.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * تاریخ شمسی (هجری خورشیدی)
 *
 * Conversion follows the arithmetic Jalali calendar rules used by the Iranian
 * civil calendar (33-year cycle with the Birashk/Borkowski break table), which is
 * exact for the years 1178–3177 (Jalali).
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {

    val monthName: String get() = MONTH_NAMES[month - 1]

    /** e.g. `۱۴۰۳/۰۵/۲۱` */
    fun formatNumeric(persianDigits: Boolean = true): String {
        val raw = "%04d/%02d/%02d".format(year, month, day)
        return if (persianDigits) PersianText.toPersianDigits(raw) else raw
    }

    /** e.g. `۲۱ مرداد ۱۴۰۳` */
    fun formatLong(persianDigits: Boolean = true): String {
        val raw = "$day $monthName $year"
        return if (persianDigits) PersianText.toPersianDigits(raw) else raw
    }

    fun toGregorian(): LocalDate {
        val jdn = jalaliToJdn(year, month, day)
        val (gy, gm, gd) = jdnToGregorian(jdn)
        return LocalDate.of(gy, gm, gd)
    }

    override fun compareTo(other: JalaliDate): Int {
        year.compareTo(other.year).let { if (it != 0) return it }
        month.compareTo(other.month).let { if (it != 0) return it }
        return day.compareTo(other.day)
    }

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )

        val WEEK_DAY_NAMES = listOf(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه",
        )

        private val BREAKS = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
        )

        fun fromGregorian(date: LocalDate): JalaliDate {
            val jdn = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
            return jdnToJalali(jdn)
        }

        fun fromEpochMillis(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): JalaliDate =
            fromGregorian(Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate())

        fun now(zone: ZoneId = ZoneId.systemDefault()): JalaliDate =
            fromGregorian(LocalDate.now(zone))

        /** Day-of-week label for a moment in time, e.g. `چهارشنبه`. */
        fun weekDayName(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
            val dow = Instant.ofEpochMilli(epochMillis).atZone(zone).dayOfWeek.value // Mon=1..Sun=7
            // Persian week starts on Saturday.
            return WEEK_DAY_NAMES[(dow + 1) % 7]
        }

        /** e.g. `۱۴:۰۵` */
        fun formatTime(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
            val t: LocalDateTime = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()
            return PersianText.toPersianDigits("%02d:%02d".format(t.hour, t.minute))
        }

        fun isLeapYear(jalaliYear: Int): Boolean = jalCal(jalaliYear).leap == 0

        fun daysInMonth(jalaliYear: Int, month: Int): Int = when {
            month <= 6 -> 31
            month <= 11 -> 30
            isLeapYear(jalaliYear) -> 30
            else -> 29
        }

        private data class JalCal(val leap: Int, val gy: Int, val march: Int)

        private fun jalCal(jy: Int): JalCal {
            require(jy >= BREAKS.first() && jy < BREAKS.last()) { "سال شمسی نامعتبر: $jy" }
            val gy = jy + 621
            var leapJ = -14
            var jp = BREAKS[0]
            var jump = 0
            for (i in 1 until BREAKS.size) {
                val jm = BREAKS[i]
                jump = jm - jp
                if (jy < jm) break
                leapJ += (jump / 33) * 8 + (jump % 33) / 4
                jp = jm
            }
            var n = jy - jp
            leapJ += (n / 33) * 8 + (n % 33 + 3) / 4
            if (jump % 33 == 4 && jump - n == 4) leapJ += 1

            val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
            val march = 20 + leapJ - leapG

            if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
            var leap = (((n + 1) % 33) - 1) % 4
            if (leap == -1) leap = 4
            return JalCal(leap, gy, march)
        }

        internal fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Int {
            val r = jalCal(jy)
            return gregorianToJdn(r.gy, 3, r.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
        }

        internal fun jdnToJalali(jdn: Int): JalaliDate {
            val (gy, _, _) = jdnToGregorian(jdn)
            var jy = gy - 621
            val r = jalCal(jy)
            val firstFarvardinJdn = gregorianToJdn(r.gy, 3, r.march)
            var k = jdn - firstFarvardinJdn
            if (k >= 0) {
                if (k <= 185) {
                    return JalaliDate(jy, 1 + k / 31, k % 31 + 1)
                }
                k -= 186
            } else {
                jy -= 1
                k += 179
                if (r.leap == 1) k += 1
            }
            return JalaliDate(jy, 7 + k / 30, k % 30 + 1)
        }

        internal fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Int {
            var d = ((gy + (gm - 8) / 6 + 100100) * 1461) / 4 +
                (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
            d -= (((gy + 100100 + (gm - 8) / 6) / 100) * 3) / 4 - 752
            return d
        }

        internal fun jdnToGregorian(jdn: Int): Triple<Int, Int, Int> {
            var j = 4 * jdn + 139361631
            j += ((((4 * jdn + 183187720) / 146097) * 3) / 4) * 4 - 3908
            val i = ((j % 1461) / 4) * 5 + 308
            val gd = ((i % 153) / 5) + 1
            val gm = ((i / 153) % 12) + 1
            val gy = (j / 1461) - 100100 + (8 - gm) / 6
            return Triple(gy, gm, gd)
        }
    }
}
