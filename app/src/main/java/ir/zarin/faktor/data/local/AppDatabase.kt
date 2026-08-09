package ir.zarin.faktor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.Product

@Database(
    entities = [Product::class, Customer::class, Invoice::class, InvoiceItem::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun customerDao(): CustomerDao

    abstract fun invoiceDao(): InvoiceDao

    companion object {
        private const val DATABASE_NAME = "zarin-faktor.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
