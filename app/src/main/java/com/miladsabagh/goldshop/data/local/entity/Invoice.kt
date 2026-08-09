package com.miladsabagh.goldshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvoiceStatus(val displayName: String) {
    PAID("پرداخت‌شده"),
    PARTIAL("پرداخت جزئی"),
    UNPAID("پرداخت‌نشده")
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: Long = 0L,
    val customerId: Long? = null,
    val customerName: String = "مشتری نقدی",
    val customerPhone: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val goldPriceAtSale: Double = 0.0,
    val taxPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalWeightGrams: Double = 0.0,
    val subtotalAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val status: InvoiceStatus = InvoiceStatus.PAID
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
