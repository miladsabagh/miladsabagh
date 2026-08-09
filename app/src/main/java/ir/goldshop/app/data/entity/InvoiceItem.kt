package ir.goldshop.app.data.entity

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
    val id: Long = 0L,
    val invoiceId: Long,
    val productId: Long? = null,
    val itemName: String,
    val karat: Int,
    val weightGrams: Double,
    val quantity: Int = 1,
    val pricePerGram: Double,
    val laborPercent: Double,
    val profitPercent: Double,
    val taxPercent: Double,
    val baseAmount: Double,
    val laborAmount: Double,
    val profitAmount: Double,
    val taxAmount: Double,
    val lineTotal: Double
)
