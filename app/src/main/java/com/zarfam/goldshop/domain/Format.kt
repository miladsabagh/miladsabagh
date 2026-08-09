package com.zarfam.goldshop.domain

import java.util.Calendar
import java.util.Locale

private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

fun String.toPersianDigits(): String = buildString {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') PERSIAN_DIGITS[ch - '0'] else ch)
    }
}

/** Converts Persian/Arabic digits to Latin so numeric input can be parsed. */
fun String.toEnglishDigits(): String = buildString {
    for (ch in this@toEnglishDigits) {
        append(
            when (ch) {
                in '۰'..'۹' -> ('0' + (ch - '۰'))
                in '٠'..'٩' -> ('0' + (ch - '٠'))
                '٫' -> '.'
                else -> ch
            }
        )
    }
}

fun String.parseMoney(): Long? = toEnglishDigits().replace(",", "").replace("٬", "").trim().toLongOrNull()

fun String.parseDecimal(): Double? = toEnglishDigits().replace(",", ".").trim().toDoubleOrNull()

/** e.g. 1234567 -> "۱٬۲۳۴٬۵۶۷" */
fun Long.toMoney(): String = String.format(Locale.US, "%,d", this).replace(",", "٬").toPersianDigits()

fun Long.toMoneyToman(): String = "${toMoney()} تومان"

/** Trims trailing zeros: 3.500 -> "۳٫۵" , 3.0 -> "۳" */
fun Double.formatWeight(): String {
    val s = String.format(Locale.US, "%.3f", this).trimEnd('0').trimEnd('.')
    return s.replace(".", "٫").toPersianDigits()
}

object JalaliDate {

    /** Returns [jy, jm, jd] for the given Gregorian date. */
    fun fromGregorian(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gdm[gm - 1]
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
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return intArrayOf(jy, jm, jd)
    }

    fun format(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val (jy, jm, jd) = fromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
        return String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd).toPersianDigits()
    }

    fun formatWithTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val time = String.format(
            Locale.US, "%02d:%02d",
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
        ).toPersianDigits()
        return "${format(millis)} - $time"
    }

    private operator fun IntArray.component1() = this[0]
    private operator fun IntArray.component2() = this[1]
    private operator fun IntArray.component3() = this[2]
}
