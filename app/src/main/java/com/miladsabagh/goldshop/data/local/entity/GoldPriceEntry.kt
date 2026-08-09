package com.miladsabagh.goldshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Historical log of the daily 18k gold price per gram (toman). */
@Entity(tableName = "gold_price_history")
data class GoldPriceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pricePerGram18k: Double,
    val recordedAt: Long = System.currentTimeMillis()
)
