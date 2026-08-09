package ir.zarrin.goldshop

import ir.zarrin.goldshop.core.JalaliDate
import ir.zarrin.goldshop.core.PersianCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianCalendarTest {

    @Test
    fun `converts known Gregorian dates to the Solar Hijri calendar`() {
        assertEquals(JalaliDate(1403, 5, 19), PersianCalendar.fromGregorian(2024, 8, 9))
        assertEquals(JalaliDate(1403, 1, 1), PersianCalendar.fromGregorian(2024, 3, 20))
        assertEquals(JalaliDate(1402, 1, 1), PersianCalendar.fromGregorian(2023, 3, 21))
        assertEquals(JalaliDate(1399, 1, 1), PersianCalendar.fromGregorian(2020, 3, 20))
        assertEquals(JalaliDate(1402, 12, 29), PersianCalendar.fromGregorian(2024, 3, 19))
    }

    @Test
    fun `converts Solar Hijri dates back to Gregorian`() {
        assertEquals(Triple(2024, 8, 9), PersianCalendar.toGregorian(JalaliDate(1403, 5, 19)))
        assertEquals(Triple(2024, 3, 20), PersianCalendar.toGregorian(JalaliDate(1403, 1, 1)))
        assertEquals(Triple(2021, 3, 21), PersianCalendar.toGregorian(JalaliDate(1400, 1, 1)))
    }

    @Test
    fun `round trips every day across four years`() {
        val start = PersianCalendar.jalaliToJdn(1400, 1, 1)
        val end = PersianCalendar.jalaliToJdn(1404, 1, 1)
        var jdn = start
        while (jdn <= end) {
            val jalali = PersianCalendar.jdnToJalali(jdn)
            assertEquals(jdn, PersianCalendar.jalaliToJdn(jalali.year, jalali.month, jalali.day))
            jdn += 1
        }
    }

    @Test
    fun `knows which years are leap years`() {
        assertTrue(PersianCalendar.isLeapYear(1403))
        assertFalse(PersianCalendar.isLeapYear(1404))
        assertTrue(PersianCalendar.isLeapYear(1399))
    }

    @Test
    fun `month lengths follow the 31-30-29 pattern`() {
        assertEquals(31, PersianCalendar.monthLength(1403, 1))
        assertEquals(31, PersianCalendar.monthLength(1403, 6))
        assertEquals(30, PersianCalendar.monthLength(1403, 7))
        assertEquals(30, PersianCalendar.monthLength(1403, 12))
        assertEquals(29, PersianCalendar.monthLength(1404, 12))
    }

    @Test
    fun `formats dates with Persian digits and month names`() {
        val date = JalaliDate(1403, 5, 19)
        assertEquals("۱۴۰۳/۰۵/۱۹", date.formatNumeric())
        assertEquals("۱۹ مرداد ۱۴۰۳", date.formatLong())
    }
}
