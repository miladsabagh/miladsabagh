package com.zarrin.goldshop.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GoldPricingTest {

    @Test
    fun `equivalent weight converts karat 24 to 18`() {
        val eq = GoldPricing.equivalentWeight18(10.0, 24)
        assertEquals(13.333, eq, 0.001)
    }

    @Test
    fun `line total includes making fee`() {
        // 5g of 18k, price 1_000_000 -> base 5_000_000, fee 10% -> 5_500_000
        val total = GoldPricing.lineTotal(
            weightGrams = 5.0,
            karat = 18,
            makingFeePercent = 10.0,
            goldPrice18PerGram = 1_000_000L
        )
        assertEquals(5_500_000L, total)
    }

    @Test
    fun `invoice totals apply profit and vat`() {
        val totals = GoldPricing.invoiceTotals(
            itemLineTotals = listOf(1_000_000L, 500_000L),
            profitPercent = 10.0,
            vatPercent = 10.0
        )
        assertEquals(1_500_000L, totals.subtotal)
        assertEquals(150_000L, totals.profitAmount)
        assertEquals(165_000L, totals.vatAmount)
        assertEquals(1_815_000L, totals.totalAmount)
    }

    @Test
    fun `persian digits conversion works`() {
        assertEquals("۱۲۳۴", toPersianDigits("1234"))
    }
}
