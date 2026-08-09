package ir.zarrin.goldshop.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.ProductCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ZarrinDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {

        fun build(context: Context, scope: CoroutineScope): ZarrinDatabase {
            lateinit var database: ZarrinDatabase
            database = Room.databaseBuilder(
                context.applicationContext,
                ZarrinDatabase::class.java,
                "zarrin.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch { seed(database) }
                    }
                })
                .build()
            return database
        }

        /** داده نمونه برای اولین اجرا تا فروشگاه از ابتدا خالی نباشد */
        private suspend fun seed(database: ZarrinDatabase) {
            val products = listOf(
                sample("A-1001", "انگشتر طرح ونیزی", ProductCategory.RING, 18, 3.450, 9.0, 7.0, 0, 2),
                sample("A-1002", "نیم‌ست قلب کارتیه", ProductCategory.SET, 18, 6.120, 12.0, 7.0, 350_000, 1),
                sample("A-1003", "دستبند النگویی رولکس", ProductCategory.BRACELET, 18, 8.740, 10.0, 7.0, 0, 1),
                sample("A-1004", "گوشواره حلقه‌ای", ProductCategory.EARRING, 18, 2.310, 14.0, 7.0, 0, 4),
                sample("A-1005", "زنجیر فیگارو ۵۰ سانتی", ProductCategory.CHAIN, 18, 5.980, 8.0, 7.0, 0, 3),
                sample("A-1006", "آویز اسم دلخواه", ProductCategory.PENDANT, 18, 1.850, 18.0, 7.0, 0, 5),
                sample("A-1007", "النگو شش‌گرمی (جفت)", ProductCategory.BANGLE, 18, 12.400, 11.0, 7.0, 0, 1),
                sample("A-1008", "انگشتر جواهر با نگین برلیان", ProductCategory.GEM, 18, 4.200, 16.0, 10.0, 12_500_000, 1),
                ProductEntity(
                    code = "S-2001",
                    name = "سکه تمام بهار آزادی (طرح جدید)",
                    category = ProductCategory.COIN.name,
                    karat = 22,
                    weightGrams = 8.133,
                    pricingMode = PricingMode.FIXED.name,
                    fixedPrice = 48_500_000,
                    taxable = false,
                    stockQty = 3,
                    note = "قیمت مقطوع بر اساس نرخ روز بازار"
                ),
                ProductEntity(
                    code = "S-2002",
                    name = "نیم سکه بهار آزادی",
                    category = ProductCategory.COIN.name,
                    karat = 22,
                    weightGrams = 4.068,
                    pricingMode = PricingMode.FIXED.name,
                    fixedPrice = 26_800_000,
                    taxable = false,
                    stockQty = 4
                )
            )
            database.productDao().let { dao -> products.forEach { dao.insert(it) } }

            val customers = listOf(
                CustomerEntity(name = "میلاد صباغ", phone = "09121234567", nationalId = "0079123456", address = "تهران، خیابان ولیعصر"),
                CustomerEntity(name = "زهرا کریمی", phone = "09351112233", address = "اصفهان، چهارباغ بالا"),
                CustomerEntity(name = "حسین مرادی", phone = "09903334455", address = "مشهد، بلوار سجاد")
            )
            database.customerDao().let { dao -> customers.forEach { dao.insert(it) } }
        }

        private fun sample(
            code: String,
            name: String,
            category: ProductCategory,
            karat: Int,
            weight: Double,
            wage: Double,
            profit: Double,
            stone: Long,
            stock: Int
        ) = ProductEntity(
            code = code,
            name = name,
            category = category.name,
            karat = karat,
            weightGrams = weight,
            wagePercent = wage,
            profitPercent = profit,
            stonePrice = stone,
            pricingMode = PricingMode.BY_WEIGHT.name,
            stockQty = stock
        )
    }
}
