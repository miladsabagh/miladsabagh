package com.zarnegar.gold.data.db

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
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET stock = MAX(0, stock - :amount) WHERE id = :id")
    suspend fun decreaseStock(id: Long, amount: Int)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY fullName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun findById(id: Long): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun findById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId ORDER BY id ASC")
    suspend fun findLines(invoiceId: Long): List<InvoiceLineEntity>

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId ORDER BY id ASC")
    fun observeLines(invoiceId: Long): Flow<List<InvoiceLineEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeById(id: Long): Flow<InvoiceEntity?>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    @Query("SELECT MAX(id) FROM invoices")
    suspend fun maxId(): Long?

    @Insert
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert
    suspend fun insertLines(lines: List<InvoiceLineEntity>)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun insertWithLines(invoice: InvoiceEntity, lines: List<InvoiceLineEntity>): Long {
        val invoiceId = insertInvoice(invoice)
        insertLines(lines.map { it.copy(invoiceId = invoiceId) })
        return invoiceId
    }
}
