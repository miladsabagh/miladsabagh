package com.zarrin.goldshop.domain

/**
 * محاسبه قیمت طلا به سبک رایج طلافروشی‌های ایران:
 * ۱) تبدیل وزن به معادل عیار ۱۸
 * ۲) افزودن اجرت ساخت
 * ۳) افزودن سود فروشنده
 * ۴) افزودن مالیات بر ارزش افزوده
 */
object GoldPricing {

    fun purityFactor(karat: Int): Double = karat / 18.0

    fun equivalentWeight18(weightGrams: Double, karat: Int): Double =
        weightGrams * purityFactor(karat)

    fun baseGoldValue(
        weightGrams: Double,
        karat: Int,
        goldPrice18PerGram: Long
    ): Long {
        val value = equivalentWeight18(weightGrams, karat) * goldPrice18PerGram
        return value.toLong()
    }

    fun makingFeeAmount(baseGoldValue: Long, makingFeePercent: Double): Long =
        ((baseGoldValue * makingFeePercent) / 100.0).toLong()

    fun lineTotal(
        weightGrams: Double,
        karat: Int,
        makingFeePercent: Double,
        goldPrice18PerGram: Long
    ): Long {
        val base = baseGoldValue(weightGrams, karat, goldPrice18PerGram)
        return base + makingFeeAmount(base, makingFeePercent)
    }

    data class InvoiceTotals(
        val subtotal: Long,
        val profitAmount: Long,
        val vatAmount: Long,
        val totalAmount: Long
    )

    fun invoiceTotals(
        itemLineTotals: List<Long>,
        profitPercent: Double,
        vatPercent: Double
    ): InvoiceTotals {
        val subtotal = itemLineTotals.sum()
        val profit = ((subtotal * profitPercent) / 100.0).toLong()
        val taxable = subtotal + profit
        val vat = ((taxable * vatPercent) / 100.0).toLong()
        return InvoiceTotals(
            subtotal = subtotal,
            profitAmount = profit,
            vatAmount = vat,
            totalAmount = taxable + vat
        )
    }
}
