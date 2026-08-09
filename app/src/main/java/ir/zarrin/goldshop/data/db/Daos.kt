package ir.zarrin.goldshop.data.db

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

    @Query(
        """
        SELECT * FROM products
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%')
          AND (:category = '' OR category = :category)
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String, category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stockQty), 0) FROM products")
    fun observeTotalStock(): Flow<Int>

    @Query("SELECT COALESCE(SUM(weightGrams * stockQty), 0) FROM products WHERE pricingMode = 'BY_WEIGHT'")
    fun observeTotalWeight(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET stockQty = MAX(0, stockQty - :amount) WHERE id = :id")
    suspend fun decreaseStock(id: Long, amount: Int)

    @Query("UPDATE products SET stockQty = stockQty + :amount WHERE id = :id")
    suspend fun increaseStock(id: Long, amount: Int)
}

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query(
        """
        SELECT * FROM customers
        WHERE :query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun search(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun findById(id: Long): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)
}

@Dao
interface InvoiceDao {

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM invoices
        WHERE (:query = '' OR number LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%')
          AND (:status = '' OR status = :status)
        ORDER BY dateMillis DESC
        """
    )
    fun search(query: String, status: String): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeById(id: Long): Flow<InvoiceWithItems?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY dateMillis DESC")
    fun observeByCustomer(customerId: Long): Flow<List<InvoiceWithItems>>

    @Query("SELECT * FROM invoices WHERE dateMillis BETWEEN :from AND :to ORDER BY dateMillis DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<InvoiceEntity>>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun findById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun itemsOf(invoiceId: Long): List<InvoiceItemEntity>

    @Insert
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE invoices SET paidAmount = :paid, status = :status WHERE id = :id")
    suspend fun updatePayment(id: Long, paid: Long, status: String)
}
