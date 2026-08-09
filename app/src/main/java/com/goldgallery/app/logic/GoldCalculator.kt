package com.goldgallery.app.logic

import kotlin.math.roundToLong

/**
 * محاسبه قیمت طلا بر اساس فرمول رایج بازار ایران:
 * قیمت خام طلا + اجرت ساخت + ۷٪ سود فروشنده + ۹٪ مالیات بر ارزش افزوده (فقط روی اجرت و سود)
 */
object GoldCalculator {

    const val PROFIT_RATE = 0.07
    const val TAX_RATE = 0.09

    fun perGramForKarat(base18PerGram: Long, karat: Int): Double =
        base18PerGram.toDouble() * karat / 18.0

    fun goldRawPrice(weightGrams: Double, karat: Int, base18PerGram: Long): Long =
        (weightGrams * perGramForKarat(base18PerGram, karat)).roundToLong()

    fun itemPrice(
        weightGrams: Double,
        karat: Int,
        wagePerGram: Long,
        base18PerGram: Long,
        quantity: Int = 1,
    ): PriceBreakdown {
        val goldRaw = goldRawPrice(weightGrams, karat, base18PerGram) * quantity
        val wage = (wagePerGram * weightGrams).roundToLong() * quantity
        val profit = ((goldRaw + wage) * PROFIT_RATE).roundToLong()
        val tax = ((wage + profit) * TAX_RATE).roundToLong()
        return PriceBreakdown(
            goldRaw = goldRaw,
            wage = wage,
            profit = profit,
            tax = tax,
            total = goldRaw + wage + profit + tax,
        )
    }

    fun total(items: List<PriceBreakdown>): PriceBreakdown = PriceBreakdown(
        goldRaw = items.sumOf { it.goldRaw },
        wage = items.sumOf { it.wage },
        profit = items.sumOf { it.profit },
        tax = items.sumOf { it.tax },
        total = items.sumOf { it.total },
    )
}

data class PriceBreakdown(
    val goldRaw: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val total: Long,
)
