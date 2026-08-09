package com.miladsabagh.goldshop.data.local

import androidx.room.TypeConverter
import com.miladsabagh.goldshop.data.local.entity.InvoiceStatus
import com.miladsabagh.goldshop.data.local.entity.ProductCategory

class Converters {

    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory =
        runCatching { ProductCategory.valueOf(value) }.getOrDefault(ProductCategory.OTHER)

    @TypeConverter
    fun fromInvoiceStatus(value: InvoiceStatus): String = value.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus =
        runCatching { InvoiceStatus.valueOf(value) }.getOrDefault(InvoiceStatus.PAID)
}
