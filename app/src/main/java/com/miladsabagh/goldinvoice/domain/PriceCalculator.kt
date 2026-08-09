package com.miladsabagh.goldinvoice.domain

/**
 * Encapsulates the pricing formula conventional in the Iranian gold & jewelry market.
 *
 * The gold price is always quoted per gram of 18-karat ("rayej") gold. For items of a
 * different purity, the effective price per gram is scaled by the karat ratio (karat / 18).
 *
 * For a single line item:
 *  1. pricePerGramForKarat = goldPricePerGram18k * (karat / 18)
 *  2. baseGoldValue         = weightGrams * pricePerGramForKarat
 *  3. laborFeeAmount        = baseGoldValue * laborFeePercent / 100          (اجرت)
 *  4. profitAmount          = (baseGoldValue + laborFeeAmount) * profitPercent / 100   (سود)
 *  5. taxAmount             = (laborFeeAmount + profitAmount) * taxPercent / 100       (مالیات بر ارزش افزوده)
 *     -- Per Iranian regulation, VAT applies only to the labor fee + profit, not to the raw gold value.
 *  6. unitPrice             = baseGoldValue + laborFeeAmount + profitAmount + taxAmount
 *  7. lineTotal             = unitPrice * quantity
 */
object PriceCalculator {

    fun pricePerGramForKarat(goldPricePerGram18k: Double, karat: Int): Double =
        goldPricePerGram18k * karat / 18.0

    data class LineBreakdown(
        val pricePerGramForKarat: Double,
        val baseGoldValue: Double,
        val laborFeeAmount: Double,
        val profitAmount: Double,
        val taxAmount: Double,
        val unitPrice: Double,
        val lineTotal: Double
    )

    fun computeLine(
        weightGrams: Double,
        karat: Int,
        goldPricePerGram18k: Double,
        laborFeePercent: Double,
        profitPercent: Double,
        taxPercent: Double,
        quantity: Int
    ): LineBreakdown {
        val perGram = pricePerGramForKarat(goldPricePerGram18k, karat)
        val baseGoldValue = weightGrams * perGram
        val laborFeeAmount = baseGoldValue * laborFeePercent / 100.0
        val profitAmount = (baseGoldValue + laborFeeAmount) * profitPercent / 100.0
        val taxAmount = (laborFeeAmount + profitAmount) * taxPercent / 100.0
        val unitPrice = baseGoldValue + laborFeeAmount + profitAmount + taxAmount
        val lineTotal = unitPrice * quantity
        return LineBreakdown(
            pricePerGramForKarat = perGram,
            baseGoldValue = baseGoldValue,
            laborFeeAmount = laborFeeAmount,
            profitAmount = profitAmount,
            taxAmount = taxAmount,
            unitPrice = unitPrice,
            lineTotal = lineTotal
        )
    }

    data class InvoiceTotals(
        val subtotal: Double,
        val discountAmount: Double,
        val grandTotal: Double
    )

    fun computeInvoiceTotals(
        lineTotals: List<Double>,
        discountPercent: Double
    ): InvoiceTotals {
        val subtotal = lineTotals.sum()
        val discountAmount = subtotal * discountPercent / 100.0
        val grandTotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        return InvoiceTotals(subtotal = subtotal, discountAmount = discountAmount, grandTotal = grandTotal)
    }
}
