package com.miladsabagh.goldshop.util

import kotlin.math.roundToLong

/**
 * Implements the standard Iranian gold-pricing formula:
 *
 * goldValue  = weight(g) * pricePerGramForKarat
 * laborFee   = goldValue * laborFeePercent / 100          (اجرت ساخت)
 * profit     = (goldValue + laborFee) * profitPercent/100 (سود فروشنده)
 * tax        = (laborFee + profit) * taxPercent / 100      (مالیات فقط بر اجرت و سود)
 * lineTotal  = (goldValue + laborFee + profit + tax + stonePrice) * quantity
 *
 * pricePerGramForKarat scales the quoted 18k price linearly by karat purity.
 */
object PriceCalculator {

    data class ItemBreakdown(
        val goldValue: Double,
        val laborFeeAmount: Double,
        val profitAmount: Double,
        val taxAmount: Double,
        val stonePrice: Double,
        val unitTotal: Double,
        val lineTotal: Double
    )

    fun pricePerGramForKarat(pricePerGram18k: Double, karat: Int): Double =
        pricePerGram18k * karat / 18.0

    fun calculate(
        weightGrams: Double,
        karat: Int,
        pricePerGram18k: Double,
        laborFeePercent: Double,
        profitPercent: Double,
        taxPercent: Double,
        stonePrice: Double = 0.0,
        quantity: Int = 1
    ): ItemBreakdown {
        val perGram = pricePerGramForKarat(pricePerGram18k, karat)
        val goldValue = weightGrams * perGram
        val laborFee = goldValue * (laborFeePercent / 100.0)
        val profit = (goldValue + laborFee) * (profitPercent / 100.0)
        val tax = (laborFee + profit) * (taxPercent / 100.0)
        val unitTotal = goldValue + laborFee + profit + tax + stonePrice
        val lineTotal = unitTotal * quantity
        return ItemBreakdown(
            goldValue = goldValue,
            laborFeeAmount = laborFee,
            profitAmount = profit,
            taxAmount = tax,
            stonePrice = stonePrice,
            unitTotal = unitTotal,
            lineTotal = lineTotal
        )
    }

    fun Double.roundToToman(): Long = this.roundToLong()
}
