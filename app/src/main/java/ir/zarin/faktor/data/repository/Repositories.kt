package ir.zarin.faktor.data.repository

import androidx.room.withTransaction
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.data.local.AppDatabase
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.domain.InvoiceNumbering
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val database: AppDatabase) {

    private val dao = database.productDao()

    fun observeAll(): Flow<List<Product>> = dao.observeAll()

    fun search(query: String): Flow<List<Product>> = dao.search(query.trim())

    suspend fun getById(id: Long): Product? = dao.getById(id)

    suspend fun save(product: Product): Long = dao.upsert(product)

    suspend fun delete(product: Product) = dao.delete(product)
}

class CustomerRepository(private val database: AppDatabase) {

    private val dao = database.customerDao()

    fun observeAll(): Flow<List<Customer>> = dao.observeAll()

    fun search(query: String): Flow<List<Customer>> = dao.search(query.trim())

    suspend fun getById(id: Long): Customer? = dao.getById(id)

    suspend fun save(customer: Customer): Long = dao.upsert(customer)

    suspend fun delete(customer: Customer) = dao.delete(customer)
}

class InvoiceRepository(private val database: AppDatabase) {

    private val dao = database.invoiceDao()
    private val productDao = database.productDao()

    fun observeAll(): Flow<List<Invoice>> = dao.observeAll()

    fun observeRecent(limit: Int = 5): Flow<List<Invoice>> = dao.observeRecent(limit)

    fun search(query: String): Flow<List<Invoice>> = dao.search(query.trim())

    fun observeWithItems(id: Long): Flow<InvoiceWithItems?> = dao.observeWithItems(id)

    suspend fun getWithItems(id: Long): InvoiceWithItems? = dao.getWithItems(id)

    fun observeSalesSince(fromMillis: Long): Flow<Long> = dao.observeSalesSince(fromMillis)

    fun observeCountSince(fromMillis: Long): Flow<Int> = dao.observeCountSince(fromMillis)

    fun observeOutstanding(): Flow<Long> = dao.observeOutstanding()

    suspend fun updatePaidAmount(id: Long, paidRial: Long) = dao.updatePaidAmount(id, paidRial)

    suspend fun delete(invoice: Invoice) = dao.delete(invoice)

    /**
     * فاکتور و اقلام آن را در یک تراکنش ثبت می‌کند، شماره فاکتور را تولید کرده و
     * موجودی کالاهای انبار را کاهش می‌دهد.
     */
    suspend fun createInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        invoicePrefix: String,
    ): Long = database.withTransaction {
        val date = JalaliCalendar.fromEpochMillis(invoice.dateMillis)
        val monthlyPrefix = InvoiceNumbering.monthlyPrefix(invoicePrefix, date)
        val lastNumber = dao.lastNumberWithPrefix(monthlyPrefix)
        val number = InvoiceNumbering.next(invoicePrefix, date, lastNumber)

        val invoiceId = dao.insertInvoice(invoice.copy(number = number))
        dao.insertItems(items.map { it.copy(id = 0, invoiceId = invoiceId) })
        items.forEach { item ->
            item.productId?.let { productId -> productDao.decreaseStock(productId, item.quantity) }
        }
        invoiceId
    }
}
