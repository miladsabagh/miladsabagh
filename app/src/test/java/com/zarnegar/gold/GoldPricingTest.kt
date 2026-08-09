package com.zarnegar.gold

import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.PricingSettings
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.WageMode
import com.zarnegar.gold.domain.pricing.GoldPricing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoldPricingTest {

    private val settings = PricingSettings(
        goldRatePerGram18k = 3_500_000,
        vatPercent = 10.0,
        vatOnStone = true,
        roundTo = 0,
    )

    @Test
    fun `rate per gram scales with karat`() {
        assertEquals(3_500_000, GoldPricing.ratePerGram(3_500_000, 750))
        assertEquals(4_274_667, GoldPricing.ratePerGram(3_500_000, 916))
        assertEquals(3_290_000, GoldPricing.ratePerGram(3_500_000, 705))
    }

    @Test
    fun `simple ring is priced with wage, profit and vat on wage plus profit`() {
        val item = SaleItem(
            title = "انگشتر",
            karat = 750,
            weightGrams = 4.0,
            wageMode = WageMode.PERCENT,
            wagePercent = 10.0,
            profitPercent = 7.0,
        )

        val line = GoldPricing.priceLine(item, settings)

        val goldValue = 4.0 * 3_500_000 // 14,000,000
        val wage = goldValue * 0.10 // 1,400,000
        val profit = (goldValue + wage) * 0.07 // 1,078,000
        val vat = (wage + profit) * 0.10 // 247,800

        assertEquals(goldValue.toLong(), line.unitGoldValue)
        assertEquals(wage.toLong(), line.unitWage)
        assertEquals(profit.toLong(), line.unitProfit)
        assertEquals(vat.toLong(), line.vat)
        assertEquals((goldValue + wage + profit + vat).toLong(), line.total)
    }

    @Test
    fun `raw gold value itself is never taxed`() {
        val item = SaleItem(
            title = "شمش",
            karat = 995,
            weightGrams = 10.0,
            wagePercent = 0.0,
            profitPercent = 0.0,
        )

        val line = GoldPricing.priceLine(item, settings)

        assertEquals(0L, line.vat)
        assertEquals(line.unitGoldValue, line.total)
    }

    @Test
    fun `stone weight is excluded from gold value but its price is added`() {
        val item = SaleItem(
            title = "انگشتر نگین‌دار",
            karat = 750,
            weightGrams = 5.0,
            stoneWeightGrams = 1.0,
            stoneValue = 20_000_000,
            wagePercent = 0.0,
            profitPercent = 0.0,
        )

        val line = GoldPricing.priceLine(item, settings)

        assertEquals(4.0, line.goldWeightGrams, 1e-9)
        assertEquals(14_000_000L, line.unitGoldValue)
        assertEquals(20_000_000L, line.unitStoneValue)
        // مالیات فقط روی ارزش سنگ چون اجرت و سود صفر است
        assertEquals(2_000_000L, line.vat)
        assertEquals(36_000_000L, line.total)
    }

    @Test
    fun `stone can be exempted from vat by settings`() {
        val item = SaleItem(
            title = "انگشتر نگین‌دار",
            karat = 750,
            weightGrams = 5.0,
            stoneWeightGrams = 1.0,
            stoneValue = 20_000_000,
        )

        val line = GoldPricing.priceLine(item, settings.copy(vatOnStone = false))

        assertEquals(0L, line.vat)
    }

    @Test
    fun `fixed price coin is exempt from vat when flagged`() {
        val coin = SaleItem(
            title = "سکه تمام",
            pricingMode = PricingMode.FIXED,
            fixedPrice = 92_500_000,
            vatExempt = true,
            quantity = 2,
        )

        val line = GoldPricing.priceLine(coin, settings)

        assertEquals(0L, line.vat)
        assertEquals(185_000_000L, line.total)
    }

    @Test
    fun `wage per gram mode multiplies wage by the gold weight`() {
        val item = SaleItem(
            title = "دستبند",
            karat = 750,
            weightGrams = 10.0,
            wageMode = WageMode.PER_GRAM,
            wagePerGram = 300_000,
            profitPercent = 0.0,
        )

        val line = GoldPricing.priceLine(item, settings)

        assertEquals(3_000_000L, line.unitWage)
    }

    @Test
    fun `quantity multiplies every component of the line`() {
        val item = SaleItem(
            title = "انگشتر",
            karat = 750,
            weightGrams = 4.0,
            wagePercent = 10.0,
            profitPercent = 7.0,
        )

        val single = GoldPricing.priceLine(item, settings)
        val triple = GoldPricing.priceLine(item.copy(quantity = 3), settings)

        assertEquals(single.grossBeforeTax * 3, triple.grossBeforeTax)
        assertEquals(single.vat * 3, triple.vat)
        assertEquals(single.total * 3, triple.total)
    }

    @Test
    fun `line discount reduces the taxable base proportionally`() {
        val item = SaleItem(
            title = "انگشتر",
            karat = 750,
            weightGrams = 4.0,
            wagePercent = 10.0,
            profitPercent = 7.0,
        )

        val full = GoldPricing.priceLine(item, settings)
        val discounted = GoldPricing.priceLine(
            item.copy(discount = full.grossBeforeTax / 2),
            settings,
        )

        assertEquals(full.vat / 2, discounted.vat)
        assertEquals(
            full.grossBeforeTax - full.grossBeforeTax / 2 + discounted.vat,
            discounted.total,
        )
    }

    @Test
    fun `invoice discount is spread across lines and totals stay consistent`() {
        val items = listOf(
            SaleItem(title = "الف", karat = 750, weightGrams = 3.0, wagePercent = 8.0, profitPercent = 7.0),
            SaleItem(title = "ب", karat = 750, weightGrams = 7.0, wagePercent = 12.0, profitPercent = 7.0),
            SaleItem(
                title = "سکه",
                pricingMode = PricingMode.FIXED,
                fixedPrice = 92_500_000,
                vatExempt = true,
            ),
        )

        val totals = GoldPricing.priceInvoice(items, settings, invoiceDiscount = 5_000_000)

        assertEquals(5_000_000L, totals.invoiceDiscount)
        assertEquals(5_000_000L, totals.lines.sumOf { it.discount })
        assertEquals(totals.lines.sumOf { it.total }, totals.netTotal)
        assertEquals(
            totals.grossBeforeTax - totals.discountTotal + totals.vatTotal,
            totals.netTotal,
        )
        assertEquals(totals.netTotal, totals.payable) // roundTo = 0
    }

    @Test
    fun `invoice discount never exceeds the invoice value`() {
        val items = listOf(
            SaleItem(title = "الف", karat = 750, weightGrams = 1.0),
        )

        val totals = GoldPricing.priceInvoice(items, settings, invoiceDiscount = 999_999_999)

        assertEquals(totals.grossBeforeTax, totals.invoiceDiscount)
        assertTrue(totals.payable >= 0)
    }

    @Test
    fun `payable is rounded to the configured unit`() {
        val items = listOf(
            SaleItem(
                title = "انگشتر",
                karat = 750,
                weightGrams = 4.137,
                wagePercent = 9.0,
                profitPercent = 7.0,
            ),
        )

        val totals = GoldPricing.priceInvoice(items, settings.copy(roundTo = 10_000))

        assertEquals(0L, totals.payable % 10_000)
        assertEquals(totals.payable - totals.netTotal, totals.roundingAdjustment)
        assertTrue(kotlin.math.abs(totals.roundingAdjustment) <= 5_000)
    }

    @Test
    fun `rounding helper picks the nearest multiple`() {
        assertEquals(1_240_000, GoldPricing.roundAmount(1_236_000, 10_000))
        assertEquals(1_230_000, GoldPricing.roundAmount(1_234_000, 10_000))
        assertEquals(1_234_567, GoldPricing.roundAmount(1_234_567, 0))
    }

    @Test
    fun `proportional allocation keeps the exact total`() {
        val allocation = GoldPricing.allocateProportionally(100, listOf(1, 1, 1))
        assertEquals(100L, allocation.sum())

        val skewed = GoldPricing.allocateProportionally(1_000_001, listOf(3, 5, 7))
        assertEquals(1_000_001L, skewed.sum())

        val emptyWeights = GoldPricing.allocateProportionally(500, listOf(0, 0))
        assertEquals(0L, emptyWeights.sum())
    }

    @Test
    fun `empty cart produces an empty invoice`() {
        val totals = GoldPricing.priceInvoice(emptyList(), settings)
        assertEquals(0L, totals.payable)
        assertTrue(totals.lines.isEmpty())
    }
}
