package com.goldjewelry.app.data.model

data class InvoiceWithItems(
    val invoice: Invoice,
    val items: List<InvoiceItem>
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val customPrice: Long? = null
) {
    fun lineTotal(goldPricePerGram: Long): Long {
        val unit = customPrice ?: GoldPriceCalculator.calculateProductPrice(product, goldPricePerGram)
        return unit * quantity
    }
}
