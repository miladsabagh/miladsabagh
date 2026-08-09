package com.goldshop.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.goldshop.app.data.model.Customer
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.InvoiceItem
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ProductCategory
import com.goldshop.app.data.model.ShopSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ProductCategory =
        runCatching { ProductCategory.valueOf(value) }.getOrDefault(ProductCategory.OTHER)
}

@Database(
    entities = [Product::class, Customer::class, Invoice::class, InvoiceItem::class, ShopSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gold_shop.db"
                )
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(context)
            database.settingsDao().upsert(ShopSettings())
            val samples = listOf(
                Product(
                    name = "انگشتر طرح گل",
                    category = ProductCategory.RING,
                    weightGrams = 4.2,
                    purity = 750,
                    laborPercent = 12.0,
                    profitPercent = 7.0,
                    stockQuantity = 3,
                    description = "انگشتر طلای ۱۸ عیار با طرح گل"
                ),
                Product(
                    name = "گردنبند مروارید",
                    category = ProductCategory.NECKLACE,
                    weightGrams = 8.5,
                    purity = 750,
                    laborPercent = 15.0,
                    profitPercent = 8.0,
                    stockQuantity = 2,
                    description = "گردنبند ظریف مناسب هدیه"
                ),
                Product(
                    name = "دستبند کارتیر",
                    category = ProductCategory.BRACELET,
                    weightGrams = 12.0,
                    purity = 750,
                    laborPercent = 10.0,
                    profitPercent = 7.0,
                    stockQuantity = 1,
                    description = "دستبند کارتیر کلاسیک"
                ),
                Product(
                    name = "سکه بهار آزادی",
                    category = ProductCategory.COIN,
                    weightGrams = 8.13,
                    purity = 900,
                    laborPercent = 0.0,
                    profitPercent = 2.0,
                    stockQuantity = 10,
                    description = "سکه تمام بهار آزادی"
                ),
                Product(
                    name = "گوشواره آویز الماس",
                    category = ProductCategory.EARRING,
                    weightGrams = 3.6,
                    purity = 750,
                    laborPercent = 18.0,
                    profitPercent = 9.0,
                    stockQuantity = 4,
                    description = "گوشواره نگین‌دار"
                )
            )
            samples.forEach { database.productDao().upsert(it) }
            database.customerDao().upsert(
                Customer(
                    fullName = "علی رضایی",
                    phone = "09121234567",
                    address = "تهران، خیابان ولیعصر"
                )
            )
        }
    }
}
