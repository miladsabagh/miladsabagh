package ir.goldshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یک قلم کالای طلا/جواهر در انبار فروشگاه که به عنوان الگو برای ساخت سریع‌تر فاکتور استفاده می‌شود.
 * مقادیر ذخیره‌شده در اینجا صرفاً پیش‌فرض هستند و هنگام افزودن به فاکتور قابل ویرایش‌اند.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String = ProductCategory.RING.persianLabel,
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val laborPercent: Double = 7.0,
    val profitPercent: Double = 7.0,
    val quantityInStock: Int = 1,
    val sku: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class ProductCategory(val persianLabel: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    EARRING("گوشواره"),
    COIN("سکه"),
    BAR("شمش"),
    SET("سرویس"),
    OTHER("سایر");

    companion object {
        fun fromLabel(label: String): ProductCategory =
            entries.firstOrNull { it.persianLabel == label } ?: OTHER
    }
}
