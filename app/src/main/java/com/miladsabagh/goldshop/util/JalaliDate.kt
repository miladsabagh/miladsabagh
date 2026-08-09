package com.miladsabagh.goldshop.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Minimal Gregorian -> Jalali (Persian/Shamsi) calendar converter.
 * No external dependency required.
 */
object JalaliDate {

    private val persianMonthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    data class YMD(val year: Int, val month: Int, val day: Int)

    fun fromEpochMillis(epochMillis: Long): YMD {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = epochMillis
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): YMD {
        // Cumulative days elapsed *before* the start of each Gregorian month (non-leap year basis).
        val gDaysBeforeMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gDaysBeforeMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
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
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return YMD(jy, jm, jd)
    }

    fun formatFull(epochMillis: Long, persianDigits: Boolean = true): String {
        val ymd = fromEpochMillis(epochMillis)
        val monthName = persianMonthNames[(ymd.month - 1).coerceIn(0, 11)]
        val raw = "${ymd.day} $monthName ${ymd.year}"
        return if (persianDigits) PersianFormat.toPersianDigits(raw) else raw
    }

    fun formatNumeric(epochMillis: Long, persianDigits: Boolean = true): String {
        val ymd = fromEpochMillis(epochMillis)
        val raw = String.format(Locale.US, "%04d/%02d/%02d", ymd.year, ymd.month, ymd.day)
        return if (persianDigits) PersianFormat.toPersianDigits(raw) else raw
    }
}
