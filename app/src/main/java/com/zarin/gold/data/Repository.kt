package com.zarin.gold.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZarinRepository(private val db: ZarinDatabase) {
    private val _goldPrice18 = MutableStateFlow(SeedData.DEFAULT_GOLD_PRICE_18)
    val goldPrice18 = _goldPrice18.asStateFlow()

    val products: Flow<List<Product>> = db.productDao().observeAll()
    val customers: Flow<List<Customer>> = db.customerDao().observeAll()
    val invoices: Flow<List<Invoice>> = db.invoiceDao().observeAll()

    suspend fun seedIfNeeded() {
        if (db.productDao().count() == 0) {
            db.productDao().insertAll(SeedData.products)
        }
        if (db.customerDao().count() == 0) {
            db.customerDao().insertAll(SeedData.customers)
        }
    }

    fun updateGoldPrice(price: Long) {
        _goldPrice18.value = price.coerceAtLeast(1)
    }

    suspend fun addCustomer(name: String, phone: String): Long =
        db.customerDao().insert(Customer(name = name.trim(), phone = phone.trim()))

    suspend fun createInvoice(
        customerName: String,
        customerPhone: String,
        lines: List<InvoiceLine>,
        discount: Long,
        note: String
    ): Long {
        val count = db.invoiceDao().count() + 1
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val invoice = Invoice(
            invoiceNumber = "ZR-$date-${count.toString().padStart(3, '0')}",
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            goldPricePerGram18 = _goldPrice18.value,
            lines = lines,
            discount = discount.coerceAtLeast(0),
            note = note.trim()
        )
        return db.invoiceDao().insert(invoice)
    }

    suspend fun getInvoice(id: Long): Invoice? = db.invoiceDao().getById(id)

    fun priceForCarat(carat: Int, base18: Long = _goldPrice18.value): Long {
        // تبدیل تقریبی نسبت به طلای ۱۸
        return ((base18.toDouble() * carat) / 18.0).toLong()
    }

    fun lineFromProduct(product: Product, quantity: Int = 1): InvoiceLine {
        val unit = priceForCarat(product.carat)
        return InvoiceLine(
            productId = product.id,
            productName = product.name,
            carat = product.carat,
            weightGram = product.weightGram,
            unitGoldPrice = unit,
            makingFeePercent = product.makingFeePercent,
            stonePrice = product.stonePrice,
            quantity = quantity
        )
    }
}
