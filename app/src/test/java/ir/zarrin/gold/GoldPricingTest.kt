package ir.zarrin.gold

import ir.zarrin.gold.domain.GoldPricing
import ir.zarrin.gold.domain.PriceBreakdown
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldPricingTest {

    @Test
    fun `price per gram scales with karat`() {
        // گرم ۱۸ عیار = ۱٬۰۰۰٬۰۰۰ → گرم ۲۴ عیار باید یک‌سوم بیشتر باشد
        assertEquals(1_000_000.0, GoldPricing.pricePerGram(1_000_000, 18), 0.001)
        assertEquals(1_333_333.333, GoldPricing.pricePerGram(1_000_000, 24), 0.5)
        assertEquals(1_166_666.667, GoldPricing.pricePerGram(1_000_000, 21), 0.5)
    }

    @Test
    fun `full breakdown follows iranian market formula`() {
        // هر گرم ۱۸ عیار: ۵٬۰۰۰٬۰۰۰ تومان، وزن ۴/۵ گرم، اجرت ۱۲٪، سود ۷٪، مالیات ۱۰٪
        val b = GoldPricing.calculate(
            pricePerGram18k = 5_000_000,
            weightGrams = 4.5,
            karat = 18,
            wagePercent = 12.0,
            profitPercent = 7.0,
            taxPercent = 10.0,
        )
        // بهای طلا = 4.5 × 5,000,000 = 22,500,000
        assertEquals(22_500_000L, b.goldValue)
        // اجرت = 22,500,000 × 12٪ = 2,700,000
        assertEquals(2_700_000L, b.wage)
        // سود = (22,500,000 + 2,700,000) × 7٪ = 1,764,000
        assertEquals(1_764_000L, b.profit)
        // مالیات = (2,700,000 + 1,764,000) × 10٪ = 446,400
        assertEquals(446_400L, b.tax)
        // جمع
        assertEquals(22_500_000L + 2_700_000L + 1_764_000L + 446_400L, b.total)
    }

    @Test
    fun `tax applies only to wage and profit not gold value`() {
        val noWageNoProfit = GoldPricing.calculate(
            pricePerGram18k = 3_000_000,
            weightGrams = 10.0,
            karat = 18,
            wagePercent = 0.0,
            profitPercent = 0.0,
            taxPercent = 10.0,
        )
        assertEquals(0L, noWageNoProfit.tax)
        assertEquals(30_000_000L, noWageNoProfit.total)
    }

    @Test
    fun `quantity multiplies all components`() {
        val single = GoldPricing.calculate(
            pricePerGram18k = 5_000_000, weightGrams = 2.0, karat = 18,
            wagePercent = 10.0, profitPercent = 7.0, taxPercent = 10.0, quantity = 1,
        )
        val triple = GoldPricing.calculate(
            pricePerGram18k = 5_000_000, weightGrams = 2.0, karat = 18,
            wagePercent = 10.0, profitPercent = 7.0, taxPercent = 10.0, quantity = 3,
        )
        assertEquals(single.goldValue * 3, triple.goldValue)
        assertEquals(single.wage * 3, triple.wage)
        assertEquals(single.profit * 3, triple.profit)
        assertEquals(single.tax * 3, triple.tax)
        assertEquals(single.total * 3, triple.total)
    }

    @Test
    fun `karat 24 gold value is higher`() {
        val k18 = GoldPricing.calculate(
            pricePerGram18k = 3_000_000, weightGrams = 1.0, karat = 18,
            wagePercent = 0.0, profitPercent = 0.0, taxPercent = 0.0,
        )
        val k24 = GoldPricing.calculate(
            pricePerGram18k = 3_000_000, weightGrams = 1.0, karat = 24,
            wagePercent = 0.0, profitPercent = 0.0, taxPercent = 0.0,
        )
        assertEquals(3_000_000L, k18.goldValue)
        assertEquals(4_000_000L, k24.goldValue)
    }

    @Test
    fun `breakdown plus sums componentwise`() {
        val a = PriceBreakdown(100, 10, 5, 2)
        val b = PriceBreakdown(200, 20, 10, 4)
        val sum = a + b
        assertEquals(300L, sum.goldValue)
        assertEquals(30L, sum.wage)
        assertEquals(15L, sum.profit)
        assertEquals(6L, sum.tax)
        assertEquals(351L, sum.total)
    }
}
