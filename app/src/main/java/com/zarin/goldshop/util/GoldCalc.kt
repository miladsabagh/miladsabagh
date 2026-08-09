package com.zarin.goldshop.util

import kotlin.math.roundToLong

/**
 * Pricing engine for gold/jewelry line items following common Iranian
 * gold-shop invoicing: gold value + making charge (اجرت) + shop profit (سود) + VAT (مالیات ارزش افزوده).
 */
object GoldCalc {

    data class LineInput(
        val weight: Double,
        val karat: Int,
        val wagePercent: Double,
        val stonePrice: Long,
        val quantity: Int,
    )

    data class LineResult(
        val goldValue: Long,
        val wage: Long,
        val stone: Long,
        val profit: Long,
        val tax: Long,
        val unitTotal: Long,
        val lineTotal: Long,
    )

    /**
     * @param goldPricePerGram day rate for 18k gold, in Toman.
     */
    fun computeLine(
        input: LineInput,
        goldPricePerGram: Long,
        profitPercent: Double,
        taxPercent: Double,
    ): LineResult {
        val adjustedRate = goldPricePerGram.toDouble() * (input.karat.toDouble() / 18.0)
        val goldValue = (input.weight * adjustedRate).roundToLong()
        val wage = (goldValue * (input.wagePercent / 100.0)).roundToLong()
        val stone = input.stonePrice
        val profit = ((goldValue + wage) * (profitPercent / 100.0)).roundToLong()
        val tax = ((wage + profit) * (taxPercent / 100.0)).roundToLong()
        val unitTotal = goldValue + wage + stone + profit + tax
        val qty = if (input.quantity <= 0) 1 else input.quantity
        return LineResult(
            goldValue = goldValue,
            wage = wage,
            stone = stone,
            profit = profit,
            tax = tax,
            unitTotal = unitTotal,
            lineTotal = unitTotal * qty,
        )
    }
}
