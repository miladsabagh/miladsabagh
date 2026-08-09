package com.goldjewelry.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long?,
    val productName: String,
    val category: ProductCategory,
    val weightGrams: Double,
    val karat: Int,
    val unitPrice: Long,
    val quantity: Int = 1,
    val makingCharge: Long = 0,
    val lineTotal: Long
)
