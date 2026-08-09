package ir.zarin.faktor.domain

import ir.zarin.faktor.data.model.PricingMode
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldPricingTest {

    private val context = PricingContext(
        goldRatePerGram18Rial = 30_000_000,
        profitPercent = 7.0,
        vatPercent = 10.0,
    )

    @Test
    fun `قیمت طلای وزنی شامل اجرت و سود و مالیات است`() {
        val input = PriceLineInput(
            pricingMode = PricingMode.BY_WEIGHT,
            quantity = 1,
            weightGrams = 5.0,
            karat = 18,
            wagePercent = 12.0,
        )

        val breakdown = GoldPricing.calculate(input, context)

        assertEquals(150_000_000L, breakdown.goldValueRial)
        assertEquals(18_000_000L, breakdown.wageRial)
        assertEquals(11_760_000L, breakdown.profitRial)
        assertEquals(2_976_000L, breakdown.vatRial)
        assertEquals(182_736_000L, breakdown.totalRial)
    }

    @Test
    fun `نرخ بر اساس عیار تعدیل می شود`() {
        assertEquals(30_000_000.0, GoldPricing.effectiveRatePerGram(30_000_000, 18), 0.001)
        assertEquals(35_000_000.0, GoldPricing.effectiveRatePerGram(30_000_000, 21), 0.001)
        assertEquals(40_000_000.0, GoldPricing.effectiveRatePerGram(30_000_000, 24), 0.001)

        val breakdown = GoldPricing.calculate(
            PriceLineInput(weightGrams = 2.0, karat = 21, wagePercent = 0.0),
            context,
        )
        assertEquals(70_000_000L, breakdown.goldValueRial)
    }

    @Test
    fun `تعداد در ارزش طلا و قیمت نگین ضرب می شود`() {
        val breakdown = GoldPricing.calculate(
            PriceLineInput(
                quantity = 3,
                weightGrams = 2.0,
                karat = 18,
                wagePercent = 0.0,
                stonePriceRial = 1_000_000,
                applyVat = false,
            ),
            context.copy(profitPercent = 0.0),
        )

        assertEquals(180_000_000L, breakdown.goldValueRial)
        assertEquals(3_000_000L, breakdown.stoneRial)
        assertEquals(183_000_000L, breakdown.totalRial)
    }

    @Test
    fun `قیمت مقطوع مشمول اجرت و سود نمی شود`() {
        val taxable = GoldPricing.calculate(
            PriceLineInput(
                pricingMode = PricingMode.FIXED_PRICE,
                quantity = 2,
                unitFixedPriceRial = 200_000_000,
                applyVat = true,
            ),
            context,
        )
        assertEquals(400_000_000L, taxable.goldValueRial)
        assertEquals(0L, taxable.wageRial)
        assertEquals(0L, taxable.profitRial)
        assertEquals(40_000_000L, taxable.vatRial)
        assertEquals(440_000_000L, taxable.totalRial)

        val exempt = GoldPricing.calculate(
            PriceLineInput(
                pricingMode = PricingMode.FIXED_PRICE,
                quantity = 2,
                unitFixedPriceRial = 200_000_000,
                applyVat = false,
            ),
            context,
        )
        assertEquals(0L, exempt.vatRial)
        assertEquals(400_000_000L, exempt.totalRial)
    }

    @Test
    fun `تخفیف از مبلغ قلم کسر می شود و منفی نمی شود`() {
        val discounted = GoldPricing.calculate(
            PriceLineInput(
                pricingMode = PricingMode.FIXED_PRICE,
                unitFixedPriceRial = 10_000_000,
                applyVat = false,
                discountRial = 2_000_000,
            ),
            context,
        )
        assertEquals(8_000_000L, discounted.totalRial)

        val overDiscounted = GoldPricing.calculate(
            PriceLineInput(
                pricingMode = PricingMode.FIXED_PRICE,
                unitFixedPriceRial = 10_000_000,
                applyVat = false,
                discountRial = 90_000_000,
            ),
            context,
        )
        assertEquals(0L, overDiscounted.totalRial)
    }

    @Test
    fun `جمع فاکتور شامل تخفیف اقلام و تخفیف کل است`() {
        val first = GoldPricing.calculate(
            PriceLineInput(weightGrams = 5.0, karat = 18, wagePercent = 12.0),
            context,
        )
        val second = GoldPricing.calculate(
            PriceLineInput(
                pricingMode = PricingMode.FIXED_PRICE,
                unitFixedPriceRial = 50_000_000,
                applyVat = false,
                discountRial = 5_000_000,
            ),
            context,
        )

        val totals = GoldPricing.totals(listOf(first, second), invoiceDiscountRial = 1_000_000)

        assertEquals(200_000_000L, totals.goldTotalRial)
        assertEquals(18_000_000L, totals.wageTotalRial)
        assertEquals(11_760_000L, totals.profitTotalRial)
        assertEquals(2_976_000L, totals.vatTotalRial)
        assertEquals(5_000_000L, totals.itemsDiscountRial)
        assertEquals(1_000_000L, totals.invoiceDiscountRial)
        assertEquals(232_736_000L, totals.subtotalRial)
        assertEquals(226_736_000L, totals.grandTotalRial)
    }

    @Test
    fun `نرخ صفر منجر به مبلغ صفر می شود`() {
        val breakdown = GoldPricing.calculate(
            PriceLineInput(weightGrams = 5.0, wagePercent = 12.0),
            PricingContext(goldRatePerGram18Rial = 0, profitPercent = 7.0, vatPercent = 10.0),
        )
        assertEquals(0L, breakdown.totalRial)
    }
}
