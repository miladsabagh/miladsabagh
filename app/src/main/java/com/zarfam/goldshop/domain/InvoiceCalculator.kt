package com.zarfam.goldshop.domain

import kotlin.math.roundToLong

/**
 * Result of pricing a single invoice line, all amounts in Toman.
 */
data class LineAmounts(
    val rawGoldValue: Long,
    val wageAmount: Long,
    val profitAmount: Long,
    val taxAmount: Long,
    val lineTotal: Long,
)

/**
 * Standard Iranian gold-market pricing:
 *
 * 1. Raw gold value  = weight x (price of 18k gram scaled to item karat)
 * 2. Crafting wage   = raw value x wage%
 * 3. Seller profit   = (raw + wage) x profit%
 * 4. VAT             = (wage + profit) x tax%   (tax applies only to wage & profit)
 * 5. Line total      = (raw + wage + profit + VAT) x quantity
 */
object InvoiceCalculator {

    const val BASE_KARAT = 18

    fun pricePerGramForKarat(pricePerGram18: Long, karat: Int): Double {
        require(karat > 0) { "karat must be positive" }
        return pricePerGram18.toDouble() * karat / BASE_KARAT
    }

    fun calculateLine(
        pricePerGram18: Long,
        weightGrams: Double,
        karat: Int,
        wagePercent: Double,
        profitPercent: Double,
        taxPercent: Double,
        quantity: Int = 1,
    ): LineAmounts {
        require(pricePerGram18 >= 0) { "gold price must not be negative" }
        require(weightGrams >= 0) { "weight must not be negative" }
        require(quantity >= 1) { "quantity must be at least 1" }

        val raw = weightGrams * pricePerGramForKarat(pricePerGram18, karat)
        val wage = raw * wagePercent / 100.0
        val profit = (raw + wage) * profitPercent / 100.0
        val tax = (wage + profit) * taxPercent / 100.0

        val rawR = raw.roundToLong() * quantity
        val wageR = wage.roundToLong() * quantity
        val profitR = profit.roundToLong() * quantity
        val taxR = tax.roundToLong() * quantity

        return LineAmounts(
            rawGoldValue = rawR,
            wageAmount = wageR,
            profitAmount = profitR,
            taxAmount = taxR,
            lineTotal = rawR + wageR + profitR + taxR,
        )
    }

    fun grandTotal(itemsTotal: Long, discount: Long): Long =
        (itemsTotal - discount).coerceAtLeast(0)
}
