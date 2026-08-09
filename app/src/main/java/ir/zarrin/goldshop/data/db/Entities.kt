package ir.zarrin.goldshop.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "products", indices = [Index(value = ["code"], unique = true)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String,
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val wagePercent: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Long = 0L,
    val pricingMode: String = "BY_WEIGHT",
    val fixedPrice: Long = 0L,
    val taxable: Boolean = true,
    val stockQty: Int = 1,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val nationalId: String = "",
    val address: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices", indices = [Index(value = ["number"], unique = true)])
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    /** نرخ طلای ۱۸ عیار در لحظه صدور فاکتور */
    val goldRate18: Long = 0L,
    val taxPercent: Double = 0.0,
    val totalGoldValue: Long = 0L,
    val totalWage: Long = 0L,
    val totalProfit: Long = 0L,
    val totalStone: Long = 0L,
    val totalTax: Long = 0L,
    val itemsDiscount: Long = 0L,
    val subtotal: Long = 0L,
    val invoiceDiscount: Long = 0L,
    val grandTotal: Long = 0L,
    val paidAmount: Long = 0L,
    val totalWeight: Double = 0.0,
    val paymentMethod: String = "CASH",
    val status: String = "PAID",
    val note: String = ""
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val name: String,
    val category: String,
    val karat: Int,
    val weightGrams: Double,
    val quantity: Int,
    val pricingMode: String,
    val ratePerGram: Long,
    val wagePercent: Double,
    val profitPercent: Double,
    val stonePrice: Long,
    val fixedPrice: Long,
    val taxable: Boolean,
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val unitPrice: Long,
    val discount: Long,
    val lineTotal: Long
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItemEntity>
)