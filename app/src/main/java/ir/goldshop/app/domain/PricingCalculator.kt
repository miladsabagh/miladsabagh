package ir.goldshop.app.domain

import kotlin.math.roundToLong

/**
 * منطق محاسبه قیمت اقلام طلا و جواهر بر اساس فرمول متداول بازار طلای ایران:
 *
 * قیمت پایه = وزن (گرم) × نرخ هر گرم طلا
 * اجرت (دستمزد ساخت) = قیمت پایه × درصد اجرت
 * سود فروشنده = (قیمت پایه + اجرت) × درصد سود
 * مالیات بر ارزش‌افزوده = (اجرت + سود) × درصد مالیات   (طبق قانون، مالیات فقط به اجرت و سود تعلق می‌گیرد نه به اصل طلا)
 * جمع واحد = قیمت پایه + اجرت + سود + مالیات
 * جمع قلم = جمع واحد × تعداد
 */
object PricingCalculator {

    data class LineInput(
        val weightGrams: Double,
        val pricePerGram: Double,
        val laborPercent: Double,
        val profitPercent: Double,
        val taxPercent: Double,
        val quantity: Int = 1
    )

    data class LineResult(
        val baseAmount: Double,
        val laborAmount: Double,
        val profitAmount: Double,
        val taxAmount: Double,
        val unitTotal: Double,
        val lineTotal: Double
    )

    fun calculateLine(input: LineInput): LineResult {
        val base = (input.weightGrams * input.pricePerGram).roundToNearest()
        val labor = (base * input.laborPercent / 100.0).roundToNearest()
        val profit = ((base + labor) * input.profitPercent / 100.0).roundToNearest()
        val tax = ((labor + profit) * input.taxPercent / 100.0).roundToNearest()
        val unitTotal = base + labor + profit + tax
        val lineTotal = unitTotal * input.quantity
        return LineResult(
            baseAmount = base,
            laborAmount = labor,
            profitAmount = profit,
            taxAmount = tax,
            unitTotal = unitTotal,
            lineTotal = lineTotal
        )
    }

    /** قیمت هر گرم را متناسب با عیار، نسبت به نرخ پایه‌ی عیار ۱۸ محاسبه می‌کند. */
    fun pricePerGramForKarat(basePricePerGram18k: Double, karat: Int): Double {
        if (basePricePerGram18k <= 0.0) return 0.0
        return (basePricePerGram18k * karat / 18.0).roundToNearest()
    }

    private fun Double.roundToNearest(): Double = this.roundToLong().toDouble()
}
