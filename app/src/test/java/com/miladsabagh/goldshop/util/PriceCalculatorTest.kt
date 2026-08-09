package com.miladsabagh.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class PriceCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun `pricePerGramForKarat scales linearly relative to 18k`() {
        val price18k = 6_000_000.0
        assertClose(6_000_000.0, PriceCalculator.pricePerGramForKarat(price18k, 18))
        assertClose(8_000_000.0, PriceCalculator.pricePerGramForKarat(price18k, 24))
        assertClose(4_666_666.67, PriceCalculator.pricePerGramForKarat(price18k, 14))
    }

    @Test
    fun `calculate applies labor fee profit and tax on top of gold value only`() {
        // weight=1g, price18k=6,000,000, labor=7%, profit=7%, tax=9%
        val result = PriceCalculator.calculate(
            weightGrams = 1.0,
            karat = 18,
            pricePerGram18k = 6_000_000.0,
            laborFeePercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0
        )
        val expectedGoldValue = 6_000_000.0
        val expectedLabor = expectedGoldValue * 0.07 // 420,000
        val expectedProfit = (expectedGoldValue + expectedLabor) * 0.07 // 449,400
        val expectedTax = (expectedLabor + expectedProfit) * 0.09 // 78,246
        val expectedUnitTotal = expectedGoldValue + expectedLabor + expectedProfit + expectedTax

        assertClose(expectedGoldValue, result.goldValue)
        assertClose(expectedLabor, result.laborFeeAmount)
        assertClose(expectedProfit, result.profitAmount)
        assertClose(expectedTax, result.taxAmount)
        assertClose(expectedUnitTotal, result.unitTotal)
        assertClose(expectedUnitTotal, result.lineTotal) // quantity defaults to 1
    }

    @Test
    fun `tax is not applied to the raw gold value, only to labor and profit`() {
        val result = PriceCalculator.calculate(
            weightGrams = 2.0,
            karat = 18,
            pricePerGram18k = 5_000_000.0,
            laborFeePercent = 0.0,
            profitPercent = 0.0,
            taxPercent = 50.0
        )
        // No labor/profit means tax base is zero, regardless of the (large) tax rate.
        assertClose(0.0, result.taxAmount)
        assertClose(10_000_000.0, result.goldValue)
        assertClose(10_000_000.0, result.unitTotal)
    }

    @Test
    fun `quantity multiplies the unit total into the line total`() {
        val result = PriceCalculator.calculate(
            weightGrams = 1.5,
            karat = 21,
            pricePerGram18k = 4_000_000.0,
            laborFeePercent = 10.0,
            profitPercent = 5.0,
            taxPercent = 9.0,
            stonePrice = 200_000.0,
            quantity = 3
        )
        assertClose(result.unitTotal * 3, result.lineTotal)
    }

    @Test
    fun `stone price is added to the unit total without incurring tax`() {
        val withoutStone = PriceCalculator.calculate(
            weightGrams = 1.0,
            karat = 18,
            pricePerGram18k = 6_000_000.0,
            laborFeePercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            stonePrice = 0.0
        )
        val withStone = PriceCalculator.calculate(
            weightGrams = 1.0,
            karat = 18,
            pricePerGram18k = 6_000_000.0,
            laborFeePercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            stonePrice = 500_000.0
        )
        assertClose(withoutStone.taxAmount, withStone.taxAmount)
        assertClose(withoutStone.unitTotal + 500_000.0, withStone.unitTotal)
    }

    @Test
    fun `zero weight yields zero total`() {
        val result = PriceCalculator.calculate(
            weightGrams = 0.0,
            karat = 18,
            pricePerGram18k = 6_000_000.0,
            laborFeePercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0
        )
        assertClose(0.0, result.lineTotal)
    }
}
