package com.goldshop.app.util

import com.goldshop.app.data.model.CartItem
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ProductCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun basePrice_for18k_equals_weight_times_rate() {
        val price = PriceCalculator.basePrice(
            weightGrams = 10.0,
            purity = 750,
            goldPricePerGram18 = 4_000_000L
        )
        assertThat(price).isEqualTo(40_000_000L)
    }

    @Test
    fun unitPrice_applies_labor_and_profit() {
        // base = 10 * 4_000_000 = 40_000_000
        // labor 10% => 44_000_000
        // profit 5% => 46_200_000
        val price = PriceCalculator.unitPrice(
            weightGrams = 10.0,
            purity = 750,
            laborPercent = 10.0,
            profitPercent = 5.0,
            goldPricePerGram18 = 4_000_000L
        )
        assertThat(price).isEqualTo(46_200_000L)
    }

    @Test
    fun cartSubtotal_sums_quantities() {
        val product = Product(
            id = 1,
            name = "انگشتر",
            category = ProductCategory.RING,
            weightGrams = 5.0,
            purity = 750,
            laborPercent = 0.0,
            profitPercent = 0.0
        )
        val cart = listOf(CartItem(product, quantity = 2))
        val total = PriceCalculator.cartSubtotal(cart, goldPricePerGram18 = 2_000_000L)
        assertThat(total).isEqualTo(20_000_000L)
    }

    @Test
    fun grandTotal_never_negative() {
        val total = PriceCalculator.grandTotal(
            subtotal = 1_000L,
            discount = 5_000L,
            tax = 0L
        )
        assertThat(total).isEqualTo(0L)
    }
}
