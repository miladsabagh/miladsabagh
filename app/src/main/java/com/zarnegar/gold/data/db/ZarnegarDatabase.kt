package com.zarnegar.gold.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ZarnegarDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var instance: ZarnegarDatabase? = null

        fun get(context: Context): ZarnegarDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ZarnegarDatabase::class.java,
                "zarnegar.db",
            ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}
