package com.goldgallery.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category, name")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(items: List<ProductEntity>)

    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty")
    suspend fun decreaseStock(id: Long, qty: Int)
}

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY number DESC")
    fun observeAllWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Query("SELECT IFNULL(MAX(number), 1000) FROM invoices")
    suspend fun maxNumber(): Long

    @Insert
    suspend fun insertInvoice(entity: InvoiceEntity): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItemEntity>)
}
