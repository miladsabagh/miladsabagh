package com.miladsabagh.zarrin.util

import kotlin.math.roundToLong

/**
 * Iranian gold-shop pricing breakdown.
 *
 * The base rate is quoted per gram of 18-karat (750 purity) gold. The value of an
 * item's raw gold scales linearly with purity. Seller profit is a percentage of
 * (gold + wage), and VAT is charged only on (wage + profit), per current rules.
 */
object Pricing {

    fun purityOf(karat: Int): Int = when (karat) {
        24 -> 999
        22 -> 916
        21 -> 875
        20 -> 833
        18 -> 750
        14 -> 585
        else -> 750
    }

    fun goldUnitPrice(basePrice18kPerGram: Long, karat: Int): Long =
        (basePrice18kPerGram.toDouble() * purityOf(karat) / 750.0).roundToLong()

    fun goldValue(weightGrams: Double, unitPricePerGram: Long): Long =
        (weightGrams * unitPricePerGram).roundToLong()

    fun wageAmount(weightGrams: Double, wagePerGram: Long): Long =
        (weightGrams * wagePerGram).roundToLong()

    fun profitAmount(goldValue: Long, wageAmount: Long, profitPercent: Double): Long =
        ((goldValue + wageAmount) * profitPercent / 100.0).roundToLong()

    fun taxAmount(wageAmount: Long, profitAmount: Long, taxPercent: Double): Long =
        ((wageAmount + profitAmount) * taxPercent / 100.0).roundToLong()

    fun lineTotal(goldValue: Long, wageAmount: Long, profitAmount: Long, taxAmount: Long): Long =
        goldValue + wageAmount + profitAmount + taxAmount
}

data class LineBreakdown(
    val unitGoldPrice: Long,
    val goldValue: Long,
    val wageAmount: Long,
    val profitAmount: Long,
    val taxAmount: Long,
    val lineTotal: Long
)

fun priceLine(
    basePrice18kPerGram: Long,
    karat: Int,
    weightGrams: Double,
    wagePerGram: Long,
    profitPercent: Double,
    taxPercent: Double
): LineBreakdown {
    val unit = Pricing.goldUnitPrice(basePrice18kPerGram, karat)
    val gold = Pricing.goldValue(weightGrams, unit)
    val wage = Pricing.wageAmount(weightGrams, wagePerGram)
    val profit = Pricing.profitAmount(gold, wage, profitPercent)
    val tax = Pricing.taxAmount(wage, profit, taxPercent)
    return LineBreakdown(
        unitGoldPrice = unit,
        goldValue = gold,
        wageAmount = wage,
        profitAmount = profit,
        taxAmount = tax,
        lineTotal = Pricing.lineTotal(gold, wage, profit, tax)
    )
}
