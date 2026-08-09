package com.miladsabagh.goldinvoice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PriceCalculatorTest {

    private fun assertApprox(expected: Double, actual: Double, tolerance: Double = 0.001) {
        assertTrue(
            "expected=$expected actual=$actual",
            abs(expected - actual) <= tolerance
        )
    }

    @Test
    fun `pricePerGramForKarat scales linearly with karat relative to 18k reference`() {
        val goldPrice18k = 9_000_000.0

        assertApprox(9_000_000.0, PriceCalculator.pricePerGramForKarat(goldPrice18k, 18))
        assertApprox(12_000_000.0, PriceCalculator.pricePerGramForKarat(goldPrice18k, 24))
        assertApprox(7_000_000.0, PriceCalculator.pricePerGramForKarat(goldPrice18k, 14))
    }

    @Test
    fun `computeLine follows the conventional Iranian jewelry pricing formula`() {
        // weight=5g, 18k, price=9,000,000 T/g, labor=7%, profit=7%, tax=9%, qty=1
        val result = PriceCalculator.computeLine(
            weightGrams = 5.0,
            karat = 18,
            goldPricePerGram18k = 9_000_000.0,
            laborFeePercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            quantity = 1
        )

        val expectedBaseGoldValue = 5.0 * 9_000_000.0 // 45,000,000
        val expectedLaborFee = expectedBaseGoldValue * 0.07 // 3,150,000
        val expectedProfit = (expectedBaseGoldValue + expectedLaborFee) * 0.07 // 3,370,500
        val expectedTax = (expectedLaborFee + expectedProfit) * 0.09 // 586,845
        val expectedUnitPrice = expectedBaseGoldValue + expectedLaborFee + expectedProfit + expectedTax

        assertApprox(expectedBaseGoldValue, result.baseGoldValue)
        assertApprox(expectedLaborFee, result.laborFeeAmount)
        assertApprox(expectedProfit, result.profitAmount)
        assertApprox(expectedTax, result.taxAmount)
        assertApprox(expectedUnitPrice, result.unitPrice)
        assertApprox(expectedUnitPrice, result.lineTotal) // quantity = 1
    }

    @Test
    fun `tax is computed only on labor fee plus profit, never on the raw gold value`() {
        val zeroLaborProfit = PriceCalculator.computeLine(
            weightGrams = 10.0,
            karat = 18,
            goldPricePerGram18k = 5_000_000.0,
            laborFeePercent = 0.0,
            profitPercent = 0.0,
            taxPercent = 9.0,
            quantity = 1
        )

        // With no labor fee and no profit, VAT base is zero, so tax must be zero
        // even though the gold value itself is large.
        assertApprox(0.0, zeroLaborProfit.taxAmount)
        assertApprox(zeroLaborProfit.baseGoldValue, zeroLaborProfit.unitPrice)
    }

    @Test
    fun `lineTotal multiplies unit price by quantity`() {
        val single = PriceCalculator.computeLine(
            weightGrams = 2.0,
            karat = 18,
            goldPricePerGram18k = 8_000_000.0,
            laborFeePercent = 10.0,
            profitPercent = 5.0,
            taxPercent = 9.0,
            quantity = 1
        )
        val triple = PriceCalculator.computeLine(
            weightGrams = 2.0,
            karat = 18,
            goldPricePerGram18k = 8_000_000.0,
            laborFeePercent = 10.0,
            profitPercent = 5.0,
            taxPercent = 9.0,
            quantity = 3
        )

        assertApprox(single.unitPrice * 3, triple.lineTotal)
    }

    @Test
    fun `computeInvoiceTotals applies percentage discount to the summed line totals`() {
        val totals = PriceCalculator.computeInvoiceTotals(
            lineTotals = listOf(1_000_000.0, 2_000_000.0, 3_000_000.0),
            discountPercent = 10.0
        )

        assertApprox(6_000_000.0, totals.subtotal)
        assertApprox(600_000.0, totals.discountAmount)
        assertApprox(5_400_000.0, totals.grandTotal)
    }

    @Test
    fun `computeInvoiceTotals never returns a negative grand total`() {
        val totals = PriceCalculator.computeInvoiceTotals(
            lineTotals = listOf(1_000_000.0),
            discountPercent = 150.0
        )

        assertEquals(0.0, totals.grandTotal, 0.0)
    }

    @Test
    fun `computeInvoiceTotals with no lines returns zeroes`() {
        val totals = PriceCalculator.computeInvoiceTotals(emptyList(), discountPercent = 5.0)

        assertEquals(0.0, totals.subtotal, 0.0)
        assertEquals(0.0, totals.discountAmount, 0.0)
        assertEquals(0.0, totals.grandTotal, 0.0)
    }
}
