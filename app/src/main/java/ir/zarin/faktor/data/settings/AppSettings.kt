package ir.zarin.faktor.data.settings

import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.domain.PricingContext

/** تنظیمات فروشگاه و قیمت‌گذاری. */
data class AppSettings(
    val shopName: String = "",
    val shopPhone: String = "",
    val shopAddress: String = "",
    /** نرخ هر گرم طلای ۱۸ عیار به ریال. */
    val goldRatePerGramRial: Long = 0,
    val profitPercent: Double = DEFAULT_PROFIT_PERCENT,
    val vatPercent: Double = DEFAULT_VAT_PERCENT,
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val invoicePrefix: String = "",
    val persianDigits: Boolean = true,
) {
    val pricingContext: PricingContext
        get() = PricingContext(
            goldRatePerGram18Rial = goldRatePerGramRial,
            profitPercent = profitPercent,
            vatPercent = vatPercent,
        )

    companion object {
        const val DEFAULT_PROFIT_PERCENT = 7.0
        const val DEFAULT_VAT_PERCENT = 10.0
    }
}
