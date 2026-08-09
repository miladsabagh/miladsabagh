package com.zarrin.goldshop.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProductCategory {
    RING,
    NECKLACE,
    BRACELET,
    EARRING,
    PENDANT,
    COIN,
    BULLION,
    OTHER
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ProductCategory,
    val weightGrams: Double,
    val purityKarat: Int,
    val makingFeePercent: Double,
    val stockCount: Int,
    val code: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val nationalId: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "زرین گالری",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val goldPrice18PerGram: Long = 5_800_000L,
    val profitPercent: Double = 7.0,
    val vatPercent: Double = 10.0,
    val invoicePrefix: String = "INV"
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String = "",
    val goldPrice18PerGram: Long,
    val profitPercent: Double,
    val vatPercent: Double,
    val subtotal: Long,
    val profitAmount: Long,
    val vatAmount: Long,
    val totalAmount: Long,
    val paidAmount: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
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
    val productId: Long?,
    val productName: String,
    val productCode: String,
    val weightGrams: Double,
    val purityKarat: Int,
    val makingFeePercent: Double,
    val unitGoldPrice: Long,
    val lineTotal: Long
)

data class InvoiceWithItems(
    val invoice: InvoiceEntity,
    val items: List<InvoiceItemEntity>
)

data class DashboardStats(
    val todaySales: Long,
    val monthSales: Long,
    val invoiceCount: Int,
    val productCount: Int,
    val lowStockCount: Int
)
