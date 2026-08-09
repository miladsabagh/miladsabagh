package ir.zarin.faktor.core

import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliCalendarTest {

    private val tehran: TimeZone = TimeZone.getTimeZone("Asia/Tehran")

    @Test
    fun `آغاز سال های شمسی به درستی تبدیل می شود`() {
        assertEquals(JalaliDate(1403, 1, 1), JalaliCalendar.fromGregorian(2024, 3, 20))
        assertEquals(JalaliDate(1404, 1, 1), JalaliCalendar.fromGregorian(2025, 3, 21))
        assertEquals(JalaliDate(1405, 1, 1), JalaliCalendar.fromGregorian(2026, 3, 21))
    }

    @Test
    fun `تاریخ میانه سال به درستی تبدیل می شود`() {
        assertEquals(JalaliDate(1405, 5, 18), JalaliCalendar.fromGregorian(2026, 8, 9))
        assertEquals(JalaliDate(1403, 10, 11), JalaliCalendar.fromGregorian(2024, 12, 31))
    }

    @Test
    fun `تبدیل معکوس به میلادی درست است`() {
        assertEquals(Triple(2024, 3, 20), JalaliCalendar.toGregorian(JalaliDate(1403, 1, 1)))
        assertEquals(Triple(2026, 8, 9), JalaliCalendar.toGregorian(JalaliDate(1405, 5, 18)))
    }

    @Test
    fun `تبدیل رفت و برگشت برای ده سال پایدار است`() {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        calendar.set(2020, 0, 1)
        repeat(3653) {
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val jalali = JalaliCalendar.fromGregorian(year, month, day)
            assertEquals(Triple(year, month, day), JalaliCalendar.toGregorian(jalali))
            assertTrue("ماه نامعتبر: $jalali", jalali.month in 1..12)
            assertTrue("روز نامعتبر: $jalali", jalali.day in 1..JalaliCalendar.monthLength(jalali.year, jalali.month))

            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
    }

    @Test
    fun `سال کبیسه و طول ماه ها درست است`() {
        assertTrue(JalaliCalendar.isLeapYear(1403))
        assertEquals(31, JalaliCalendar.monthLength(1404, 1))
        assertEquals(30, JalaliCalendar.monthLength(1404, 8))
        assertEquals(30, JalaliCalendar.monthLength(1403, 12))
        assertEquals(29, JalaliCalendar.monthLength(1404, 12))
    }

    @Test
    fun `تبدیل از میلی ثانیه با منطقه زمانی تهران انجام می شود`() {
        val calendar = GregorianCalendar(tehran)
        calendar.clear()
        calendar.set(2026, 7, 9, 14, 5, 0)

        assertEquals(JalaliDate(1405, 5, 18), JalaliCalendar.fromEpochMillis(calendar.timeInMillis, tehran))
        assertEquals("14:05", JalaliCalendar.formatTime(calendar.timeInMillis, tehran))
    }

    @Test
    fun `قالب بندی تاریخ شامل نام ماه است`() {
        val date = JalaliDate(1405, 5, 18)
        assertEquals("1405/05/18", date.format())
        assertEquals("18 مرداد 1405", date.formatLong())
    }

    @Test
    fun `ابتدای ماه شمسی محاسبه می شود`() {
        val now = JalaliCalendar.toEpochMillis(JalaliDate(1405, 5, 18), tehran) + 10 * 3_600_000L
        val startOfMonth = JalaliCalendar.startOfJalaliMonth(now, tehran)

        assertEquals(JalaliDate(1405, 5, 1), JalaliCalendar.fromEpochMillis(startOfMonth, tehran))
        assertTrue(startOfMonth < now)
    }
}
