package ir.zarin.faktor.domain

import ir.zarin.faktor.data.model.PricingMode
import kotlin.math.roundToLong

/**
 * پارامترهای عمومی قیمت‌گذاری که برای همه اقلام یک فاکتور یکسان است.
 *
 * @param goldRatePerGram18Rial نرخ هر گرم طلای ۱۸ عیار (ریال)
 * @param profitPercent درصد سود فروشنده
 * @param vatPercent درصد مالیات بر ارزش افزوده
 */
data class PricingContext(
    val goldRatePerGram18Rial: Long,
    val profitPercent: Double,
    val vatPercent: Double,
)

/** ورودی محاسبه قیمت یک قلم کالا. */
data class PriceLineInput(
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val quantity: Int = 1,
    val weightGrams: Double = 0.0,
    val karat: Int = 18,
    val wagePercent: Double = 0.0,
    val stonePriceRial: Long = 0,
    val unitFixedPriceRial: Long = 0,
    val applyVat: Boolean = true,
    val discountRial: Long = 0,
)

/** ریز محاسبات یک قلم کالا. */
data class PriceBreakdown(
    val goldValueRial: Long = 0,
    val wageRial: Long = 0,
    val profitRial: Long = 0,
    val stoneRial: Long = 0,
    val vatRial: Long = 0,
    val discountRial: Long = 0,
) {
    val beforeDiscountRial: Long get() = goldValueRial + wageRial + profitRial + stoneRial + vatRial
    val totalRial: Long get() = (beforeDiscountRial - discountRial).coerceAtLeast(0)
}

/** جمع کل یک فاکتور. */
data class InvoiceTotals(
    val goldTotalRial: Long = 0,
    val wageTotalRial: Long = 0,
    val profitTotalRial: Long = 0,
    val stoneTotalRial: Long = 0,
    val vatTotalRial: Long = 0,
    val itemsDiscountRial: Long = 0,
    val invoiceDiscountRial: Long = 0,
) {
    val subtotalRial: Long
        get() = goldTotalRial + wageTotalRial + profitTotalRial + stoneTotalRial + vatTotalRial

    val grandTotalRial: Long
        get() = (subtotalRial - itemsDiscountRial - invoiceDiscountRial).coerceAtLeast(0)
}

/**
 * موتور محاسبه قیمت طلا و جواهر مطابق رویه رایج بازار ایران:
 *
 * ```
 * ارزش طلا = وزن × نرخ هر گرم (تعدیل‌شده با عیار) × تعداد
 * اجرت      = ارزش طلا × درصد اجرت
 * سود       = (ارزش طلا + اجرت) × درصد سود
 * مالیات    = (اجرت + سود) × درصد مالیات بر ارزش افزوده
 * مبلغ قلم  = ارزش طلا + اجرت + سود + نگین + مالیات − تخفیف
 * ```
 *
 * برای اقلام «قیمت مقطوع» (مانند سکه یا جواهر با قیمت ثابت)، مبلغ پایه همان قیمت
 * اعلام‌شده است و در صورت مشمول بودن، مالیات روی کل آن مبلغ محاسبه می‌شود.
 */
object GoldPricing {

    const val BASE_KARAT = 18

    /** نرخ هر گرم برای عیار دلخواه بر مبنای نرخ طلای ۱۸ عیار. */
    fun effectiveRatePerGram(goldRatePerGram18Rial: Long, karat: Int): Double {
        val safeKarat = karat.coerceIn(1, 24)
        return goldRatePerGram18Rial.toDouble() * safeKarat / BASE_KARAT
    }

    fun calculate(input: PriceLineInput, context: PricingContext): PriceBreakdown {
        val quantity = input.quantity.coerceAtLeast(1)
        val stone = (input.stonePriceRial.coerceAtLeast(0)) * quantity

        return when (input.pricingMode) {
            PricingMode.FIXED_PRICE -> {
                val base = input.unitFixedPriceRial.coerceAtLeast(0) * quantity
                val vat = if (input.applyVat) percentOf(base, context.vatPercent) else 0L
                PriceBreakdown(
                    goldValueRial = base,
                    wageRial = 0,
                    profitRial = 0,
                    stoneRial = stone,
                    vatRial = vat,
                    discountRial = input.discountRial.coerceAtLeast(0),
                )
            }

            PricingMode.BY_WEIGHT -> {
                val rate = effectiveRatePerGram(context.goldRatePerGram18Rial, input.karat)
                val goldValue = (input.weightGrams.coerceAtLeast(0.0) * rate * quantity).roundToLong()
                val wage = percentOf(goldValue, input.wagePercent)
                val profit = percentOf(goldValue + wage, context.profitPercent)
                val vat = if (input.applyVat) percentOf(wage + profit, context.vatPercent) else 0L
                PriceBreakdown(
                    goldValueRial = goldValue,
                    wageRial = wage,
                    profitRial = profit,
                    stoneRial = stone,
                    vatRial = vat,
                    discountRial = input.discountRial.coerceAtLeast(0),
                )
            }
        }
    }

    fun totals(breakdowns: List<PriceBreakdown>, invoiceDiscountRial: Long = 0): InvoiceTotals =
        InvoiceTotals(
            goldTotalRial = breakdowns.sumOf { it.goldValueRial },
            wageTotalRial = breakdowns.sumOf { it.wageRial },
            profitTotalRial = breakdowns.sumOf { it.profitRial },
            stoneTotalRial = breakdowns.sumOf { it.stoneRial },
            vatTotalRial = breakdowns.sumOf { it.vatRial },
            itemsDiscountRial = breakdowns.sumOf { it.discountRial },
            invoiceDiscountRial = invoiceDiscountRial.coerceAtLeast(0),
        )

    private fun percentOf(amount: Long, percent: Double): Long =
        if (percent <= 0.0) 0L else (amount * percent / 100.0).roundToLong()
}
