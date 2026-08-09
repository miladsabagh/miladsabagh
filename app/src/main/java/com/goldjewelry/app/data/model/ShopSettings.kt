package com.goldjewelry.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "فروشگاه طلا و جواهر",
    val shopAddress: String = "",
    val shopPhone: String = "",
    val shopLicense: String = "",
    val goldPricePerGram: Long = 3_500_000,
    val taxPercent: Double = 9.0,
    val invoicePrefix: String = "INV",
    val lastInvoiceNumber: Int = 1000
)
