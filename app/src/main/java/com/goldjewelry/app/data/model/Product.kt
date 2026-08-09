package com.goldjewelry.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCategory(val label: String) {
    GOLD("طلا"),
    JEWELRY("جواهر"),
    COIN("سکه"),
    OTHER("سایر")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: ProductCategory,
    val weightGrams: Double,
    val karat: Int = 18,
    val makingChargePercent: Double = 7.0,
    val fixedPrice: Long? = null,
    val description: String = "",
    val sku: String = "",
    val isActive: Boolean = true
)
