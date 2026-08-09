package com.miladsabagh.goldshop.data.repository

import com.miladsabagh.goldshop.data.local.dao.CustomerDao
import com.miladsabagh.goldshop.data.local.dao.GoldPriceDao
import com.miladsabagh.goldshop.data.local.dao.InvoiceDao
import com.miladsabagh.goldshop.data.local.dao.ProductDao
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.data.local.entity.GoldPriceEntry
import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for data operations. Keeps ViewModels free of DAO wiring.
 */
class GoldShopRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val goldPriceDao: GoldPriceDao
) {
    // Products
    fun observeProducts(): Flow<List<Product>> = productDao.observeAll()
    fun searchProducts(query: String): Flow<List<Product>> =
        if (query.isBlank()) productDao.observeAll() else productDao.search(query)

    suspend fun getProduct(id: Long) = productDao.getById(id)
    suspend fun saveProduct(product: Product): Long = productDao.upsert(product)
    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    // Customers
    fun observeCustomers(): Flow<List<Customer>> = customerDao.observeAll()
    fun searchCustomers(query: String): Flow<List<Customer>> =
        if (query.isBlank()) customerDao.observeAll() else customerDao.search(query)

    suspend fun getCustomer(id: Long) = customerDao.getById(id)
    suspend fun saveCustomer(customer: Customer): Long = customerDao.upsert(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    // Invoices
    fun observeInvoices(): Flow<List<Invoice>> = invoiceDao.observeAll()
    fun searchInvoices(query: String): Flow<List<Invoice>> =
        if (query.isBlank()) invoiceDao.observeAll() else invoiceDao.search(query)

    suspend fun getInvoice(id: Long) = invoiceDao.getInvoice(id)
    suspend fun getInvoiceItems(invoiceId: Long) = invoiceDao.getItems(invoiceId)
    suspend fun nextInvoiceNumber() = invoiceDao.nextInvoiceNumber()

    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = invoiceDao.createInvoiceWithItems(invoice, items)
        items.forEach { item ->
            item.productId?.let { productId ->
                productDao.decreaseStock(productId, item.quantity)
            }
        }
        return invoiceId
    }

    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoiceWithItems(invoice)

    fun observeTodayInvoiceCount(startOfDay: Long) = invoiceDao.observeTodayCount(startOfDay)
    fun observeTodayTotal(startOfDay: Long) = invoiceDao.observeTodayTotal(startOfDay)
    fun observeTodayWeight(startOfDay: Long) = invoiceDao.observeTodayWeight(startOfDay)
    suspend fun sumTotalBetween(start: Long, end: Long) = invoiceDao.sumTotalBetween(start, end)
    suspend fun sumWeightBetween(start: Long, end: Long) = invoiceDao.sumWeightBetween(start, end)
    suspend fun countBetween(start: Long, end: Long) = invoiceDao.countBetween(start, end)

    // Gold price
    fun observeLatestGoldPrice(): Flow<GoldPriceEntry?> = goldPriceDao.observeLatest()
    fun observeGoldPriceHistory(limit: Int = 30) = goldPriceDao.observeHistory(limit)
    suspend fun recordGoldPrice(pricePerGram: Double) =
        goldPriceDao.insert(GoldPriceEntry(pricePerGram18k = pricePerGram))
}
