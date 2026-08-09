package ir.goldshop.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.goldshop.app.data.dao.CustomerDao
import ir.goldshop.app.data.dao.InvoiceDao
import ir.goldshop.app.data.dao.ProductDao
import ir.goldshop.app.data.dao.SettingsDao
import ir.goldshop.app.data.entity.Customer
import ir.goldshop.app.data.entity.Invoice
import ir.goldshop.app.data.entity.InvoiceItem
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.data.entity.ShopSettings

@Database(
    entities = [Customer::class, Product::class, Invoice::class, InvoiceItem::class, ShopSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goldshop.db"
                ).build().also { instance = it }
            }
    }
}
