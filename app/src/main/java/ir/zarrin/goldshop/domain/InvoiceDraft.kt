package ir.zarrin.goldshop.domain

import ir.zarrin.goldshop.data.db.ProductEntity

/** یک ردیف در حال ویرایش فاکتور (هنوز ذخیره نشده) */
data class DraftItem(
    val key: Long,
    val productId: Long? = null,
    val name: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val karat: Int = Karats.BASE,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val wagePercent: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Long = 0L,
    val fixedPrice: Long = 0L,
    val taxable: Boolean = true,
    val discount: Long = 0L,
    /** حداکثر تعداد قابل فروش بر اساس موجودی انبار (صفر یعنی بدون محدودیت) */
    val availableStock: Int = 0
) {
    fun priceInput(baseRate18: Long, taxPercent: Double) = PriceInput(
        pricingMode = pricingMode,
        weightGrams = weightGrams,
        karat = karat,
        baseRatePerGram = baseRate18,
        wagePercent = wagePercent,
        profitPercent = profitPercent,
        stonePrice = stonePrice,
        fixedPrice = fixedPrice,
        taxPercent = taxPercent,
        taxable = taxable,
        quantity = quantity,
        discount = discount
    )

    fun breakdown(baseRate18: Long, taxPercent: Double): PriceBreakdown =
        PricingEngine.calculate(priceInput(baseRate18, taxPercent))

    companion object {
        fun fromProduct(product: ProductEntity, key: Long): DraftItem = DraftItem(
            key = key,
            productId = product.id,
            name = product.name,
            category = ProductCategory.fromName(product.category),
            karat = product.karat,
            weightGrams = product.weightGrams,
            quantity = 1,
            pricingMode = PricingMode.fromName(product.pricingMode),
            wagePercent = product.wagePercent,
            profitPercent = product.profitPercent,
            stonePrice = product.stonePrice,
            fixedPrice = product.fixedPrice,
            taxable = product.taxable,
            availableStock = product.stockQty
        )
    }
}
