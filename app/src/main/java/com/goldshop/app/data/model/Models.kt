package com.goldshop.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProductCategory(val labelFa: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    EARRING("گوشواره"),
    PENDANT("آویز"),
    SET("سرویس"),
    COIN("سکه"),
    BULLION("شمش"),
    OTHER("سایر")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val weightGrams: Double,
    val purity: Int = 750, // عیار (مثلاً ۷۵۰ = ۱۸ عیار)
    val laborPercent: Double = 10.0, // اجرت به درصد
    val profitPercent: Double = 7.0, // سود به درصد
    val stockQuantity: Int = 1,
    val description: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val nationalId: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "طلا و جواهر سعدی",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val goldPricePerGram18: Long = 3_850_000L, // قیمت هر گرم طلای ۱۸ عیار (ریال)
    val taxPercent: Double = 0.0,
    val invoicePrefix: String = "INV"
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("customerId")]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "مشتری حضوری",
    val customerPhone: String = "",
    val goldPricePerGram18: Long,
    val subtotal: Long,
    val discount: Long = 0,
    val taxAmount: Long = 0,
    val total: Long,
    val paidAmount: Long = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val productName: String,
    val categoryLabel: String,
    val weightGrams: Double,
    val purity: Int,
    val laborPercent: Double,
    val profitPercent: Double,
    val unitPrice: Long,
    val lineTotal: Long,
    val quantity: Int = 1
)

data class InvoiceWithItems(
    val invoice: Invoice,
    val items: List<InvoiceItem>
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val customLaborPercent: Double? = null,
    val customProfitPercent: Double? = null
)

data class DashboardStats(
    val productCount: Int = 0,
    val customerCount: Int = 0,
    val invoiceCount: Int = 0,
    val todaySales: Long = 0,
    val monthSales: Long = 0
)
