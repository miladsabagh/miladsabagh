package com.miladsabagh.goldinvoice.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Query("SELECT COUNT(*) FROM invoices WHERE createdAt >= :sinceEpochMillis")
    fun observeCountSince(sinceEpochMillis: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0) FROM invoices WHERE createdAt >= :sinceEpochMillis")
    fun observeTotalSalesSince(sinceEpochMillis: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun countAll(): Int

    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItem>)

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = invoiceId) })
        return invoiceId
    }

    @Update
    suspend fun update(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)
}
