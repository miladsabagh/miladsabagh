package com.miladsabagh.goldshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCategory(val displayName: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    EARRING("گوشواره"),
    COIN("سکه"),
    BULLION("شمش"),
    SET("سرویس"),
    OTHER("سایر")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val code: String = "",
    val weightGrams: Double,
    val karat: Int = 18,
    val laborFeePercent: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
