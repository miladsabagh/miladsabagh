package ir.zarrin.goldshop.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingEngineTest {

    private val baseRate = 6_850_000L

    @Test
    fun `نرخ هر گرم بر اساس عیار محاسبه می شود`() {
        assertEquals(6_850_000L, PricingEngine.ratePerGram(baseRate, 18))
        assertEquals(7_991_667L, PricingEngine.ratePerGram(baseRate, 21))
        assertEquals(9_133_333L, PricingEngine.ratePerGram(baseRate, 24))
        assertEquals(0L, PricingEngine.ratePerGram(baseRate, 0))
    }

    @Test
    fun `قیمت کالای وزنی شامل بهای طلا اجرت سود و مالیات است`() {
        val result = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.BY_WEIGHT,
                weightGrams = 3.450,
                karat = 18,
                baseRatePerGram = baseRate,
                wagePercent = 9.0,
                profitPercent = 7.0,
                taxPercent = 10.0,
                taxable = true
            )
        )

        assertEquals(23_632_500L, result.goldValue)
        assertEquals(2_126_925L, result.wage)
        assertEquals(1_803_160L, result.profit)
        assertEquals(3_930_085L, result.taxBase)
        assertEquals(393_009L, result.tax)
        assertEquals(27_955_594L, result.unitPrice)
        assertEquals(27_955_594L, result.lineTotal)
    }

    @Test
    fun `بهای نگین مشمول اجرت و سود و مالیات نمی شود`() {
        val withoutStone = PricingEngine.calculate(
            PriceInput(
                weightGrams = 2.0,
                karat = 18,
                baseRatePerGram = baseRate,
                wagePercent = 10.0,
                profitPercent = 7.0,
                taxPercent = 10.0
            )
        )
        val withStone = PricingEngine.calculate(
            PriceInput(
                weightGrams = 2.0,
                karat = 18,
                baseRatePerGram = baseRate,
                wagePercent = 10.0,
                profitPercent = 7.0,
                stonePrice = 1_500_000L,
                taxPercent = 10.0
            )
        )

        assertEquals(withoutStone.tax, withStone.tax)
        assertEquals(withoutStone.unitPrice + 1_500_000L, withStone.unitPrice)
    }

    @Test
    fun `کالای مقطوع غیر مشمول مالیات، قیمت ثابت دارد`() {
        val coin = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 48_500_000L,
                taxPercent = 10.0,
                taxable = false,
                quantity = 2
            )
        )

        assertEquals(0L, coin.goldValue)
        assertEquals(0L, coin.tax)
        assertEquals(48_500_000L, coin.unitPrice)
        assertEquals(97_000_000L, coin.lineSubtotal)
    }

    @Test
    fun `کالای مقطوع مشمول مالیات، مالیات روی کل قیمت دارد`() {
        val item = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 10_000_000L,
                taxPercent = 10.0,
                taxable = true
            )
        )

        assertEquals(1_000_000L, item.tax)
        assertEquals(11_000_000L, item.unitPrice)
    }

    @Test
    fun `تخفیف بیشتر از جمع ردیف به جمع ردیف محدود می شود`() {
        val item = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 5_000_000L,
                taxable = false,
                quantity = 2,
                discount = 20_000_000L
            )
        )

        assertEquals(10_000_000L, item.discount)
        assertEquals(0L, item.lineTotal)
    }

    @Test
    fun `تعداد کمتر از یک به یک تبدیل می شود`() {
        val item = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 1_000_000L,
                taxable = false,
                quantity = 0
            )
        )

        assertEquals(1, item.quantity)
        assertEquals(1_000_000L, item.lineTotal)
    }

    @Test
    fun `جمع بندی فاکتور مقادیر ردیف ها را در تعداد ضرب می کند`() {
        val ring = PricingEngine.calculate(
            PriceInput(
                weightGrams = 3.0,
                karat = 18,
                baseRatePerGram = baseRate,
                wagePercent = 10.0,
                profitPercent = 7.0,
                taxPercent = 10.0,
                quantity = 2
            )
        )
        val coin = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 48_500_000L,
                taxable = false
            )
        )

        val totals = listOf(ring, coin).summarize(invoiceDiscount = 500_000L, totalWeight = 6.0)

        assertEquals(ring.goldValue * 2, totals.goldValue)
        assertEquals(ring.wage * 2, totals.wage)
        assertEquals(ring.tax * 2, totals.tax)
        assertEquals(ring.lineTotal + coin.lineTotal, totals.subtotal)
        assertEquals(totals.subtotal - 500_000L, totals.grandTotal)
        assertEquals(3, totals.itemCount)
        assertEquals(6.0, totals.totalWeight, 0.0001)
    }

    @Test
    fun `تخفیف کل فاکتور از جمع فاکتور بیشتر نمی شود`() {
        val item = PricingEngine.calculate(
            PriceInput(
                pricingMode = PricingMode.FIXED,
                fixedPrice = 1_000_000L,
                taxable = false
            )
        )

        val totals = listOf(item).summarize(invoiceDiscount = 9_000_000L)

        assertEquals(1_000_000L, totals.invoiceDiscount)
        assertEquals(0L, totals.grandTotal)
    }
}
