package com.miladsabagh.goldshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceId: Long = 0L,
    val productId: Long? = null,
    val itemName: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val weightGrams: Double,
    val karat: Int,
    val quantity: Int = 1,
    val goldPricePerGram: Double,
    val laborFeePercent: Double,
    val laborFeeAmount: Double,
    val profitPercent: Double,
    val profitAmount: Double,
    val stonePrice: Double = 0.0,
    val taxAmount: Double,
    val lineTotal: Double
)
