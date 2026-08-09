package com.goldjewelry.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.goldjewelry.app.data.model.Customer
import com.goldjewelry.app.data.model.Invoice
import com.goldjewelry.app.data.model.InvoiceItem
import com.goldjewelry.app.data.model.Product
import com.goldjewelry.app.data.model.ProductCategory
import com.goldjewelry.app.data.model.ShopSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Customer::class,
        Invoice::class,
        InvoiceItem::class,
        ShopSettings::class
    ],
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
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "gold_jewelry_db"
            )
                .addCallback(SeedCallback())
                .build()
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedData(database)
                }
            }
        }

        private suspend fun seedData(database: AppDatabase) {
            database.settingsDao().insert(ShopSettings())

            val products = listOf(
                Product(
                    name = "انگشتر طلا ۱۸ عیار",
                    category = ProductCategory.GOLD,
                    weightGrams = 3.5,
                    karat = 18,
                    makingChargePercent = 8.0,
                    sku = "G-RING-001"
                ),
                Product(
                    name = "گردنبند طلا زنجیری",
                    category = ProductCategory.GOLD,
                    weightGrams = 12.0,
                    karat = 18,
                    makingChargePercent = 7.0,
                    sku = "G-NECK-001"
                ),
                Product(
                    name = "دستبند طلا",
                    category = ProductCategory.GOLD,
                    weightGrams = 8.2,
                    karat = 18,
                    makingChargePercent = 9.0,
                    sku = "G-BRAC-001"
                ),
                Product(
                    name = "گوشواره الماس",
                    category = ProductCategory.JEWELRY,
                    weightGrams = 2.1,
                    karat = 18,
                    makingChargePercent = 12.0,
                    fixedPrice = 45_000_000,
                    sku = "J-EAR-001"
                ),
                Product(
                    name = "سکه تمام بهار آزادی",
                    category = ProductCategory.COIN,
                    weightGrams = 8.13,
                    karat = 22,
                    makingChargePercent = 0.0,
                    sku = "C-FULL-001"
                ),
                Product(
                    name = "نیم سکه",
                    category = ProductCategory.COIN,
                    weightGrams = 4.07,
                    karat = 22,
                    makingChargePercent = 0.0,
                    sku = "C-HALF-001"
                )
            )
            products.forEach { database.productDao().insert(it) }

            database.customerDao().insert(
                Customer(
                    fullName = "علی محمدی",
                    phone = "09121234567",
                    nationalId = "0012345678",
                    address = "تهران، خیابان ولیعصر"
                )
            )
        }
    }
}
