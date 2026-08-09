package com.zarfam.goldshop.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(product: Product)

    @Delete
    suspend fun delete(product: Product)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun observeAll(): Flow<List<Customer>>

    @Upsert
    suspend fun upsert(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<ShopSettings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): ShopSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ShopSettings)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Query("SELECT COALESCE(MAX(invoiceNumber), 1000) + 1 FROM invoices")
    suspend fun nextInvoiceNumber(): Long

    @Query("SELECT COALESCE(SUM(grandTotal), 0) FROM invoices WHERE dateMillis >= :startMillis")
    fun observeSalesSince(startMillis: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM invoices")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("UPDATE products SET stockCount = MAX(stockCount - :qty, 0) WHERE id = :productId")
    suspend fun decrementStock(productId: Long, qty: Int)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsOf(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun deleteInvoiceById(invoiceId: Long)

    @Transaction
    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val id = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = id) })
        items.forEach { item ->
            item.productId?.let { decrementStock(it, item.quantity) }
        }
        return id
    }

    @Transaction
    suspend fun deleteInvoice(invoiceId: Long) {
        deleteItemsOf(invoiceId)
        deleteInvoiceById(invoiceId)
    }
}
