package com.zarin.goldshop.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val invoiceDao = db.invoiceDao()
    private val settingsDao = db.settingsDao()

    val products: Flow<List<Product>> = productDao.observeAll()
    val customers: Flow<List<Customer>> = customerDao.observeAll()
    val invoices: Flow<List<InvoiceWithItems>> = invoiceDao.observeAll()
    val settings: Flow<AppSettings?> = settingsDao.observe()

    suspend fun upsertProduct(product: Product) { productDao.upsert(product) }
    suspend fun deleteProduct(product: Product) { productDao.delete(product) }

    suspend fun upsertCustomer(customer: Customer): Long = customerDao.upsert(customer)
    suspend fun deleteCustomer(customer: Customer) { customerDao.delete(customer) }

    fun observeInvoice(id: Long): Flow<InvoiceWithItems?> = invoiceDao.observeById(id)

    suspend fun getSettings(): AppSettings = settingsDao.get() ?: AppSettings().also { settingsDao.upsert(it) }

    suspend fun saveSettings(settings: AppSettings) { settingsDao.upsert(settings) }

    /** Persists an invoice with its items, decrements stock and bumps the invoice sequence. */
    suspend fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        stockUpdates: List<Pair<Long, Int>>,
        newSeq: Int,
    ): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        invoiceDao.insertItems(items.map { it.copy(invoiceId = invoiceId) })
        stockUpdates.forEach { (productId, qty) -> productDao.decrementStock(productId, qty) }
        val current = getSettings()
        settingsDao.upsert(current.copy(lastInvoiceSeq = newSeq))
        return invoiceId
    }

    suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteItems(id)
        invoiceDao.deleteInvoice(id)
    }
}
