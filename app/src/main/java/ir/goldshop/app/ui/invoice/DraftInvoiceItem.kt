package ir.goldshop.app.ui.invoice

import ir.goldshop.app.domain.PricingCalculator

data class DraftInvoiceItem(
    val localId: Long,
    val productId: Long? = null,
    val itemName: String = "",
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val pricePerGram: Double = 0.0,
    val laborPercent: Double = 7.0,
    val profitPercent: Double = 7.0,
    val taxPercent: Double = 9.0
) {
    val result: PricingCalculator.LineResult
        get() = PricingCalculator.calculateLine(
            PricingCalculator.LineInput(
                weightGrams = weightGrams,
                pricePerGram = pricePerGram,
                laborPercent = laborPercent,
                profitPercent = profitPercent,
                taxPercent = taxPercent,
                quantity = quantity
            )
        )
}
