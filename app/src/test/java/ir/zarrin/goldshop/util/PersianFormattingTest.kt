package ir.zarrin.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersianFormattingTest {

    @Test
    fun `ارقام لاتین به فارسی تبدیل می شوند`() {
        assertEquals("۱۴۰۳", "1403".toPersianDigits())
        assertEquals("کد ۱۲۳-A", "کد 123-A".toPersianDigits())
    }

    @Test
    fun `ارقام فارسی و عربی به لاتین تبدیل می شوند`() {
        assertEquals("1403", "۱۴۰۳".toLatinDigits())
        assertEquals("1403", "١٤٠٣".toLatinDigits())
    }

    @Test
    fun `جداسازی سه رقمی مبالغ`() {
        assertEquals("۱٬۲۳۴٬۵۶۷", 1_234_567L.groupDigits())
        assertEquals("۵۰۰", 500L.groupDigits())
        assertEquals("۰", 0L.groupDigits())
        assertEquals("12,500,000".replace(',', '٬'), 12_500_000L.groupDigits(persianDigits = false))
        assertEquals("-۱٬۰۰۰", (-1000L).groupDigits())
    }

    @Test
    fun `نمایش وزن با حداکثر سه رقم اعشار`() {
        assertEquals("۴٫۲۵", 4.25.formatWeight())
        assertEquals("۳", 3.0.formatWeight())
        assertEquals("۸٫۱۳۳", 8.133.formatWeight())
    }

    @Test
    fun `نمایش درصد`() {
        assertEquals("۹٪", 9.0.formatPercent())
        assertEquals("۷٫۵٪", 7.5.formatPercent())
    }

    @Test
    fun `خواندن مبلغ از ورودی کاربر`() {
        assertEquals(1234567L, "۱٬۲۳۴٬۵۶۷".parseAmountOrNull())
        assertEquals(6850000L, "6,850,000".parseAmountOrNull())
        assertNull("".parseAmountOrNull())
        assertNull("بدون عدد".parseAmountOrNull())
    }

    @Test
    fun `خواندن عدد اعشاری از ورودی کاربر`() {
        assertEquals(4.25, "۴٫۲۵".parseDecimalOrNull()!!, 0.0001)
        assertEquals(3.0, "3".parseDecimalOrNull()!!, 0.0001)
        assertNull("abc".parseDecimalOrNull())
    }
}
