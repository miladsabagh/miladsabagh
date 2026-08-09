package ir.zarrin.goldshop.domain

/** دسته‌بندی کالاهای طلا و جواهر */
enum class ProductCategory(val label: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    BANGLE("النگو"),
    EARRING("گوشواره"),
    SET("سرویس"),
    PENDANT("آویز"),
    CHAIN("زنجیر"),
    COIN("سکه"),
    GEM("جواهر و نگین"),
    OTHER("متفرقه");

    companion object {
        fun fromName(value: String?): ProductCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}

/** شیوه قیمت‌گذاری کالا */
enum class PricingMode(val label: String) {
    /** بر اساس وزن و نرخ روز طلا */
    BY_WEIGHT("بر اساس وزن"),

    /** قیمت مقطوع، مانند سکه یا کالای امانی */
    FIXED("قیمت مقطوع");

    companion object {
        fun fromName(value: String?): PricingMode =
            entries.firstOrNull { it.name == value } ?: BY_WEIGHT
    }
}

/** روش پرداخت فاکتور */
enum class PaymentMethod(val label: String) {
    CASH("نقدی"),
    CARD("کارت‌خوان"),
    TRANSFER("کارت به کارت"),
    CHEQUE("چک"),
    CREDIT("نسیه");

    companion object {
        fun fromName(value: String?): PaymentMethod =
            entries.firstOrNull { it.name == value } ?: CASH
    }
}

/** وضعیت فاکتور */
enum class InvoiceStatus(val label: String) {
    PAID("تسویه شده"),
    PARTIAL("پرداخت جزئی"),
    UNPAID("پرداخت نشده");

    companion object {
        fun fromName(value: String?): InvoiceStatus =
            entries.firstOrNull { it.name == value } ?: UNPAID

        fun of(total: Long, paid: Long): InvoiceStatus = when {
            paid >= total -> PAID
            paid <= 0L -> UNPAID
            else -> PARTIAL
        }
    }
}

/** واحد پول نمایش داده شده */
enum class Currency(val label: String, val multiplier: Long) {
    TOMAN("تومان", 1L),
    RIAL("ریال", 10L);

    companion object {
        fun fromName(value: String?): Currency =
            entries.firstOrNull { it.name == value } ?: TOMAN
    }
}

/** عیارهای رایج طلا در بازار ایران */
object Karats {
    val COMMON = listOf(18, 20, 21, 22, 24)
    const val BASE = 18
}
