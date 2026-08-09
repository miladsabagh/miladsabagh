package com.goldgallery.app.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val weightGrams: Double,
    val karat: Int,
    val wagePerGram: Long,
    val stock: Int,
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: Long,
    val customerName: String,
    val customerPhone: String,
    val createdAt: Long,
    val goldPrice18PerGram: Long,
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("invoiceId")],
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productName: String,
    val category: String,
    val weightGrams: Double,
    val karat: Int,
    val wagePerGram: Long,
    val quantity: Int,
    val goldRaw: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val total: Long,
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItemEntity>,
)
