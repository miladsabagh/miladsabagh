package com.miladsabagh.zarrin.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity): Long

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: CustomerEntity): Long

    @Delete
    suspend fun delete(customer: CustomerEntity)
}

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeAllWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Query("SELECT COALESCE(MAX(invoiceNumber), 1000) FROM invoices")
    suspend fun lastInvoiceNumber(): Long

    @Insert
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Transaction
    suspend fun insertFull(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val invoiceId = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = invoiceId) })
        return invoiceId
    }

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(grandTotal), 0) FROM invoices WHERE createdAt >= :fromMillis")
    suspend fun salesSince(fromMillis: Long): Long

    @Query("SELECT COUNT(*) FROM invoices WHERE createdAt >= :fromMillis")
    suspend fun countSince(fromMillis: Long): Int
}
