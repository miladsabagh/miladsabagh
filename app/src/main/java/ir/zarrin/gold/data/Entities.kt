package ir.zarrin.gold.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/** دسته‌بندی‌های رایج کالای طلا و جواهر. */
val PRODUCT_CATEGORIES = listOf(
    "انگشتر", "گردنبند", "دستبند", "گوشواره", "النگو",
    "سرویس", "زنجیر", "آویز", "سکه", "متفرقه"
)

val KARATS = listOf(18, 21, 22, 24)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val karat: Int,
    val weightGrams: Double,
    val wagePercent: Double,
    val stock: Int,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: Long,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val date: Long = System.currentTimeMillis(),
    val goldPricePerGram18k: Long,
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val discount: Long = 0,
    val total: Long,
    val paid: Boolean = false,
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val name: String,
    val karat: Int,
    val weightGrams: Double,
    val quantity: Int,
    val wagePercent: Double,
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val lineTotal: Long,
)

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItem>,
)
