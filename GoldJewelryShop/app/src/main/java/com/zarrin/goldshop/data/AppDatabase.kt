package com.zarrin.goldshop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ProductCategory = ProductCategory.valueOf(value)
}

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        ShopSettingsEntity::class,
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
    abstract fun settingsDao(): SettingsDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zarrin_gold_shop.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
