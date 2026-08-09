package ir.zarrin.goldshop

import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.LineInput
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldCalculatorTest {

    private val rate18 = 3_500_000L

    @Test
    fun `manufactured gold line follows the wage then profit then tax order`() {
        val input = LineInput(
            kind = ItemKind.MANUFACTURED,
            karat = Karat.K18,
            weightGrams = 5.0,
            quantity = 2,
            goldRatePerGram = rate18,
            wageMode = WageMode.PERCENT,
            wageValue = 10.0,
            profitPercent = 7.0,
            taxBasis = TaxBasis.WAGE_AND_PROFIT,
            taxPercent = 10.0
        )

        val totals = GoldCalculator.calculateLine(input)

        assertEquals(17_500_000L, totals.goldValue)
        assertEquals(1_750_000L, totals.wage)
        assertEquals(1_347_500L, totals.profit)
        assertEquals(3_097_500L, totals.taxableBase)
        assertEquals(309_750L, totals.tax)
        assertEquals(20_907_250L, totals.unitPrice)
        assertEquals(41_814_500L, totals.total)
    }

    @Test
    fun `tax is charged on wage and profit only, never on the raw gold value`() {
        val input = LineInput(
            weightGrams = 10.0,
            goldRatePerGram = rate18,
            wageMode = WageMode.PERCENT,
            wageValue = 0.0,
            profitPercent = 0.0,
            taxBasis = TaxBasis.WAGE_AND_PROFIT,
            taxPercent = 10.0
        )

        val totals = GoldCalculator.calculateLine(input)

        assertEquals(0L, totals.tax)
        assertEquals(35_000_000L, totals.unitPrice)
    }

    @Test
    fun `coins priced per piece are exempt from tax and carry no making charge`() {
        val input = LineInput(
            kind = ItemKind.COIN,
            quantity = 3,
            unitPriceOverride = 18_500_000L,
            taxBasis = TaxBasis.EXEMPT,
            taxPercent = 10.0
        )

        val totals = GoldCalculator.calculateLine(input)

        assertEquals(18_500_000L, totals.goldValue)
        assertEquals(0L, totals.wage)
        assertEquals(0L, totals.tax)
        assertEquals(55_500_000L, totals.total)
    }

    @Test
    fun `full price tax basis also taxes the gold and the stone`() {
        val input = LineInput(
            kind = ItemKind.SILVER,
            weightGrams = 20.0,
            goldRatePerGram = 100_000L,
            wageMode = WageMode.FIXED,
            wageValue = 500_000.0,
            stonePrice = 300_000L,
            taxBasis = TaxBasis.FULL_PRICE,
            taxPercent = 10.0
        )

        val totals = GoldCalculator.calculateLine(input)

        assertEquals(2_000_000L, totals.goldValue)
        assertEquals(500_000L, totals.wage)
        assertEquals(2_800_000L, totals.taxableBase)
        assertEquals(280_000L, totals.tax)
        assertEquals(3_080_000L, totals.unitPrice)
    }

    @Test
    fun `making charge can be quoted per gram`() {
        val input = LineInput(
            weightGrams = 7.5,
            goldRatePerGram = rate18,
            wageMode = WageMode.PER_GRAM,
            wageValue = 200_000.0,
            taxBasis = TaxBasis.EXEMPT
        )

        val totals = GoldCalculator.calculateLine(input)

        assertEquals(1_500_000L, totals.wage)
    }

    @Test
    fun `karat rates are derived from the quoted 18 karat gram price`() {
        assertEquals(3_500_000L, GoldCalculator.rateForKarat(rate18, Karat.K18))
        assertEquals(4_083_333L, GoldCalculator.rateForKarat(rate18, Karat.K21))
        assertEquals(4_662_000L, GoldCalculator.rateForKarat(rate18, Karat.K24))
        assertEquals(0L, GoldCalculator.rateForKarat(rate18, 0))
    }

    @Test
    fun `invoice summary applies discount before computing the outstanding balance`() {
        val first = LineInput(
            weightGrams = 5.0,
            quantity = 1,
            goldRatePerGram = rate18,
            wageValue = 10.0,
            profitPercent = 7.0,
            taxPercent = 10.0
        )
        val second = LineInput(
            kind = ItemKind.COIN,
            quantity = 1,
            unitPriceOverride = 18_500_000L,
            taxBasis = TaxBasis.EXEMPT
        )
        val lines = listOf(first, second).map { it to GoldCalculator.calculateLine(it) }

        val totals = GoldCalculator.summarize(lines, discount = 407_250L, paid = 10_000_000L)

        assertEquals(39_407_250L, totals.gross)
        assertEquals(407_250L, totals.discount)
        assertEquals(39_000_000L, totals.payable)
        assertEquals(29_000_000L, totals.remaining)
        assertEquals(5.0, totals.totalWeightGrams, 0.0001)
        assertEquals(2, totals.pieceCount)
    }

    @Test
    fun `discount can never exceed the invoice total`() {
        val line = LineInput(weightGrams = 1.0, goldRatePerGram = rate18, taxBasis = TaxBasis.EXEMPT)
        val lines = listOf(line to GoldCalculator.calculateLine(line))

        val totals = GoldCalculator.summarize(lines, discount = 999_999_999L)

        assertEquals(3_500_000L, totals.discount)
        assertEquals(0L, totals.payable)
    }
}
