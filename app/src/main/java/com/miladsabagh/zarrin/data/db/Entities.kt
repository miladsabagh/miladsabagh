package com.miladsabagh.zarrin.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ProductCategory(val displayName: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    BANGLE("النگو"),
    EARRING("گوشواره"),
    SET("سرویس"),
    COIN("سکه"),
    OTHER("متفرقه")
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ProductCategory,
    val karat: Int = 18,
    val weightGrams: Double,
    val wagePerGram: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: Long,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String,
    val createdAt: Long = System.currentTimeMillis(),
    val goldPricePerGram18k: Long,
    val profitPercent: Double,
    val taxPercent: Double,
    val goldValue: Long,
    val wageTotal: Long,
    val profitAmount: Long,
    val taxAmount: Long,
    val grandTotal: Long
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
    val title: String,
    val karat: Int,
    val weightGrams: Double,
    val wagePerGram: Long,
    val goldPricePerGram: Long,
    val goldValue: Long,
    val wageAmount: Long,
    val profitAmount: Long,
    val taxAmount: Long,
    val lineTotal: Long
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItemEntity>
)
