package ir.zarrin.goldshop.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import ir.zarrin.goldshop.domain.LineInput
import ir.zarrin.goldshop.domain.model.InvoiceType
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.PaymentMethod
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode

@Entity(tableName = "products", indices = [Index("code"), Index("name")])
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String = "",
    val name: String = "",
    val kind: ItemKind = ItemKind.MANUFACTURED,
    val karat: Int = Karat.K18,
    val weightGrams: Double = 0.0,
    val wageMode: WageMode = WageMode.PERCENT,
    val wageValue: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Long = 0L,
    val unitPriceOverride: Long? = null,
    val stock: Int = 1,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers", indices = [Index("name"), Index("phone")])
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val address: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["number"], unique = true), Index("dateMillis"), Index("customerId")]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val number: String = "",
    val type: InvoiceType = InvoiceType.SALE,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerNationalId: String = "",
    val customerAddress: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val baseGoldRate: Long = 0L,
    val taxPercent: Double = 10.0,
    val discount: Long = 0L,
    val paid: Long = 0L,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    /** Denormalised payable amount so lists and reports do not have to reprice every line. */
    @ColumnInfo(name = "payableCache") val payable: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceId: Long = 0L,
    val productId: Long? = null,
    val position: Int = 0,
    val title: String = "",
    val kind: ItemKind = ItemKind.MANUFACTURED,
    val karat: Int = Karat.K18,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val goldRatePerGram: Long = 0L,
    val wageMode: WageMode = WageMode.PERCENT,
    val wageValue: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Long = 0L,
    val taxBasis: TaxBasis = TaxBasis.WAGE_AND_PROFIT,
    val taxPercent: Double = 0.0,
    val unitPriceOverride: Long? = null
)

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItem>
) {
    val orderedItems: List<InvoiceItem> get() = items.sortedBy { it.position }
}

fun InvoiceItem.toLineInput(): LineInput = LineInput(
    kind = kind,
    karat = karat,
    weightGrams = weightGrams,
    quantity = quantity,
    goldRatePerGram = goldRatePerGram,
    wageMode = wageMode,
    wageValue = wageValue,
    profitPercent = profitPercent,
    stonePrice = stonePrice,
    taxBasis = taxBasis,
    taxPercent = taxPercent,
    unitPriceOverride = unitPriceOverride
)
