package com.zarfam.goldshop.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ProductCategory(val fa: String) {
    RING("انگشتر"),
    NECKLACE("گردن‌بند"),
    BRACELET("دستبند"),
    BANGLE("النگو"),
    EARRING("گوشواره"),
    CHAIN("زنجیر"),
    COIN("سکه"),
    SET("نیم‌ست"),
    OTHER("سایر");

    companion object {
        fun fromName(name: String): ProductCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = ProductCategory.OTHER.name,
    val code: String = "",
    val weightGrams: Double,
    val karat: Int = 18,
    val wagePercent: Double = 0.0,
    val profitPercent: Double = 7.0,
    val stockCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val nationalId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: Long,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val goldPricePerGram18: Long,
    val taxPercent: Double,
    val itemsTotal: Long,
    val discount: Long = 0,
    val grandTotal: Long,
    val note: String = "",
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val name: String,
    val weightGrams: Double,
    val karat: Int,
    val wagePercent: Double,
    val profitPercent: Double,
    val quantity: Int,
    val rawGoldValue: Long,
    val wageAmount: Long,
    val profitAmount: Long,
    val taxAmount: Long,
    val lineTotal: Long,
)

@Entity(tableName = "settings")
data class ShopSettings(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "گالری طلا و جواهر زرفام",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val goldPricePerGram18: Long = 0,
    val taxPercent: Double = 9.0,
)

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItem>,
)
