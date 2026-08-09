package com.goldgallery.app.data.model

import com.goldgallery.app.data.db.ProductEntity

enum class Category(val persianName: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    EARRING("گوشواره"),
    SET("سرویس طلا"),
    COIN("سکه");

    companion object {
        fun of(name: String): Category = entries.firstOrNull { it.name == name } ?: RING
    }
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
)
