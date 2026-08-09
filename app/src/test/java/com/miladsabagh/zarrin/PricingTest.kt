package com.miladsabagh.zarrin

import com.miladsabagh.zarrin.util.Pricing
import com.miladsabagh.zarrin.util.parseAmountToLong
import com.miladsabagh.zarrin.util.parseWeightToDouble
import com.miladsabagh.zarrin.util.priceLine
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingTest {

    @Test
    fun `purity mapping matches karat standards`() {
        assertEquals(750, Pricing.purityOf(18))
        assertEquals(875, Pricing.purityOf(21))
        assertEquals(916, Pricing.purityOf(22))
        assertEquals(999, Pricing.purityOf(24))
    }

    @Test
    fun `unit price scales with karat purity`() {
        // 3,000,000 per gram of 18k => 24k costs 999/750 of that
        assertEquals(3_000_000L, Pricing.goldUnitPrice(3_000_000L, 18))
        assertEquals(3_996_000L, Pricing.goldUnitPrice(3_000_000L, 24))
        assertEquals(3_500_000L, Pricing.goldUnitPrice(3_000_000L, 21))
    }

    @Test
    fun `full line breakdown follows iranian gold shop rules`() {
        // 2 grams of 18k at 3,000,000/g, wage 500,000/g, 7% profit, 9% VAT
        val line = priceLine(
            basePrice18kPerGram = 3_000_000L,
            karat = 18,
            weightGrams = 2.0,
            wagePerGram = 500_000L,
            profitPercent = 7.0,
            taxPercent = 9.0
        )
        assertEquals(3_000_000L, line.unitGoldPrice)
        assertEquals(6_000_000L, line.goldValue)
        assertEquals(1_000_000L, line.wageAmount)
        // profit = 7% of (6,000,000 + 1,000,000) = 490,000
        assertEquals(490_000L, line.profitAmount)
        // VAT = 9% of (wage + profit) = 9% of 1,490,000 = 134,100
        assertEquals(134_100L, line.taxAmount)
        assertEquals(7_624_100L, line.lineTotal)
    }

    @Test
    fun `vat excludes raw gold value`() {
        val line = priceLine(
            basePrice18kPerGram = 4_000_000L,
            karat = 18,
            weightGrams = 1.0,
            wagePerGram = 0L,
            profitPercent = 0.0,
            taxPercent = 9.0
        )
        // No wage and no profit => no VAT on pure gold value
        assertEquals(0L, line.taxAmount)
        assertEquals(4_000_000L, line.lineTotal)
    }

    @Test
    fun `persian and arabic digit amounts parse correctly`() {
        assertEquals(1_250_000L, "1,250,000".parseAmountToLong())
        assertEquals(1_250_000L, "۱٬۲۵۰٬۰۰۰".parseAmountToLong())
        assertEquals(1_250_000L, "۱۲۵۰۰۰۰".parseAmountToLong())
        assertEquals(1_250_000L, "١٢٥٠٠٠٠".parseAmountToLong())
        assertEquals(null, "abc".parseAmountToLong())
    }

    @Test
    fun `weights parse with persian decimal separator`() {
        assertEquals(2.35, "2.35".parseWeightToDouble()!!, 0.0001)
        assertEquals(2.35, "۲٫۳۵".parseWeightToDouble()!!, 0.0001)
        assertEquals(8.133, "۸٫۱۳۳".parseWeightToDouble()!!, 0.0001)
        assertEquals(null, "abc".parseWeightToDouble())
    }
}
