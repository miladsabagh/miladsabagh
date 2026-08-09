package com.miladsabagh.zarrin.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun categoryToString(value: ProductCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): ProductCategory =
        runCatching { ProductCategory.valueOf(value) }.getOrDefault(ProductCategory.OTHER)
}

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "zarrin.db"
            ).addCallback(SeedCallback()).build().also { instance = it }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val scope = CoroutineScope(Dispatchers.IO)
            instance?.let { database ->
                scope.launch {
                    seed(database)
                }
            }
        }

        private suspend fun seed(database: AppDatabase) {
            val products = listOf(
                ProductEntity(name = "انگشتر طلای زنانه مدل رز", category = ProductCategory.RING, karat = 18, weightGrams = 2.35, wagePerGram = 350_000),
                ProductEntity(name = "انگشتر مردانه طرح ساده", category = ProductCategory.RING, karat = 18, weightGrams = 4.10, wagePerGram = 300_000),
                ProductEntity(name = "گردنبند زنجیری کارتیه", category = ProductCategory.NECKLACE, karat = 18, weightGrams = 6.80, wagePerGram = 420_000),
                ProductEntity(name = "گوشواره میخی مروارید", category = ProductCategory.EARRING, karat = 18, weightGrams = 1.95, wagePerGram = 480_000),
                ProductEntity(name = "دستبند پانچ طرح ورساچه", category = ProductCategory.BRACELET, karat = 18, weightGrams = 5.20, wagePerGram = 390_000),
                ProductEntity(name = "النگوی تک‌پوش صوفیا", category = ProductCategory.BANGLE, karat = 18, weightGrams = 8.60, wagePerGram = 330_000),
                ProductEntity(name = "سرویس کامل عروس لیلا", category = ProductCategory.SET, karat = 18, weightGrams = 42.00, wagePerGram = 460_000),
                ProductEntity(name = "سکه تمام بهار آزادی", category = ProductCategory.COIN, karat = 22, weightGrams = 8.133, wagePerGram = 0)
            )
            products.forEach { database.productDao().upsert(it) }

            val customers = listOf(
                CustomerEntity(name = "مریم احمدی", phone = "09121234567"),
                CustomerEntity(name = "رضا کریمی", phone = "09359876543"),
                CustomerEntity(name = "مشتری حضوری", phone = "-")
            )
            customers.forEach { database.customerDao().upsert(it) }
        }
    }
}
