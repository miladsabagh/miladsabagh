package ir.zarrin.goldshop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Product::class, Customer::class, Invoice::class, InvoiceItem::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ZarrinDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun customerDao(): CustomerDao

    abstract fun invoiceDao(): InvoiceDao

    companion object {
        fun build(context: Context): ZarrinDatabase =
            Room.databaseBuilder(context.applicationContext, ZarrinDatabase::class.java, "zarrin.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
