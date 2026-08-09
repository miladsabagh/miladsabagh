package ir.zarrin.gold

import ir.zarrin.gold.util.JalaliDate
import ir.zarrin.gold.util.PersianFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersianFormatTest {

    @Test
    fun `numbers convert to persian digits with grouping`() {
        assertEquals("۱٬۲۳۴٬۵۶۷", PersianFormat.formatNumber(1_234_567))
        assertEquals("۰", PersianFormat.formatNumber(0))
        assertEquals("۵٬۰۰۰٬۰۰۰ تومان", PersianFormat.formatCurrency(5_000_000))
    }

    @Test
    fun `weight formatting drops trailing zeros`() {
        assertEquals("۴.۵ گرم", PersianFormat.formatWeight(4.5))
        assertEquals("۳ گرم", PersianFormat.formatWeight(3.0))
        assertEquals("۲.۷۵ گرم", PersianFormat.formatWeight(2.750))
    }

    @Test
    fun `persian and arabic digits parse correctly`() {
        assertEquals(4.5, PersianFormat.parseDouble("۴٫۵")!!, 0.0001)
        assertEquals(4.5, PersianFormat.parseDouble("4.5")!!, 0.0001)
        assertEquals(1_500_000L, PersianFormat.parseLong("۱٬۵۰۰٬۰۰۰"))
        assertEquals(1_500_000L, PersianFormat.parseLong("1,500,000"))
        assertNull(PersianFormat.parseLong("abc"))
    }

    @Test
    fun `gregorian to jalali conversion`() {
        // ۹ اوت ۲۰۲۶ = ۱۸ مرداد ۱۴۰۵
        assertEquals(listOf(1405, 5, 18), JalaliDate.fromGregorian(2026, 8, 9).toList())
        // ۲۱ مارس ۲۰۲۵ = ۱ فروردین ۱۴۰۴ (نوروز)
        assertEquals(listOf(1404, 1, 1), JalaliDate.fromGregorian(2025, 3, 21).toList())
        // ۲۰ مارس ۲۰۲۵ = ۳۰ اسفند ۱۴۰۳
        assertEquals(listOf(1403, 12, 30), JalaliDate.fromGregorian(2025, 3, 20).toList())
    }
}
