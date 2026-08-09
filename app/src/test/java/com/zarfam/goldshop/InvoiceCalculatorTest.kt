package com.zarfam.goldshop

import com.zarfam.goldshop.domain.InvoiceCalculator
import com.zarfam.goldshop.domain.JalaliDate
import com.zarfam.goldshop.domain.toEnglishDigits
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.domain.toPersianDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class InvoiceCalculatorTest {

    @Test
    fun `raw gold value equals weight times price for 18 karat`() {
        // 5g x 5,000,000 Toman, no wage/profit/tax
        val result = InvoiceCalculator.calculateLine(
            pricePerGram18 = 5_000_000,
            weightGrams = 5.0,
            karat = 18,
            wagePercent = 0.0,
            profitPercent = 0.0,
            taxPercent = 0.0,
        )
        assertEquals(25_000_000, result.rawGoldValue)
        assertEquals(25_000_000, result.lineTotal)
        assertEquals(0, result.wageAmount)
        assertEquals(0, result.profitAmount)
        assertEquals(0, result.taxAmount)
    }

    @Test
    fun `24 karat gold is priced at 24 over 18 of the base price`() {
        val result = InvoiceCalculator.calculateLine(
            pricePerGram18 = 3_000_000,
            weightGrams = 1.0,
            karat = 24,
            wagePercent = 0.0,
            profitPercent = 0.0,
            taxPercent = 0.0,
        )
        assertEquals(4_000_000, result.rawGoldValue)
    }

    @Test
    fun `standard market formula with wage profit and tax`() {
        // 10g @ 5,000,000/g, wage 10%, profit 7%, VAT 9%
        // raw    = 50,000,000
        // wage   = 5,000,000
        // profit = (50M + 5M) x 7% = 3,850,000
        // tax    = (5M + 3.85M) x 9% = 796,500
        // total  = 59,646,500
        val result = InvoiceCalculator.calculateLine(
            pricePerGram18 = 5_000_000,
            weightGrams = 10.0,
            karat = 18,
            wagePercent = 10.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
        )
        assertEquals(50_000_000, result.rawGoldValue)
        assertEquals(5_000_000, result.wageAmount)
        assertEquals(3_850_000, result.profitAmount)
        assertEquals(796_500, result.taxAmount)
        assertEquals(59_646_500, result.lineTotal)
    }

    @Test
    fun `quantity multiplies every component`() {
        val single = InvoiceCalculator.calculateLine(
            pricePerGram18 = 5_000_000,
            weightGrams = 2.0,
            karat = 18,
            wagePercent = 12.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            quantity = 1,
        )
        val triple = InvoiceCalculator.calculateLine(
            pricePerGram18 = 5_000_000,
            weightGrams = 2.0,
            karat = 18,
            wagePercent = 12.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            quantity = 3,
        )
        assertEquals(single.lineTotal * 3, triple.lineTotal)
        assertEquals(single.taxAmount * 3, triple.taxAmount)
    }

    @Test
    fun `grand total subtracts discount and never goes negative`() {
        assertEquals(9_000, InvoiceCalculator.grandTotal(10_000, 1_000))
        assertEquals(0, InvoiceCalculator.grandTotal(10_000, 20_000))
    }

    @Test
    fun `negative inputs are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            InvoiceCalculator.calculateLine(
                pricePerGram18 = -1,
                weightGrams = 1.0,
                karat = 18,
                wagePercent = 0.0,
                profitPercent = 0.0,
                taxPercent = 0.0,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            InvoiceCalculator.calculateLine(
                pricePerGram18 = 1,
                weightGrams = 1.0,
                karat = 18,
                wagePercent = 0.0,
                profitPercent = 0.0,
                taxPercent = 0.0,
                quantity = 0,
            )
        }
    }

    @Test
    fun `money formatting uses persian digits and separators`() {
        assertEquals("۱٬۲۳۴٬۵۶۷", 1_234_567L.toMoney())
        assertEquals("۰", 0L.toMoney())
    }

    @Test
    fun `digit conversion round trips`() {
        assertEquals("123456", "۱۲۳۴۵۶".toEnglishDigits())
        assertEquals("۱۲۳", "123".toPersianDigits())
    }

    @Test
    fun `jalali conversion matches known dates`() {
        // 2026-03-21 is Farvardin 1, 1405
        val nowruz = JalaliDate.fromGregorian(2026, 3, 21)
        assertEquals(1405, nowruz[0])
        assertEquals(1, nowruz[1])
        assertEquals(1, nowruz[2])

        // 2026-08-09 is Mordad 18, 1405
        val today = JalaliDate.fromGregorian(2026, 8, 9)
        assertEquals(1405, today[0])
        assertEquals(5, today[1])
        assertEquals(18, today[2])
    }
}
