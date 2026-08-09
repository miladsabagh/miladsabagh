package com.goldjewelry.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.goldjewelry.app.data.model.Invoice
import com.goldjewelry.app.data.model.InvoiceItem
import com.goldjewelry.app.data.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItems(invoiceId: Long): List<InvoiceItem>

    @Transaction
    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? {
        val invoice = getById(id) ?: return null
        val items = getItems(id)
        return InvoiceWithItems(invoice, items)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("SELECT COUNT(*) FROM invoices")
    fun count(): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0) FROM invoices WHERE status != 'CANCELLED'")
    fun totalSales(): Flow<Long>

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 5): Flow<List<Invoice>>
}
