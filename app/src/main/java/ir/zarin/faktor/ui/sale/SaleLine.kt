package ir.zarin.faktor.ui.sale

import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.model.ProductCategory
import ir.zarin.faktor.domain.GoldPricing
import ir.zarin.faktor.domain.PriceBreakdown
import ir.zarin.faktor.domain.PriceLineInput
import ir.zarin.faktor.domain.PricingContext
import java.util.UUID

/** یک قلم در سبد فاکتور در حال ثبت. */
data class SaleLine(
    val key: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val name: String = "",
    val category: ProductCategory = ProductCategory.GOLD,
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val karat: Int = 18,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val wagePercent: Double = 0.0,
    val stonePriceRial: Long = 0,
    val unitFixedPriceRial: Long = 0,
    val applyVat: Boolean = true,
    val discountRial: Long = 0,
) {
    fun toPriceLineInput(): PriceLineInput = PriceLineInput(
        pricingMode = pricingMode,
        quantity = quantity,
        weightGrams = weightGrams,
        karat = karat,
        wagePercent = wagePercent,
        stonePriceRial = stonePriceRial,
        unitFixedPriceRial = unitFixedPriceRial,
        applyVat = applyVat,
        discountRial = discountRial,
    )

    fun breakdown(context: PricingContext): PriceBreakdown =
        GoldPricing.calculate(toPriceLineInput(), context)

    fun toInvoiceItem(breakdown: PriceBreakdown): InvoiceItem = InvoiceItem(
        productId = productId,
        name = name,
        category = category,
        pricingMode = pricingMode,
        karat = karat,
        weightGrams = weightGrams,
        quantity = quantity,
        wagePercent = wagePercent,
        stonePriceRial = stonePriceRial,
        unitFixedPriceRial = unitFixedPriceRial,
        applyVat = applyVat,
        goldValueRial = breakdown.goldValueRial,
        wageRial = breakdown.wageRial,
        profitRial = breakdown.profitRial,
        stoneRial = breakdown.stoneRial,
        vatRial = breakdown.vatRial,
        discountRial = breakdown.discountRial,
        lineTotalRial = breakdown.totalRial,
    )

    companion object {
        fun fromProduct(product: Product, quantity: Int = 1): SaleLine = SaleLine(
            productId = product.id.takeIf { it != 0L },
            name = product.name,
            category = product.category,
            pricingMode = product.pricingMode,
            karat = product.karat,
            weightGrams = product.weightGrams,
            quantity = quantity,
            wagePercent = product.wagePercent,
            stonePriceRial = product.stonePriceRial,
            unitFixedPriceRial = product.fixedPriceRial,
            applyVat = product.applyVat,
        )
    }
}
