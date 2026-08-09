package com.goldgallery.app

import com.goldgallery.app.logic.JalaliDate
import com.goldgallery.app.logic.PersianFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class JalaliDateTest {

    @Test
    fun `nowruz is first of farvardin`() {
        val date = JalaliDate.fromGregorian(2024, 3, 20)
        assertEquals(JalaliDate.JDate(1403, 1, 1), date)
    }

    @Test
    fun `day before nowruz is last of esfand`() {
        val date = JalaliDate.fromGregorian(2024, 3, 19)
        assertEquals(JalaliDate.JDate(1402, 12, 29), date)
    }

    @Test
    fun `summer date converts correctly`() {
        val date = JalaliDate.fromGregorian(2026, 8, 9)
        assertEquals(JalaliDate.JDate(1405, 5, 18), date)
    }

    @Test
    fun `leap jalali year has 30 esfand`() {
        // ۱۴۰۳ کبیسه است؛ اسفند ۳۰ روز دارد
        val date = JalaliDate.fromGregorian(2025, 3, 20)
        assertEquals(JalaliDate.JDate(1403, 12, 30), date)
    }

    @Test
    fun `date formats as yyyy slash mm slash dd`() {
        assertEquals("1403/01/01", JalaliDate.JDate(1403, 1, 1).toString())
    }
}

class PersianFormatTest {

    @Test
    fun `digits convert to persian`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianFormat.toPersianDigits("1234567890"))
    }

    @Test
    fun `persian digits convert back to english`() {
        assertEquals("1403", PersianFormat.toEnglishDigits("۱۴۰۳"))
    }

    @Test
    fun `money groups thousands and appends toman`() {
        assertEquals("۱٬۲۳۴٬۵۶۷ تومان", PersianFormat.money(1_234_567))
    }

    @Test
    fun `weight trims trailing zeros`() {
        assertEquals("۴٫۲ گرم", PersianFormat.weight(4.2))
        assertEquals("۸٫۱۳۳ گرم", PersianFormat.weight(8.133))
    }
}
