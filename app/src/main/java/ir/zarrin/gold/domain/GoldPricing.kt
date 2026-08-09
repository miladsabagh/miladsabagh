package ir.zarrin.gold.domain

import kotlin.math.roundToLong

/**
 * اجزای قیمت یک قلم کالای طلا (همه مبالغ به تومان).
 */
data class PriceBreakdown(
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
) {
    val total: Long get() = goldValue + wage + profit + tax

    operator fun plus(other: PriceBreakdown) = PriceBreakdown(
        goldValue = goldValue + other.goldValue,
        wage = wage + other.wage,
        profit = profit + other.profit,
        tax = tax + other.tax,
    )

    companion object {
        val ZERO = PriceBreakdown(0, 0, 0, 0)
    }
}

/**
 * موتور محاسبه قیمت طلا طبق فرمول رایج بازار طلای ایران:
 *
 * بهای طلا  = وزن × قیمت هر گرم (تبدیل‌شده به عیار کالا)
 * اجرت ساخت = بهای طلا × درصد اجرت
 * سود فروش  = (بهای طلا + اجرت) × درصد سود
 * مالیات    = (اجرت + سود) × نرخ مالیات بر ارزش افزوده
 *             (طبق قانون، به اصل طلا مالیات تعلق نمی‌گیرد)
 * قیمت نهایی = بهای طلا + اجرت + سود + مالیات
 */
object GoldPricing {

    /** قیمت هر گرم طلا برای عیار دلخواه، بر مبنای قیمت هر گرم طلای ۱۸ عیار. */
    fun pricePerGram(pricePerGram18k: Long, karat: Int): Double {
        require(karat in 1..24) { "عیار نامعتبر" }
        return pricePerGram18k.toDouble() * karat / 18.0
    }

    fun calculate(
        pricePerGram18k: Long,
        weightGrams: Double,
        karat: Int,
        wagePercent: Double,
        profitPercent: Double,
        taxPercent: Double,
        quantity: Int = 1,
    ): PriceBreakdown {
        require(weightGrams >= 0) { "وزن نامعتبر" }
        require(quantity >= 1) { "تعداد نامعتبر" }
        val goldValue = pricePerGram(pricePerGram18k, karat) * weightGrams
        val wage = goldValue * wagePercent / 100.0
        val profit = (goldValue + wage) * profitPercent / 100.0
        val tax = (wage + profit) * taxPercent / 100.0
        return PriceBreakdown(
            goldValue = goldValue.roundToLong() * quantity,
            wage = wage.roundToLong() * quantity,
            profit = profit.roundToLong() * quantity,
            tax = tax.roundToLong() * quantity,
        )
    }
}
