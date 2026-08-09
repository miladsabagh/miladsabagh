package com.zarin.goldshop

import com.zarin.goldshop.util.GoldCalc
import com.zarin.goldshop.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldCalcTest {

    @Test
    fun computeLine_18k_basic() {
        // 5 grams of 18k gold at 3,000,000 Toman/gram, 10% wage, 7% profit, 9% tax.
        val r = GoldCalc.computeLine(
            GoldCalc.LineInput(weight = 5.0, karat = 18, wagePercent = 10.0, stonePrice = 0, quantity = 1),
            goldPricePerGram = 3_000_000L,
            profitPercent = 7.0,
            taxPercent = 9.0,
        )
        val goldValue = 15_000_000L                 // 5 * 3,000,000
        val wage = 1_500_000L                        // 10% of gold value
        val profit = 1_155_000L                      // 7% of (gold + wage)
        val tax = 238_950L                           // 9% of (wage + profit)
        assertEquals(goldValue, r.goldValue)
        assertEquals(wage, r.wage)
        assertEquals(profit, r.profit)
        assertEquals(tax, r.tax)
        assertEquals(goldValue + wage + profit + tax, r.unitTotal)
        assertEquals(r.unitTotal, r.lineTotal)
    }

    @Test
    fun computeLine_karat_adjusts_rate() {
        // 24k should be priced higher than 18k for the same weight.
        val base = GoldCalc.computeLine(
            GoldCalc.LineInput(1.0, 18, 0.0, 0, 1), 1_000_000L, 0.0, 0.0
        )
        val high = GoldCalc.computeLine(
            GoldCalc.LineInput(1.0, 24, 0.0, 0, 1), 1_000_000L, 0.0, 0.0
        )
        assertEquals(1_000_000L, base.goldValue)
        assertEquals((1_000_000.0 * 24 / 18).toLong(), high.goldValue)
    }

    @Test
    fun computeLine_quantity_multiplies_line_total() {
        val r = GoldCalc.computeLine(
            GoldCalc.LineInput(2.0, 18, 0.0, 500_000, 3), 2_000_000L, 0.0, 0.0
        )
        assertEquals(r.unitTotal * 3, r.lineTotal)
    }

    @Test
    fun persian_amount_formatting() {
        assertEquals("۱٬۲۳۴٬۵۶۷", PersianUtils.formatAmount(1_234_567L))
        assertEquals("۰", PersianUtils.formatAmount(0L))
    }

    @Test
    fun gregorian_to_jalali_known_dates() {
        val nowruz = PersianUtils.gregorianToJalali(2024, 3, 20) // 1403/01/01
        assertEquals(1403, nowruz.year)
        assertEquals(1, nowruz.month)
        assertEquals(1, nowruz.day)
    }
}
