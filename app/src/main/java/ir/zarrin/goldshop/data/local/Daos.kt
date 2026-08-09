package ir.zarrin.goldshop.data.local

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
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): Product?

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET stock = MAX(stock - :amount, 0) WHERE id = :id")
    suspend fun decreaseStock(id: Long, amount: Int)
}

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun findById(id: Long): Customer?

    @Query("SELECT COUNT(*) FROM customers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun findWithItems(id: Long): InvoiceWithItems?

    @Query("SELECT * FROM invoices WHERE dateMillis >= :from AND dateMillis < :to ORDER BY dateMillis DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<Invoice>>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    @Query("SELECT number FROM invoices ORDER BY id DESC LIMIT 1")
    suspend fun lastNumber(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsOf(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = if (invoice.id == 0L) {
            insertInvoice(invoice)
        } else {
            updateInvoice(invoice)
            invoice.id
        }
        deleteItemsOf(invoiceId)
        insertItems(
            items.mapIndexed { index, item ->
                item.copy(id = 0L, invoiceId = invoiceId, position = index)
            }
        )
        return invoiceId
    }
}
