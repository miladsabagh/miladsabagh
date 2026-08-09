package ir.goldshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod(val persianLabel: String) {
    CASH("نقدی"),
    CARD("کارت‌خوان"),
    TRANSFER("کارت به کارت"),
    CHEQUE("چک"),
    INSTALLMENT("اقساطی");

    companion object {
        fun fromLabel(label: String): PaymentMethod =
            entries.firstOrNull { it.persianLabel == label } ?: CASH
    }
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val customerAddress: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val goldPricePerGramSnapshot: Double,
    val totalWeightGrams: Double,
    val subtotalAmount: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val paymentMethod: String = PaymentMethod.CASH.persianLabel,
    val notes: String = ""
) {
    val remainingAmount: Double get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
    val isFullyPaid: Boolean get() = paidAmount >= totalAmount - 0.5
}
