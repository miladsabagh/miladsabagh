package ir.zarin.faktor.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Product>>

    @Query(
        """
        SELECT * FROM products
        WHERE :query = '' OR name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """,
    )
    fun search(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Upsert
    suspend fun upsert(product: Product): Long

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET stockQty = MAX(stockQty - :amount, 0) WHERE id = :id")
    suspend fun decreaseStock(id: Long, amount: Int)
}

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Customer>>

    @Query(
        """
        SELECT * FROM customers
        WHERE :query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun search(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Upsert
    suspend fun upsert(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Invoice>>

    @Query(
        """
        SELECT * FROM invoices
        WHERE :query = '' OR number LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%'
        ORDER BY dateMillis DESC, id DESC
        """,
    )
    fun search(query: String): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getWithItems(id: Long): InvoiceWithItems?

    @Query("SELECT COALESCE(SUM(grandTotalRial), 0) FROM invoices WHERE dateMillis >= :fromMillis")
    fun observeSalesSince(fromMillis: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM invoices WHERE dateMillis >= :fromMillis")
    fun observeCountSince(fromMillis: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(grandTotalRial - paidAmountRial), 0) FROM invoices
        WHERE grandTotalRial > paidAmountRial
        """,
    )
    fun observeOutstanding(): Flow<Long>

    @Query(
        """
        SELECT number FROM invoices WHERE number LIKE :prefix || '%'
        ORDER BY number DESC LIMIT 1
        """,
    )
    suspend fun lastNumberWithPrefix(prefix: String): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("UPDATE invoices SET paidAmountRial = :paidRial WHERE id = :id")
    suspend fun updatePaidAmount(id: Long, paidRial: Long)

    @Delete
    suspend fun delete(invoice: Invoice)
}
