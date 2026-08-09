package com.goldjewelry.app.data.database

import androidx.room.TypeConverter
import com.goldjewelry.app.data.model.InvoiceStatus
import com.goldjewelry.app.data.model.ProductCategory

class Converters {
    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory =
        ProductCategory.valueOf(value)

    @TypeConverter
    fun fromInvoiceStatus(value: InvoiceStatus): String = value.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus =
        InvoiceStatus.valueOf(value)
}
