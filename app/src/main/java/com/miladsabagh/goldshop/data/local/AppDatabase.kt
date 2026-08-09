package com.miladsabagh.goldshop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miladsabagh.goldshop.data.local.dao.CustomerDao
import com.miladsabagh.goldshop.data.local.dao.GoldPriceDao
import com.miladsabagh.goldshop.data.local.dao.InvoiceDao
import com.miladsabagh.goldshop.data.local.dao.ProductDao
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.data.local.entity.GoldPriceEntry
import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.data.local.entity.Product

@Database(
    entities = [Product::class, Customer::class, Invoice::class, InvoiceItem::class, GoldPriceEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun goldPriceDao(): GoldPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goldshop.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
