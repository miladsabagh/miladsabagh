package com.goldshop.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.goldshop.app.data.model.Customer
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.InvoiceItem
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ShopSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :qty WHERE id = :id AND stockQuantity >= :qty")
    suspend fun decreaseStock(id: Long, qty: Int): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Query("SELECT COUNT(*) FROM customers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItems(invoiceId: Long): List<InvoiceItem>

    @Query("SELECT COUNT(*) FROM invoices")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0) FROM invoices WHERE createdAt >= :from")
    fun observeSalesSince(from: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    @Delete
    suspend fun delete(invoice: Invoice)

    @Transaction
    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val id = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = id) })
        return id
    }
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = 1")
    fun observe(): Flow<ShopSettings?>

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun get(): ShopSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ShopSettings)

    @Update
    suspend fun update(settings: ShopSettings)
}
