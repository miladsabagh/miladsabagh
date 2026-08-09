package com.miladsabagh.goldinvoice.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberFormatUtilTest {

    @Test
    fun `toPersianDigits converts every ASCII digit`() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
        assertEquals("۱۲۵,۰۰۰ تومان", "125,000 تومان".toPersianDigits())
    }

    @Test
    fun `toEnglishDigits converts Persian and Arabic-Indic digits back to ASCII`() {
        assertEquals("125000", "۱۲۵۰۰۰".toEnglishDigits())
        assertEquals("125000", "١٢٥٠٠٠".toEnglishDigits())
    }

    @Test
    fun `formatCurrency groups thousands and renders Persian digits`() {
        assertEquals("۱,۲۵۰,۰۰۰", formatCurrency(1_250_000.0))
        assertEquals("۰", formatCurrency(0.0))
    }

    @Test
    fun `formatCurrency rounds to the nearest whole toman`() {
        assertEquals("۱,۰۰۰", formatCurrency(999.6))
    }

    @Test
    fun `parseLocalizedDouble understands Persian digits and thousands separators`() {
        assertEquals(1250000.5, parseLocalizedDouble("۱,۲۵۰,۰۰۰.۵")!!, 0.0001)
        assertEquals(18.0, parseLocalizedDouble("18")!!, 0.0001)
        assertNull(parseLocalizedDouble("abc"))
    }

    @Test
    fun `parseLocalizedInt understands Persian digits`() {
        assertEquals(1250000, parseLocalizedInt("۱,۲۵۰,۰۰۰"))
        assertNull(parseLocalizedInt(""))
    }
}
