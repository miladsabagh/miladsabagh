package com.miladsabagh.goldinvoice.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentStatus {
    PAID, PARTIAL, UNPAID
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerNameSnapshot: String = "",
    val customerPhoneSnapshot: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val goldPricePerGram18k: Double,
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val notes: String = ""
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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val itemName: String,
    val weightGrams: Double,
    val karat: Int,
    val goldPricePerGram: Double,
    val laborFeePercent: Double,
    val profitPercent: Double,
    val taxPercent: Double,
    val quantity: Int,
    val baseGoldValue: Double,
    val laborFeeAmount: Double,
    val profitAmount: Double,
    val taxAmount: Double,
    val unitPrice: Double,
    val lineTotal: Double
)
