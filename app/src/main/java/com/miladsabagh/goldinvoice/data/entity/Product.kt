package com.miladsabagh.goldinvoice.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A jewelry / gold item kept in shop inventory.
 *
 * Pricing percentages default from shop settings but can be overridden per product,
 * since labor fee and profit margin often vary by item type (ring, necklace, coin, ...).
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "",
    val code: String = "",
    val weightGrams: Double,
    val karat: Int = 18,
    val laborFeePercent: Double,
    val profitPercent: Double,
    val taxPercent: Double,
    val quantity: Int = 1,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

val JEWELRY_CATEGORIES = listOf(
    "انگشتر",
    "گردنبند",
    "دستبند",
    "النگو",
    "گوشواره",
    "سکه",
    "آویز",
    "پابند",
    "سایر"
)

val AVAILABLE_KARATS = listOf(24, 22, 21, 18, 14, 10)
