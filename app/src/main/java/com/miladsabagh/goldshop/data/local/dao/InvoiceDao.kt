package com.miladsabagh.goldshop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    val invoice: Invoice,
    val items: List<InvoiceItem>
)

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY issuedAt DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Query(
        "SELECT * FROM invoices WHERE customerName LIKE '%' || :query || '%' " +
            "OR customerPhone LIKE '%' || :query || '%' " +
            "OR CAST(invoiceNumber AS TEXT) LIKE '%' || :query || '%' " +
            "ORDER BY issuedAt DESC"
    )
    fun search(query: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoice(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItems(invoiceId: Long): List<InvoiceItem>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun observeItems(invoiceId: Long): Flow<List<InvoiceItem>>

    @Query("SELECT COALESCE(MAX(invoiceNumber), 1000) + 1 FROM invoices")
    suspend fun nextInvoiceNumber(): Long

    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Transaction
    suspend fun createInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = insertInvoice(invoice)
        insertItems(items.map { it.copy(invoiceId = invoiceId) })
        return invoiceId
    }

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Transaction
    suspend fun deleteInvoiceWithItems(invoice: Invoice) {
        deleteItemsForInvoice(invoice.id)
        deleteInvoice(invoice)
    }

    @Query("SELECT COUNT(*) FROM invoices WHERE issuedAt >= :startOfDay")
    fun observeTodayCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM invoices WHERE issuedAt >= :startOfDay")
    fun observeTodayTotal(startOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalWeightGrams), 0) FROM invoices WHERE issuedAt >= :startOfDay")
    fun observeTodayWeight(startOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM invoices WHERE issuedAt BETWEEN :start AND :end")
    suspend fun sumTotalBetween(start: Long, end: Long): Double

    @Query("SELECT COALESCE(SUM(totalWeightGrams), 0) FROM invoices WHERE issuedAt BETWEEN :start AND :end")
    suspend fun sumWeightBetween(start: Long, end: Long): Double

    @Query("SELECT COUNT(*) FROM invoices WHERE issuedAt BETWEEN :start AND :end")
    suspend fun countBetween(start: Long, end: Long): Int
}
