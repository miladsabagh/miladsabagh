package com.goldshop.app.util

import com.goldshop.app.data.model.CartItem
import com.goldshop.app.data.model.Product
import kotlin.math.roundToLong

/**
 * محاسبه قیمت طلا بر اساس فرمول رایج طلافروشی‌های ایران:
 * قیمت پایه = وزن × (عیار/۷۵۰) × قیمت هر گرم ۱۸ عیار
 * قیمت نهایی = قیمت پایه × (۱ + اجرت%) × (۱ + سود%)
 */
object PriceCalculator {

    fun purityFactor(purity: Int): Double = purity / 750.0

    fun basePrice(weightGrams: Double, purity: Int, goldPricePerGram18: Long): Long {
        return (weightGrams * purityFactor(purity) * goldPricePerGram18).roundToLong()
    }

    fun unitPrice(
        weightGrams: Double,
        purity: Int,
        laborPercent: Double,
        profitPercent: Double,
        goldPricePerGram18: Long
    ): Long {
        val base = basePrice(weightGrams, purity, goldPricePerGram18).toDouble()
        val withLabor = base * (1 + laborPercent / 100.0)
        val withProfit = withLabor * (1 + profitPercent / 100.0)
        return withProfit.roundToLong()
    }

    fun productUnitPrice(product: Product, goldPricePerGram18: Long): Long {
        return unitPrice(
            weightGrams = product.weightGrams,
            purity = product.purity,
            laborPercent = product.laborPercent,
            profitPercent = product.profitPercent,
            goldPricePerGram18 = goldPricePerGram18
        )
    }

    fun cartItemTotal(item: CartItem, goldPricePerGram18: Long): Long {
        val labor = item.customLaborPercent ?: item.product.laborPercent
        val profit = item.customProfitPercent ?: item.product.profitPercent
        val unit = unitPrice(
            weightGrams = item.product.weightGrams,
            purity = item.product.purity,
            laborPercent = labor,
            profitPercent = profit,
            goldPricePerGram18 = goldPricePerGram18
        )
        return unit * item.quantity
    }

    fun cartSubtotal(items: List<CartItem>, goldPricePerGram18: Long): Long =
        items.sumOf { cartItemTotal(it, goldPricePerGram18) }

    fun taxAmount(subtotal: Long, discount: Long, taxPercent: Double): Long {
        val taxable = (subtotal - discount).coerceAtLeast(0)
        return (taxable * taxPercent / 100.0).roundToLong()
    }

    fun grandTotal(subtotal: Long, discount: Long, tax: Long): Long =
        (subtotal - discount + tax).coerceAtLeast(0)
}
