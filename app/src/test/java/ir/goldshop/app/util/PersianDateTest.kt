package ir.goldshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PersianDateTest {

    private fun gregorianTimestamp(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(year, month - 1, day, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    @Test
    fun `converts well-known gregorian date to jalali - nowruz 1403`() {
        // ۲۰ مارس ۲۰۲۴ برابر است با ۱ فروردین ۱۴۰۳ (نوروز)
        val timestamp = gregorianTimestamp(2024, 3, 20)
        val persian = PersianDate.fromTimestamp(timestamp)

        assertEquals(1403, persian.year)
        assertEquals(1, persian.month)
        assertEquals(1, persian.day)
    }

    @Test
    fun `converts gregorian date to jalali - known date in mid-year`() {
        // ۲۲ سپتامبر ۲۰۲۴ برابر است با ۱ مهر ۱۴۰۳
        val timestamp = gregorianTimestamp(2024, 9, 22)
        val persian = PersianDate.fromTimestamp(timestamp)

        assertEquals(1403, persian.year)
        assertEquals(7, persian.month)
        assertEquals(1, persian.day)
    }

    @Test
    fun `converts gregorian date to jalali - end of persian year`() {
        // ۱۹ مارس ۲۰۲۴ برابر است با ۲۹ اسفند ۱۴۰۲ (سال ۱۴۰۲ کبیسه نیست)
        val timestamp = gregorianTimestamp(2024, 3, 19)
        val persian = PersianDate.fromTimestamp(timestamp)

        assertEquals(1402, persian.year)
        assertEquals(12, persian.month)
        assertEquals(29, persian.day)
    }
}
