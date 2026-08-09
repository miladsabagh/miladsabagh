package com.goldjewelry.app.util

import com.goldjewelry.app.data.model.Product
import com.goldjewelry.app.data.model.ProductCategory
import kotlin.math.roundToLong

object GoldPriceCalculator {

    private val karatFactors = mapOf(
        24 to 1.0,
        22 to 22.0 / 24.0,
        21 to 21.0 / 24.0,
        18 to 18.0 / 24.0,
        14 to 14.0 / 24.0
    )

    fun karatFactor(karat: Int): Double = karatFactors[karat] ?: (karat / 24.0)

    fun calculateGoldValue(weightGrams: Double, karat: Int, goldPricePerGram: Long): Long {
        val pureWeight = weightGrams * karatFactor(karat)
        return (pureWeight * goldPricePerGram).roundToLong()
    }

    fun calculateMakingCharge(goldValue: Long, makingChargePercent: Double): Long {
        return (goldValue * makingChargePercent / 100.0).roundToLong()
    }

    fun calculateProductPrice(product: Product, goldPricePerGram: Long): Long {
        product.fixedPrice?.let { return it }

        if (product.category == ProductCategory.JEWELRY && product.weightGrams <= 0) {
            return product.fixedPrice ?: 0L
        }

        val goldValue = calculateGoldValue(product.weightGrams, product.karat, goldPricePerGram)
        val makingCharge = calculateMakingCharge(goldValue, product.makingChargePercent)
        return goldValue + makingCharge
    }

    fun calculateCartTotal(
        items: List<Pair<Product, Int>>,
        goldPricePerGram: Long
    ): Long {
        return items.sumOf { (product, qty) ->
            calculateProductPrice(product, goldPricePerGram) * qty
        }
    }
}
