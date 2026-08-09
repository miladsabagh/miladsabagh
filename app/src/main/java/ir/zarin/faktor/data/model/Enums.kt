package ir.zarin.faktor.data.model

enum class ProductCategory {
    GOLD,
    JEWELRY,
    COIN,
    SILVER,
    OTHER,
    ;

    companion object {
        fun fromNameOrDefault(name: String?): ProductCategory =
            entries.firstOrNull { it.name == name } ?: GOLD
    }
}

/** روش قیمت‌گذاری قلم کالا: محاسبه بر اساس وزن و نرخ روز، یا قیمت مقطوع. */
enum class PricingMode {
    BY_WEIGHT,
    FIXED_PRICE,
    ;

    companion object {
        fun fromNameOrDefault(name: String?): PricingMode =
            entries.firstOrNull { it.name == name } ?: BY_WEIGHT
    }
}

enum class PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    CREDIT,
    ;

    companion object {
        fun fromNameOrDefault(name: String?): PaymentMethod =
            entries.firstOrNull { it.name == name } ?: CASH
    }
}

enum class PaymentStatus { PAID, PARTIAL, UNPAID }
