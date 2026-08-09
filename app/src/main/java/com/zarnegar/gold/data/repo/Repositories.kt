package com.zarnegar.gold.data.repo

import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.data.db.CustomerDao
import com.zarnegar.gold.data.db.InvoiceDao
import com.zarnegar.gold.data.db.ProductDao
import com.zarnegar.gold.data.db.toDomain
import com.zarnegar.gold.data.db.toEntity
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val dao: ProductDao) {
    val products: Flow<List<Product>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun find(id: Long): Product? = dao.findById(id)?.toDomain()

    suspend fun upsert(product: Product): Long =
        if (product.id == 0L) dao.insert(product.toEntity()) else {
            dao.update(product.toEntity()); product.id
        }

    suspend fun delete(product: Product) = dao.delete(product.toEntity())

    suspend fun decreaseStock(productId: Long, amount: Int) = dao.decreaseStock(productId, amount)
}

class CustomerRepository(private val dao: CustomerDao) {
    val customers: Flow<List<Customer>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun find(id: Long): Customer? = dao.findById(id)?.toDomain()

    suspend fun upsert(customer: Customer): Long =
        if (customer.id == 0L) dao.insert(customer.toEntity()) else {
            dao.update(customer.toEntity()); customer.id
        }

    suspend fun delete(customer: Customer) = dao.delete(customer.toEntity())
}

class InvoiceRepository(
    private val dao: InvoiceDao,
    private val productDao: ProductDao,
) {
    val invoices: Flow<List<Invoice>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeInvoice(id: Long): Flow<Invoice?> = dao.observeById(id).map { entity ->
        entity?.let { it.toDomain(dao.findLines(it.id).map { line -> line.toDomain() }) }
    }

    suspend fun find(id: Long): Invoice? {
        val entity = dao.findById(id) ?: return null
        return entity.toDomain(dao.findLines(id).map { it.toDomain() })
    }

    suspend fun save(invoice: Invoice, lines: List<InvoiceLine>): Long {
        val id = dao.insertWithLines(invoice.toEntity(), lines.map { it.toEntity(0) })
        lines.forEach { line ->
            line.productId?.let { productDao.decreaseStock(it, line.quantity) }
        }
        return id
    }

    suspend fun update(invoice: Invoice) = dao.update(invoice.toEntity())

    suspend fun delete(id: Long) = dao.deleteById(id)

    /** شمارهٔ فاکتور بعدی، به شکل `۱۴۰۳-۰۰۱۲` بر اساس سال جاری شمسی. */
    suspend fun nextInvoiceNumber(nowMillis: Long = System.currentTimeMillis()): String {
        val year = JalaliDate.fromEpochMillis(nowMillis).year
        val sequence = (dao.maxId() ?: 0L) + 1
        return "%04d-%04d".format(year, sequence)
    }
}
