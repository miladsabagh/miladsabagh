package ir.zarrin.goldshop.domain.model

/** Category of a line on an invoice; it drives the default tax treatment. */
enum class ItemKind(val label: String, val defaultTaxBasis: TaxBasis) {
    MANUFACTURED("طلای ساخته‌شده", TaxBasis.WAGE_AND_PROFIT),
    MELTED("طلای آبشده", TaxBasis.EXEMPT),
    COIN("سکه", TaxBasis.EXEMPT),
    GEM("جواهر و نگین", TaxBasis.FULL_PRICE),
    SILVER("نقره", TaxBasis.FULL_PRICE),
    SERVICE("خدمات و تعمیرات", TaxBasis.FULL_PRICE);

    /** Coins and gems are priced per piece rather than by weight. */
    val isPricedPerPiece: Boolean get() = this == COIN || this == GEM || this == SERVICE

    companion object {
        fun fromName(name: String?): ItemKind =
            entries.firstOrNull { it.name == name } ?: MANUFACTURED
    }
}

/**
 * Which part of the line price value added tax applies to.
 *
 * Since 1402 Iranian gold invoices only charge VAT on the making charge and the seller profit,
 * while the raw gold value, coins and bullion stay exempt.
 */
enum class TaxBasis(val label: String) {
    WAGE_AND_PROFIT("اجرت و سود"),
    FULL_PRICE("کل مبلغ"),
    EXEMPT("معاف از مالیات");

    companion object {
        fun fromName(name: String?): TaxBasis =
            entries.firstOrNull { it.name == name } ?: WAGE_AND_PROFIT
    }
}

/** How the making charge (اجرت) of a line is expressed. */
enum class WageMode(val label: String, val unit: String) {
    PERCENT("درصدی", "٪"),
    PER_GRAM("به ازای هر گرم", "در گرم"),
    FIXED("مبلغ مقطوع", "");

    companion object {
        fun fromName(name: String?): WageMode =
            entries.firstOrNull { it.name == name } ?: PERCENT
    }
}

enum class InvoiceType(val label: String) {
    SALE("فروش"),
    PURCHASE("خرید");

    companion object {
        fun fromName(name: String?): InvoiceType =
            entries.firstOrNull { it.name == name } ?: SALE
    }
}

enum class PaymentMethod(val label: String) {
    CASH("نقدی"),
    CARD("کارت‌خوان"),
    TRANSFER("کارت به کارت / انتقال"),
    CHEQUE("چک"),
    CREDIT("نسیه");

    companion object {
        fun fromName(name: String?): PaymentMethod =
            entries.firstOrNull { it.name == name } ?: CASH
    }
}

enum class Currency(val label: String, val shortLabel: String) {
    TOMAN("تومان", "تومان"),
    RIAL("ریال", "ریال");

    companion object {
        fun fromName(name: String?): Currency =
            entries.firstOrNull { it.name == name } ?: TOMAN
    }
}

/** Gold purity expressed in parts per thousand. */
object Karat {
    const val K18 = 750
    const val K21 = 875
    const val K22 = 916
    const val K24 = 999
    const val MELTED = 995

    val COMMON = listOf(
        K18 to "۱۸ عیار (۷۵۰)",
        K21 to "۲۱ عیار (۸۷۵)",
        K22 to "۲۲ عیار (۹۱۶)",
        MELTED to "آبشده (۹۹۵)",
        K24 to "۲۴ عیار (۹۹۹)"
    )

    fun label(permille: Int): String =
        COMMON.firstOrNull { it.first == permille }?.second
            ?: "عیار ${ir.zarrin.goldshop.core.PersianNumbers.toPersianDigits(permille.toString())}"
}
