package com.miladsabagh.goldinvoice.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miladsabagh.goldinvoice.data.dao.CustomerDao
import com.miladsabagh.goldinvoice.data.dao.InvoiceDao
import com.miladsabagh.goldinvoice.data.dao.ProductDao
import com.miladsabagh.goldinvoice.data.entity.Customer
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.entity.InvoiceItem
import com.miladsabagh.goldinvoice.data.entity.Product

@Database(
    entities = [Customer::class, Product::class, Invoice::class, InvoiceItem::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gold_invoice.db"
                ).build().also { instance = it }
            }
    }
}
