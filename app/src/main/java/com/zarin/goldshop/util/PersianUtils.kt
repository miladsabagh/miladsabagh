package com.zarin.goldshop.util

import java.util.Calendar
import java.util.Date

/**
 * Helpers for Persian (Jalali) dates, digit conversion and currency formatting.
 * Amounts across the app are stored as whole Toman (تومان) values.
 */
object PersianUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    private val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(if (c in '0'..'9') persianDigits[c - '0'] else c)
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            val idx = persianDigits.indexOf(c)
            when {
                idx >= 0 -> sb.append(('0' + idx))
                c in "٠١٢٣٤٥٦٧٨٩" -> sb.append(('0' + "٠١٢٣٤٥٦٧٨٩".indexOf(c)))
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /** Groups an amount with thousands separators and renders Persian digits. */
    fun formatAmount(value: Long): String {
        val negative = value < 0
        val digits = kotlin.math.abs(value).toString()
        val sb = StringBuilder()
        val len = digits.length
        for (i in 0 until len) {
            if (i > 0 && (len - i) % 3 == 0) sb.append('٬')
            sb.append(digits[i])
        }
        val grouped = (if (negative) "-" else "") + sb.toString()
        return toPersianDigits(grouped)
    }

    fun formatToman(value: Long): String = formatAmount(value) + " تومان"

    fun formatWeight(grams: Double): String {
        val s = if (grams == grams.toLong().toDouble()) grams.toLong().toString()
        else String.format("%.3f", grams)
        return toPersianDigits(s)
    }

    fun formatNumber(value: Double): String {
        val s = if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format("%.2f", value)
        return toPersianDigits(s)
    }

    data class JalaliDate(val year: Int, val month: Int, val day: Int)

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        val jm = i + 1
        val jd = jDayNo + 1
        return JalaliDate(jy, jm, jd)
    }

    fun formatDate(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.time = Date(millis)
        val j = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val text = "${j.year}/${"%02d".format(j.month)}/${"%02d".format(j.day)}"
        return toPersianDigits(text)
    }

    fun formatDateLong(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.time = Date(millis)
        val j = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return toPersianDigits("${j.day} ${monthNames[j.month - 1]} ${j.year}")
    }
}
