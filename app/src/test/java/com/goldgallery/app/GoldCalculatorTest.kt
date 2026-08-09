package com.goldgallery.app

import com.goldgallery.app.logic.GoldCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldCalculatorTest {

    @Test
    fun `per gram price scales with karat`() {
        val base18 = 1_800_000L
        assertEquals(1_800_000.0, GoldCalculator.perGramForKarat(base18, 18), 0.001)
        assertEquals(2_100_000.0, GoldCalculator.perGramForKarat(base18, 21), 0.001)
        assertEquals(2_200_000.0, GoldCalculator.perGramForKarat(base18, 22), 0.001)
        assertEquals(2_400_000.0, GoldCalculator.perGramForKarat(base18, 24), 0.001)
    }

    @Test
    fun `gold raw price uses weight and karat`() {
        // ۲ گرم طلای ۲۴ عیار با نرخ ۱۸ عیار ۱٬۸۰۰٬۰۰۰ → ۲ × ۲٬۴۰۰٬۰۰۰
        assertEquals(4_800_000L, GoldCalculator.goldRawPrice(2.0, 24, 1_800_000L))
        // ۳ گرم طلای ۱۸ عیار → ۳ × ۱٬۸۰۰٬۰۰۰
        assertEquals(5_400_000L, GoldCalculator.goldRawPrice(3.0, 18, 1_800_000L))
    }

    @Test
    fun `item price breakdown follows market formula`() {
        // ۱۰ گرم طلای ۱۸ عیار، اجرت ۱۰۰ هزار تومان هر گرم
        val price = GoldCalculator.itemPrice(
            weightGrams = 10.0,
            karat = 18,
            wagePerGram = 100_000,
            base18PerGram = 1_000_000,
        )
        assertEquals(10_000_000L, price.goldRaw)
        assertEquals(1_000_000L, price.wage)
        // سود = ۷٪ × (طلای خام + اجرت) = ۷۷۰٬۰۰۰
        assertEquals(770_000L, price.profit)
        // مالیات = ۹٪ × (اجرت + سود) = ۱۵۹٬۳۰۰
        assertEquals(159_300L, price.tax)
        assertEquals(11_929_300L, price.total)
    }

    @Test
    fun `quantity multiplies gold and wage`() {
        val single = GoldCalculator.itemPrice(2.0, 18, 100_000, 1_000_000, quantity = 1)
        val triple = GoldCalculator.itemPrice(2.0, 18, 100_000, 1_000_000, quantity = 3)
        assertEquals(single.goldRaw * 3, triple.goldRaw)
        assertEquals(single.wage * 3, triple.wage)
        assertEquals(single.total * 3, triple.total)
    }

    @Test
    fun `total aggregates breakdowns`() {
        val a = GoldCalculator.itemPrice(2.0, 18, 100_000, 1_000_000)
        val b = GoldCalculator.itemPrice(1.0, 22, 200_000, 1_000_000)
        val total = GoldCalculator.total(listOf(a, b))
        assertEquals(a.goldRaw + b.goldRaw, total.goldRaw)
        assertEquals(a.wage + b.wage, total.wage)
        assertEquals(a.profit + b.profit, total.profit)
        assertEquals(a.tax + b.tax, total.tax)
        assertEquals(a.total + b.total, total.total)
    }
}
