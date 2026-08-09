package ir.goldshop.app.data.repository

import ir.goldshop.app.data.AppDatabase
import ir.goldshop.app.data.dao.InvoiceWithItems
import ir.goldshop.app.data.entity.Customer
import ir.goldshop.app.data.entity.Invoice
import ir.goldshop.app.data.entity.InvoiceItem
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.data.entity.ShopSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class GoldShopRepository(private val database: AppDatabase) {

    // ----- Customers -----
    fun observeCustomers(): Flow<List<Customer>> = database.customerDao().observeAll()
    fun searchCustomers(query: String): Flow<List<Customer>> =
        if (query.isBlank()) observeCustomers() else database.customerDao().search(query)

    suspend fun getCustomer(id: Long): Customer? = database.customerDao().getById(id)
    suspend fun saveCustomer(customer: Customer): Long = database.customerDao().upsert(customer)
    suspend fun updateCustomer(customer: Customer) = database.customerDao().update(customer)
    suspend fun deleteCustomer(customer: Customer) = database.customerDao().delete(customer)
    fun observeCustomerCount(): Flow<Int> = database.customerDao().observeCount()

    // ----- Products -----
    fun observeProducts(): Flow<List<Product>> = database.productDao().observeAll()
    fun searchProducts(query: String): Flow<List<Product>> =
        if (query.isBlank()) observeProducts() else database.productDao().search(query)

    suspend fun getProduct(id: Long): Product? = database.productDao().getById(id)
    suspend fun saveProduct(product: Product): Long = database.productDao().upsert(product)
    suspend fun updateProduct(product: Product) = database.productDao().update(product)
    suspend fun deleteProduct(product: Product) = database.productDao().delete(product)
    fun observeProductCount(): Flow<Int> = database.productDao().observeCount()
    suspend fun decrementStock(productId: Long, amount: Int) =
        database.productDao().decrementStock(productId, amount)

    // ----- Invoices -----
    fun observeInvoices(): Flow<List<Invoice>> = database.invoiceDao().observeAll()
    fun searchInvoices(query: String): Flow<List<Invoice>> =
        if (query.isBlank()) observeInvoices() else database.invoiceDao().search(query)

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? =
        database.invoiceDao().getInvoiceWithItems(id)

    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val id = database.invoiceDao().insertInvoiceWithItems(invoice, items)
        database.settingsDao().incrementInvoiceNumber()
        for (item in items) {
            val productId = item.productId
            if (productId != null) {
                decrementStock(productId, item.quantity)
            }
        }
        return id
    }

    suspend fun deleteInvoice(invoice: Invoice) = database.invoiceDao().deleteInvoice(invoice)
    fun observeInvoiceCount(): Flow<Int> = database.invoiceDao().observeCount()

    fun observeTodayTotalSales(): Flow<Double> =
        database.invoiceDao().observeTodayTotalSales(startOfTodayMillis())

    fun observeTodayInvoiceCount(): Flow<Int> =
        database.invoiceDao().observeTodayInvoiceCount(startOfTodayMillis())

    private fun startOfTodayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ----- Settings -----
    fun observeSettings(): Flow<ShopSettings> =
        database.settingsDao().observe().map { it ?: ShopSettings() }

    suspend fun getSettingsOrDefault(): ShopSettings =
        database.settingsDao().get() ?: ShopSettings().also { database.settingsDao().upsert(it) }

    suspend fun saveSettings(settings: ShopSettings) = database.settingsDao().upsert(settings)

    suspend fun generateNextInvoiceNumber(): String {
        val settings = getSettingsOrDefault()
        val year = ir.goldshop.app.util.PersianDate.fromTimestamp(System.currentTimeMillis()).year
        return "$year-${settings.nextInvoiceNumber}"
    }
}
