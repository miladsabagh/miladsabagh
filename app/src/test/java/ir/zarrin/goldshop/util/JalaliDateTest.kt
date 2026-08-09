package ir.zarrin.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliDateTest {

    @Test
    fun `تبدیل میلادی به شمسی برای تاریخ های شناخته شده`() {
        assertEquals(
            JalaliDate(1403, 1, 1),
            JalaliDate.fromGregorian(GregorianDate(2024, 3, 20))
        )
        assertEquals(
            JalaliDate(1357, 11, 22),
            JalaliDate.fromGregorian(GregorianDate(1979, 2, 11))
        )
        assertEquals(
            JalaliDate(1399, 12, 30),
            JalaliDate.fromGregorian(GregorianDate(2021, 3, 20))
        )
    }

    @Test
    fun `تبدیل شمسی به میلادی برای تاریخ های شناخته شده`() {
        assertEquals(
            GregorianDate(2024, 3, 20),
            JalaliDate.toGregorian(JalaliDate(1403, 1, 1))
        )
        assertEquals(
            GregorianDate(1979, 2, 11),
            JalaliDate.toGregorian(JalaliDate(1357, 11, 22))
        )
    }

    @Test
    fun `تبدیل رفت و برگشت برای بازه ای از روزها پایدار است`() {
        var year = 2020
        var month = 1
        var day = 1
        repeat(400) {
            val gregorian = GregorianDate(year, month, day)
            val jalali = JalaliDate.fromGregorian(gregorian)
            assertEquals(gregorian, JalaliDate.toGregorian(jalali))

            day += 3
            if (day > 28) {
                day -= 28
                month += 1
                if (month > 12) {
                    month = 1
                    year += 1
                }
            }
        }
    }

    @Test
    fun `سال کبیسه شمسی درست تشخیص داده می شود`() {
        assertTrue(JalaliDate.isLeapYear(1399))
        assertTrue(JalaliDate.isLeapYear(1403))
        assertTrue(JalaliDate.isLeapYear(1408))
        assertFalse(JalaliDate.isLeapYear(1402))
        assertFalse(JalaliDate.isLeapYear(1404))
    }

    @Test
    fun `تعداد روزهای ماه های شمسی درست است`() {
        assertEquals(31, JalaliDate.monthLength(1403, 1))
        assertEquals(31, JalaliDate.monthLength(1403, 6))
        assertEquals(30, JalaliDate.monthLength(1403, 7))
        assertEquals(30, JalaliDate.monthLength(1403, 11))
        assertEquals(30, JalaliDate.monthLength(1403, 12))
        assertEquals(29, JalaliDate.monthLength(1402, 12))
    }

    @Test
    fun `قالب بندی تاریخ با ارقام فارسی انجام می شود`() {
        val date = JalaliDate(1403, 5, 19)
        assertEquals("۱۴۰۳/۰۵/۱۹", date.formatted())
        assertEquals("1403/05/19", date.formatted(persianDigits = false))
        assertEquals("۱۹ مرداد ۱۴۰۳", date.formattedLong())
    }

    @Test
    fun `نام ماه های شمسی درست برگردانده می شود`() {
        assertEquals("فروردین", JalaliDate.monthName(1))
        assertEquals("اسفند", JalaliDate.monthName(12))
        assertEquals(12, JalaliDate.monthNames().size)
    }
}
