package ir.zarin.faktor.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import ir.zarin.faktor.core.CurrencyUnit

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["number"], unique = true), Index(value = ["dateMillis"])],
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    /** نرخ روز طلای ۱۸ عیار در لحظه صدور فاکتور. */
    val goldRatePerGramRial: Long = 0,
    val profitPercent: Double = 0.0,
    val vatPercent: Double = 0.0,
    val goldTotalRial: Long = 0,
    val wageTotalRial: Long = 0,
    val profitTotalRial: Long = 0,
    val stoneTotalRial: Long = 0,
    val vatTotalRial: Long = 0,
    val itemsDiscountRial: Long = 0,
    val invoiceDiscountRial: Long = 0,
    val grandTotalRial: Long = 0,
    val paidAmountRial: Long = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    val remainingRial: Long get() = (grandTotalRial - paidAmountRial).coerceAtLeast(0)

    val status: PaymentStatus
        get() = when {
            paidAmountRial >= grandTotalRial -> PaymentStatus.PAID
            paidAmountRial <= 0 -> PaymentStatus.UNPAID
            else -> PaymentStatus.PARTIAL
        }
}

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["invoiceId"])],
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val name: String = "",
    val category: ProductCategory = ProductCategory.GOLD,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val wagePercent: Double = 0.0,
    val stonePriceRial: Long = 0,
    val unitFixedPriceRial: Long = 0,
    val applyVat: Boolean = true,
    val goldValueRial: Long = 0,
    val wageRial: Long = 0,
    val profitRial: Long = 0,
    val stoneRial: Long = 0,
    val vatRial: Long = 0,
    val discountRial: Long = 0,
    val lineTotalRial: Long = 0,
)

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItem>,
)
