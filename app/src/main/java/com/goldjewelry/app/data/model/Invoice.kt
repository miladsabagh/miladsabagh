package com.goldjewelry.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvoiceStatus(val label: String) {
    DRAFT("پیش‌نویس"),
    ISSUED("صادر شده"),
    PAID("پرداخت شده"),
    CANCELLED("لغو شده")
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String,
    val subtotal: Long,
    val discount: Long = 0,
    val tax: Long = 0,
    val total: Long,
    val goldPricePerGram: Long,
    val status: InvoiceStatus = InvoiceStatus.ISSUED,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
