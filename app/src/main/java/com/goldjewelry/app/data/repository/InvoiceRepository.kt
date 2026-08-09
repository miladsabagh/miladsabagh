package com.goldjewelry.app.data.repository

import com.goldjewelry.app.data.database.InvoiceDao
import com.goldjewelry.app.data.model.CartItem
import com.goldjewelry.app.data.model.Customer
import com.goldjewelry.app.data.model.Invoice
import com.goldjewelry.app.data.model.InvoiceItem
import com.goldjewelry.app.data.model.InvoiceStatus
import com.goldjewelry.app.data.model.InvoiceWithItems
import com.goldjewelry.app.data.model.ShopSettings
import com.goldjewelry.app.util.GoldPriceCalculator
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {

    fun getAll(): Flow<List<Invoice>> = invoiceDao.getAll()

    fun getRecent(limit: Int = 5): Flow<List<Invoice>> = invoiceDao.getRecent(limit)

    fun count(): Flow<Int> = invoiceDao.count()

    fun totalSales(): Flow<Long> = invoiceDao.totalSales()

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItems(id)

    suspend fun createInvoice(
        cartItems: List<CartItem>,
        customer: Customer?,
        customerName: String,
        customerPhone: String,
        settings: ShopSettings,
        discount: Long = 0,
        notes: String = ""
    ): Long {
        val goldPrice = settings.goldPricePerGram
        val subtotal = cartItems.sumOf { it.lineTotal(goldPrice) }
        val tax = ((subtotal - discount) * settings.taxPercent / 100.0).toLong()
        val total = subtotal - discount + tax

        val nextNumber = settings.lastInvoiceNumber + 1
        val invoiceNumber = "${settings.invoicePrefix}-$nextNumber"

        val invoice = Invoice(
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customerName,
            customerPhone = customerPhone,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total,
            goldPricePerGram = goldPrice,
            status = InvoiceStatus.ISSUED,
            notes = notes
        )

        val invoiceId = invoiceDao.insertInvoice(invoice)

        val items = cartItems.map { cartItem ->
            val product = cartItem.product
            val unitPrice = cartItem.customPrice
                ?: GoldPriceCalculator.calculateProductPrice(product, goldPrice)
            val goldValue = GoldPriceCalculator.calculateGoldValue(
                product.weightGrams, product.karat, goldPrice
            )
            val makingCharge = unitPrice - goldValue

            InvoiceItem(
                invoiceId = invoiceId,
                productId = product.id,
                productName = product.name,
                category = product.category,
                weightGrams = product.weightGrams,
                karat = product.karat,
                unitPrice = unitPrice,
                quantity = cartItem.quantity,
                makingCharge = makingCharge.coerceAtLeast(0),
                lineTotal = unitPrice * cartItem.quantity
            )
        }

        invoiceDao.insertItems(items)
        return invoiceId
    }
}
