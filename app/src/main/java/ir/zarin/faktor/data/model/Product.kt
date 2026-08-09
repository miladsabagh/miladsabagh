package ir.zarin.faktor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val category: ProductCategory = ProductCategory.GOLD,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    /** عیار طلا؛ نرخ روز بر مبنای عیار ۱۸ ذخیره می‌شود. */
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val wagePercent: Double = 0.0,
    val stonePriceRial: Long = 0,
    val fixedPriceRial: Long = 0,
    val stockQty: Int = 1,
    val applyVat: Boolean = true,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
