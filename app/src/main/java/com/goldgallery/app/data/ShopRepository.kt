package com.goldgallery.app.data

import com.goldgallery.app.data.db.AppDatabase
import com.goldgallery.app.data.db.InvoiceEntity
import com.goldgallery.app.data.db.InvoiceItemEntity
import com.goldgallery.app.data.model.CartItem
import com.goldgallery.app.logic.GoldCalculator
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()

    val products = productDao.observeAll()
    val invoices = invoiceDao.observeAllWithItems()

    fun invoice(id: Long) = invoiceDao.observeWithItems(id)

    suspend fun seedIfEmpty() {
        if (productDao.count() == 0) {
            productDao.insertAll(SampleData.products)
        }
    }

    suspend fun issueInvoice(
        customerName: String,
        customerPhone: String,
        goldPrice18: Long,
        cart: List<CartItem>,
    ): Long {
        val number = invoiceDao.maxNumber() + 1
        val invoiceId = invoiceDao.insertInvoice(
            InvoiceEntity(
                number = number,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                createdAt = System.currentTimeMillis(),
                goldPrice18PerGram = goldPrice18,
            )
        )
        val items = cart.map { cartItem ->
            val p = cartItem.product
            val price = GoldCalculator.itemPrice(
                weightGrams = p.weightGrams,
                karat = p.karat,
                wagePerGram = p.wagePerGram,
                base18PerGram = goldPrice18,
                quantity = cartItem.quantity,
            )
            InvoiceItemEntity(
                invoiceId = invoiceId,
                productName = p.name,
                category = p.category,
                weightGrams = p.weightGrams,
                karat = p.karat,
                wagePerGram = p.wagePerGram,
                quantity = cartItem.quantity,
                goldRaw = price.goldRaw,
                wage = price.wage,
                profit = price.profit,
                tax = price.tax,
                total = price.total,
            )
        }
        invoiceDao.insertItems(items)
        cart.forEach { productDao.decreaseStock(it.product.id, it.quantity) }
        return invoiceId
    }
}
