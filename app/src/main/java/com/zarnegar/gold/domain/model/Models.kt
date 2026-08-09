package com.zarnegar.gold.domain.model

/**
 * کالای انبار.
 *
 * همهٔ مبالغ به واحد پول فروشگاه (پیش‌فرض: تومان) و به‌صورت عدد صحیح نگهداری می‌شوند.
 */
data class Product(
    val id: Long = 0,
    val code: String,
    val name: String,
    val category: ProductCategory = ProductCategory.RING,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val karat: Int = Karats.BASE,
    /** وزن کل کالا شامل سنگ */
    val weightGrams: Double = 0.0,
    /** وزن سنگ/نگین که از وزن طلا کسر می‌شود */
    val stoneWeightGrams: Double = 0.0,
    /** ارزش سنگ/نگین */
    val stoneValue: Long = 0,
    val wageMode: WageMode = WageMode.PERCENT,
    val wagePercent: Double = 0.0,
    val wagePerGram: Long = 0,
    val profitPercent: Double = 0.0,
    /** قیمت مقطوع، فقط وقتی [pricingMode] برابر [PricingMode.FIXED] است */
    val fixedPrice: Long = 0,
    val vatExempt: Boolean = false,
    val stock: Int = 0,
    val note: String = "",
) {
    val goldWeightGrams: Double get() = (weightGrams - stoneWeightGrams).coerceAtLeast(0.0)
}

/** مشتری */
data class Customer(
    val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val nationalCode: String = "",
    val address: String = "",
    val note: String = "",
)

/** اطلاعات فروشگاه و پارامترهای قیمت‌گذاری */
data class ShopSettings(
    val shopName: String = "گالری طلا و جواهر زرنگار",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val economicCode: String = "",
    val cardNumber: String = "",
    val currencyLabel: String = "تومان",
    /** نرخ هر گرم طلای ۱۸ عیار (مبنای محاسبات) */
    val goldRatePerGram18k: Long = 3_500_000,
    val vatPercent: Double = 10.0,
    /** آیا ارزش سنگ مشمول مالیات بر ارزش افزوده است */
    val vatOnStone: Boolean = true,
    /** گرد کردن مبلغ نهایی فاکتور؛ صفر یعنی بدون گرد کردن */
    val roundTo: Long = 1_000,
    val defaultWagePercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val invoiceFooterNote: String = "کالای فروخته‌شده با ارائهٔ فاکتور تا ۷ روز قابل تعویض است.",
) {
    fun pricing(): PricingSettings = PricingSettings(
        goldRatePerGram18k = goldRatePerGram18k,
        vatPercent = vatPercent,
        vatOnStone = vatOnStone,
        roundTo = roundTo,
    )
}

/** پارامترهای لازم برای موتور محاسبهٔ قیمت */
data class PricingSettings(
    val goldRatePerGram18k: Long,
    val vatPercent: Double = 10.0,
    val vatOnStone: Boolean = true,
    val roundTo: Long = 1_000,
)

/** یک قلم در سبد فروش؛ تصویری لحظه‌ای از کالا به‌همراه تعداد و تخفیف */
data class SaleItem(
    val productId: Long = 0,
    val title: String,
    val description: String = "",
    val category: ProductCategory = ProductCategory.OTHER,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val karat: Int = Karats.BASE,
    val weightGrams: Double = 0.0,
    val stoneWeightGrams: Double = 0.0,
    val stoneValue: Long = 0,
    val wageMode: WageMode = WageMode.PERCENT,
    val wagePercent: Double = 0.0,
    val wagePerGram: Long = 0,
    val profitPercent: Double = 0.0,
    val fixedPrice: Long = 0,
    val vatExempt: Boolean = false,
    val quantity: Int = 1,
    val discount: Long = 0,
) {
    companion object {
        fun fromProduct(product: Product, quantity: Int = 1): SaleItem = SaleItem(
            productId = product.id,
            title = product.name,
            description = product.code,
            category = product.category,
            pricingMode = product.pricingMode,
            karat = product.karat,
            weightGrams = product.weightGrams,
            stoneWeightGrams = product.stoneWeightGrams,
            stoneValue = product.stoneValue,
            wageMode = product.wageMode,
            wagePercent = product.wagePercent,
            wagePerGram = product.wagePerGram,
            profitPercent = product.profitPercent,
            fixedPrice = product.fixedPrice,
            vatExempt = product.vatExempt,
            quantity = quantity,
        )
    }
}

/** ریز محاسبهٔ یک سطر فاکتور */
data class LineBreakdown(
    val item: SaleItem,
    val goldWeightGrams: Double,
    val ratePerGram: Long,
    /** ارزش طلای خام برای یک عدد */
    val unitGoldValue: Long,
    val unitWage: Long,
    val unitProfit: Long,
    val unitStoneValue: Long,
    val unitPriceBeforeTax: Long,
    val quantity: Int,
    val grossBeforeTax: Long,
    val taxableBase: Long,
    val discount: Long,
    val vat: Long,
    val total: Long,
) {
    val totalWeightGrams: Double get() = item.weightGrams * quantity
}

/** جمع‌بندی کل فاکتور */
data class InvoiceTotals(
    val lines: List<LineBreakdown>,
    val goldValueTotal: Long,
    val wageTotal: Long,
    val profitTotal: Long,
    val stoneTotal: Long,
    val grossBeforeTax: Long,
    val itemDiscountTotal: Long,
    val invoiceDiscount: Long,
    val discountTotal: Long,
    val vatTotal: Long,
    val netTotal: Long,
    val roundingAdjustment: Long,
    val payable: Long,
    val totalWeightGrams: Double,
    val itemCount: Int,
) {
    companion object {
        val EMPTY = InvoiceTotals(
            lines = emptyList(),
            goldValueTotal = 0,
            wageTotal = 0,
            profitTotal = 0,
            stoneTotal = 0,
            grossBeforeTax = 0,
            itemDiscountTotal = 0,
            invoiceDiscount = 0,
            discountTotal = 0,
            vatTotal = 0,
            netTotal = 0,
            roundingAdjustment = 0,
            payable = 0,
            totalWeightGrams = 0.0,
            itemCount = 0,
        )
    }
}

/** فاکتور ذخیره‌شده */
data class Invoice(
    val id: Long = 0,
    val number: String,
    val createdAt: Long,
    val customerId: Long? = null,
    val customerName: String = "مشتری متفرقه",
    val customerPhone: String = "",
    val customerNationalCode: String = "",
    val goldRateSnapshot: Long = 0,
    val vatPercentSnapshot: Double = 0.0,
    val grossBeforeTax: Long = 0,
    val itemDiscountTotal: Long = 0,
    val invoiceDiscount: Long = 0,
    val vatTotal: Long = 0,
    val roundingAdjustment: Long = 0,
    val payable: Long = 0,
    val paidAmount: Long = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val status: InvoiceStatus = InvoiceStatus.UNPAID,
    val note: String = "",
    val lines: List<InvoiceLine> = emptyList(),
) {
    val remaining: Long get() = (payable - paidAmount).coerceAtLeast(0)
    val totalWeightGrams: Double get() = lines.sumOf { it.weightGrams * it.quantity }
}

/** سطر ذخیره‌شدهٔ فاکتور */
data class InvoiceLine(
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val title: String,
    val description: String = "",
    val karat: Int = Karats.BASE,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val ratePerGram: Long = 0,
    val goldValue: Long = 0,
    val wage: Long = 0,
    val profit: Long = 0,
    val stoneValue: Long = 0,
    val discount: Long = 0,
    val vat: Long = 0,
    val total: Long = 0,
)
