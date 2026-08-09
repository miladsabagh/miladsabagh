package com.miladsabagh.zarrin.data

import android.content.Context
import com.miladsabagh.zarrin.data.db.AppDatabase
import com.miladsabagh.zarrin.data.db.CustomerEntity
import com.miladsabagh.zarrin.data.db.InvoiceEntity
import com.miladsabagh.zarrin.data.db.InvoiceItemEntity
import com.miladsabagh.zarrin.data.db.InvoiceWithItems
import com.miladsabagh.zarrin.data.db.ProductEntity
import com.miladsabagh.zarrin.util.priceLine
import kotlinx.coroutines.flow.Flow

data class NewInvoiceItem(
    val productId: Long?,
    val title: String,
    val karat: Int,
    val weightGrams: Double,
    val wagePerGram: Long
)

class Repository(context: Context) {

    private val db = AppDatabase.get(context)
    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val invoiceDao = db.invoiceDao()

    val products: Flow<List<ProductEntity>> = productDao.observeAll()
    val customers: Flow<List<CustomerEntity>> = customerDao.observeAll()
    val invoices: Flow<List<InvoiceWithItems>> = invoiceDao.observeAllWithItems()

    fun invoice(id: Long): Flow<InvoiceWithItems?> = invoiceDao.observeWithItems(id)

    suspend fun saveProduct(product: ProductEntity): Long = productDao.upsert(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.delete(product)

    suspend fun saveCustomer(customer: CustomerEntity): Long = customerDao.upsert(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.delete(customer)

    suspend fun todaySales(): Pair<Long, Int> {
        val startOfDay = startOfTodayMillis()
        return invoiceDao.salesSince(startOfDay) to invoiceDao.countSince(startOfDay)
    }

    suspend fun totalSales(): Pair<Long, Int> =
        invoiceDao.salesSince(0L) to invoiceDao.countSince(0L)

    suspend fun createInvoice(
        customer: CustomerEntity?,
        items: List<NewInvoiceItem>,
        settings: ShopSettings
    ): Long {
        require(items.isNotEmpty()) { "Invoice must contain at least one item" }

        val lines = items.map { item ->
            val breakdown = priceLine(
                basePrice18kPerGram = settings.goldPricePerGram18k,
                karat = item.karat,
                weightGrams = item.weightGrams,
                wagePerGram = item.wagePerGram,
                profitPercent = settings.profitPercent,
                taxPercent = settings.taxPercent
            )
            item to breakdown
        }

        val invoiceNumber = invoiceDao.lastInvoiceNumber() + 1
        val invoice = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customer?.name ?: "مشتری حضوری",
            customerPhone = customer?.phone ?: "-",
            goldPricePerGram18k = settings.goldPricePerGram18k,
            profitPercent = settings.profitPercent,
            taxPercent = settings.taxPercent,
            goldValue = lines.sumOf { it.second.goldValue },
            wageTotal = lines.sumOf { it.second.wageAmount },
            profitAmount = lines.sumOf { it.second.profitAmount },
            taxAmount = lines.sumOf { it.second.taxAmount },
            grandTotal = lines.sumOf { it.second.lineTotal }
        )

        val itemEntities = lines.map { (item, breakdown) ->
            InvoiceItemEntity(
                invoiceId = 0,
                productId = item.productId,
                title = item.title,
                karat = item.karat,
                weightGrams = item.weightGrams,
                wagePerGram = item.wagePerGram,
                goldPricePerGram = breakdown.unitGoldPrice,
                goldValue = breakdown.goldValue,
                wageAmount = breakdown.wageAmount,
                profitAmount = breakdown.profitAmount,
                taxAmount = breakdown.taxAmount,
                lineTotal = breakdown.lineTotal
            )
        }

        return invoiceDao.insertFull(invoice, itemEntities)
    }

    suspend fun deleteInvoice(id: Long) = invoiceDao.deleteById(id)

    private fun startOfTodayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
