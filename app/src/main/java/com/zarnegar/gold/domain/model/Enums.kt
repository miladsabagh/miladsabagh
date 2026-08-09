package com.zarnegar.gold.domain.model

/** دسته‌بندی کالا */
enum class ProductCategory(val label: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    BANGLE("النگو"),
    EARRING("گوشواره"),
    CHAIN("زنجیر"),
    PENDANT("آویز"),
    SET("سرویس"),
    COIN("سکه"),
    GEM("جواهر"),
    OTHER("متفرقه"),
    ;

    companion object {
        fun fromName(name: String?): ProductCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}

/** نحوهٔ محاسبهٔ قیمت */
enum class PricingMode(val label: String) {
    /** بر اساس وزن و نرخ روز طلا */
    BY_WEIGHT("محاسبه بر اساس وزن"),

    /** قیمت مقطوع (مثل سکه یا کالای آماده) */
    FIXED("قیمت مقطوع"),
    ;

    companion object {
        fun fromName(name: String?): PricingMode =
            entries.firstOrNull { it.name == name } ?: BY_WEIGHT
    }
}

/** نحوهٔ محاسبهٔ اجرت ساخت */
enum class WageMode(val label: String) {
    PERCENT("درصدی"),
    PER_GRAM("مبلغ ثابت هر گرم"),
    ;

    companion object {
        fun fromName(name: String?): WageMode =
            entries.firstOrNull { it.name == name } ?: PERCENT
    }
}

/** روش پرداخت */
enum class PaymentMethod(val label: String) {
    CASH("نقدی"),
    CARD("کارتخوان"),
    TRANSFER("کارت به کارت / انتقال"),
    CHEQUE("چک"),
    CREDIT("نسیه"),
    ;

    companion object {
        fun fromName(name: String?): PaymentMethod =
            entries.firstOrNull { it.name == name } ?: CASH
    }
}

/** وضعیت تسویهٔ فاکتور */
enum class InvoiceStatus(val label: String) {
    PAID("تسویه شده"),
    PARTIAL("پرداخت جزئی"),
    UNPAID("پرداخت نشده"),
    ;

    companion object {
        fun fromName(name: String?): InvoiceStatus =
            entries.firstOrNull { it.name == name } ?: UNPAID

        fun of(payable: Long, paid: Long): InvoiceStatus = when {
            paid >= payable && payable > 0 -> PAID
            paid <= 0L -> UNPAID
            else -> PARTIAL
        }
    }
}

/** عیارهای رایج طلا در بازار ایران */
object Karats {
    const val BASE = 750 // مبنای نرخ‌گذاری: طلای ۱۸ عیار

    val COMMON = listOf(
        705 to "۱۷ عیار (۷۰۵)",
        740 to "۱۷٫۷ عیار (۷۴۰)",
        750 to "۱۸ عیار (۷۵۰)",
        795 to "۱۹ عیار (۷۹۵)",
        875 to "۲۱ عیار (۸۷۵)",
        916 to "۲۲ عیار (۹۱۶)",
        995 to "آب‌شده (۹۹۵)",
        999 to "۲۴ عیار (۹۹۹)",
    )

    fun label(karat: Int): String =
        COMMON.firstOrNull { it.first == karat }?.second ?: "عیار $karat"
}
