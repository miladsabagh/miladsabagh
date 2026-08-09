package ir.goldshop.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ir.goldshop.app.data.entity.Invoice
import ir.goldshop.app.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    val invoice: Invoice,
    val items: List<InvoiceItem>
)

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issuedAt DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE customerName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%' ORDER BY issuedAt DESC")
    fun search(query: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItem>

    @Transaction
    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? {
        val invoice = getInvoiceById(id) ?: return null
        val items = getItemsForInvoice(id)
        return InvoiceWithItems(invoice, items)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT COUNT(*) FROM invoices")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM invoices WHERE issuedAt >= :startOfDay")
    fun observeTodayTotalSales(startOfDay: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM invoices WHERE issuedAt >= :startOfDay")
    fun observeTodayInvoiceCount(startOfDay: Long): Flow<Int>

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = insertInvoice(invoice)
        val itemsWithInvoiceId = items.map { it.copy(invoiceId = invoiceId) }
        insertItems(itemsWithInvoiceId)
        return invoiceId
    }
}
