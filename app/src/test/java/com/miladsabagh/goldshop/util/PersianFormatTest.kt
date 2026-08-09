package com.miladsabagh.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PersianFormatTest {

    @Test
    fun `toPersianDigits converts ascii digits only`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianFormat.toPersianDigits("1234567890"))
        assertEquals("قیمت: ۵۰۰ تومان", PersianFormat.toPersianDigits("قیمت: 500 تومان"))
    }

    @Test
    fun `toEnglishDigits reverses toPersianDigits`() {
        val original = "1403/05/19"
        val persian = PersianFormat.toPersianDigits(original)
        assertEquals(original, PersianFormat.toEnglishDigits(persian))
    }

    @Test
    fun `formatNumber inserts thousands separators`() {
        val formatted = PersianFormat.formatNumber(1_234_567L)
        assertEquals("۱٬۲۳۴٬۵۶۷", formatted)
    }

    @Test
    fun `formatToman appends the currency unit`() {
        val formatted = PersianFormat.formatToman(1_000.0)
        assertEquals("۱٬۰۰۰ تومان", formatted)
    }

    @Test
    fun `formatWeight renders three decimal places with the gram unit`() {
        val formatted = PersianFormat.formatWeight(1.5, persianDigitsOutput = false)
        assertEquals("1.500 گرم", formatted)
    }
}
