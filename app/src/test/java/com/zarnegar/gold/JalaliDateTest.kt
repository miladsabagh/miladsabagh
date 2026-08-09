package com.zarnegar.gold

import com.zarnegar.gold.core.JalaliDate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliDateTest {

    @Test
    fun `converts known gregorian dates to jalali`() {
        assertEquals(
            JalaliDate(1403, 5, 21),
            JalaliDate.fromGregorian(LocalDate.of(2024, 8, 11)),
        )
        assertEquals(
            JalaliDate(1399, 1, 1),
            JalaliDate.fromGregorian(LocalDate.of(2020, 3, 20)),
        )
        assertEquals(
            JalaliDate(1402, 12, 29),
            JalaliDate.fromGregorian(LocalDate.of(2024, 3, 19)),
        )
        assertEquals(
            JalaliDate(1404, 1, 1),
            JalaliDate.fromGregorian(LocalDate.of(2025, 3, 21)),
        )
    }

    @Test
    fun `round trips every day across a decade`() {
        var date = LocalDate.of(2018, 1, 1)
        val end = LocalDate.of(2028, 1, 1)
        while (date.isBefore(end)) {
            val jalali = JalaliDate.fromGregorian(date)
            assertEquals(date, jalali.toGregorian())
            date = date.plusDays(1)
        }
    }

    @Test
    fun `identifies leap years`() {
        assertTrue(JalaliDate.isLeapYear(1403))
        assertFalse(JalaliDate.isLeapYear(1404))
        assertTrue(JalaliDate.isLeapYear(1399))
    }

    @Test
    fun `reports days in month`() {
        assertEquals(31, JalaliDate.daysInMonth(1403, 1))
        assertEquals(30, JalaliDate.daysInMonth(1403, 7))
        assertEquals(30, JalaliDate.daysInMonth(1403, 12)) // سال کبیسه
        assertEquals(29, JalaliDate.daysInMonth(1404, 12))
    }

    @Test
    fun `formats dates in persian`() {
        val date = JalaliDate(1403, 5, 21)
        assertEquals("۱۴۰۳/۰۵/۲۱", date.formatNumeric())
        assertEquals("۲۱ مرداد ۱۴۰۳", date.formatLong())
        assertEquals("مرداد", date.monthName)
    }

    @Test
    fun `orders dates chronologically`() {
        val list = listOf(
            JalaliDate(1403, 12, 1),
            JalaliDate(1402, 1, 1),
            JalaliDate(1403, 1, 15),
            JalaliDate(1403, 1, 2),
        ).sorted()

        assertEquals(
            listOf(
                JalaliDate(1402, 1, 1),
                JalaliDate(1403, 1, 2),
                JalaliDate(1403, 1, 15),
                JalaliDate(1403, 12, 1),
            ),
            list,
        )
    }
}
