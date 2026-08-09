package ir.goldshop.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {

    @Test
    fun `calculateLine computes base labor profit and tax correctly for 18k gold`() {
        // وزن ۲ گرم، نرخ هر گرم ۵٬۰۰۰٬۰۰۰ تومان، اجرت ۷٪، سود ۷٪، مالیات ۹٪
        val input = PricingCalculator.LineInput(
            weightGrams = 2.0,
            pricePerGram = 5_000_000.0,
            laborPercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0,
            quantity = 1
        )

        val result = PricingCalculator.calculateLine(input)

        // قیمت پایه = ۲ × ۵٬۰۰۰٬۰۰۰ = ۱۰٬۰۰۰٬۰۰۰
        assertEquals(10_000_000.0, result.baseAmount, 0.001)
        // اجرت = ۱۰٬۰۰۰٬۰۰۰ × ۷٪ = ۷۰۰٬۰۰۰
        assertEquals(700_000.0, result.laborAmount, 0.001)
        // سود = (۱۰٬۰۰۰٬۰۰۰ + ۷۰۰٬۰۰۰) × ۷٪ = ۷۴۹٬۰۰۰
        assertEquals(749_000.0, result.profitAmount, 0.001)
        // مالیات = (۷۰۰٬۰۰۰ + ۷۴۹٬۰۰۰) × ۹٪ = ۱۳۰٬۴۱۰
        assertEquals(130_410.0, result.taxAmount, 0.001)
        // جمع واحد = ۱۰٬۰۰۰٬۰۰۰ + ۷۰۰٬۰۰۰ + ۷۴۹٬۰۰۰ + ۱۳۰٬۴۱۰ = ۱۱٬۵۷۹٬۴۱۰
        assertEquals(11_579_410.0, result.unitTotal, 0.001)
        assertEquals(11_579_410.0, result.lineTotal, 0.001)
    }

    @Test
    fun `calculateLine multiplies line total by quantity`() {
        val input = PricingCalculator.LineInput(
            weightGrams = 1.0,
            pricePerGram = 1_000_000.0,
            laborPercent = 10.0,
            profitPercent = 10.0,
            taxPercent = 9.0,
            quantity = 3
        )

        val result = PricingCalculator.calculateLine(input)

        assertEquals(result.unitTotal * 3, result.lineTotal, 0.001)
    }

    @Test
    fun `calculateLine returns zero amounts for zero weight`() {
        val input = PricingCalculator.LineInput(
            weightGrams = 0.0,
            pricePerGram = 5_000_000.0,
            laborPercent = 7.0,
            profitPercent = 7.0,
            taxPercent = 9.0
        )

        val result = PricingCalculator.calculateLine(input)

        assertEquals(0.0, result.baseAmount, 0.001)
        assertEquals(0.0, result.laborAmount, 0.001)
        assertEquals(0.0, result.profitAmount, 0.001)
        assertEquals(0.0, result.taxAmount, 0.001)
        assertEquals(0.0, result.lineTotal, 0.001)
    }

    @Test
    fun `pricePerGramForKarat scales proportionally from 18k base`() {
        val base18k = 5_000_000.0

        assertEquals(5_000_000.0, PricingCalculator.pricePerGramForKarat(base18k, 18), 0.001)
        // عیار ۲۴ = نسبت ۲۴/۱۸
        assertEquals((base18k * 24 / 18.0), PricingCalculator.pricePerGramForKarat(base18k, 24), 1.0)
        // عیار ۲۱
        assertEquals((base18k * 21 / 18.0), PricingCalculator.pricePerGramForKarat(base18k, 21), 1.0)
    }

    @Test
    fun `pricePerGramForKarat returns zero when base price is not set`() {
        assertEquals(0.0, PricingCalculator.pricePerGramForKarat(0.0, 18), 0.001)
        assertEquals(0.0, PricingCalculator.pricePerGramForKarat(-100.0, 18), 0.001)
    }
}
