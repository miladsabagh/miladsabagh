package ir.zarrin.goldshop.domain

import kotlin.math.roundToLong

/**
 * ورودی محاسبه قیمت یک ردیف فاکتور.
 *
 * تمام مبالغ بر حسب واحد پایه (تومان) و به صورت عدد صحیح نگهداری می‌شوند.
 */
data class PriceInput(
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val weightGrams: Double = 0.0,
    val karat: Int = Karats.BASE,
    /** نرخ هر گرم طلای ۱۸ عیار */
    val baseRatePerGram: Long = 0L,
    val wagePercent: Double = 0.0,
    val profitPercent: Double = 0.0,
    /** بهای نگین یا سنگ که مشمول اجرت و سود نمی‌شود */
    val stonePrice: Long = 0L,
    /** قیمت مقطوع برای کالاهایی مانند سکه */
    val fixedPrice: Long = 0L,
    val taxPercent: Double = 0.0,
    val taxable: Boolean = true,
    val quantity: Int = 1,
    /** تخفیف ردیف که پس از محاسبه مالیات از جمع ردیف کم می‌شود */
    val discount: Long = 0L
)

/**
 * ریز محاسبات قیمت یک ردیف فاکتور.
 *
 * مقادیر [goldValue] تا [unitPrice] برای «یک عدد» و مقادیر جمع ردیف
 * ([lineSubtotal] و [lineTotal]) برای کل تعداد محاسبه می‌شوند.
 */
data class PriceBreakdown(
    val goldValue: Long = 0L,
    val wage: Long = 0L,
    val profit: Long = 0L,
    val stone: Long = 0L,
    val taxBase: Long = 0L,
    val tax: Long = 0L,
    val unitPrice: Long = 0L,
    val quantity: Int = 1,
    val lineSubtotal: Long = 0L,
    val discount: Long = 0L,
    val lineTotal: Long = 0L
) {
    val totalGoldValue: Long get() = goldValue * quantity
    val totalWage: Long get() = wage * quantity
    val totalProfit: Long get() = profit * quantity
    val totalStone: Long get() = stone * quantity
    val totalTax: Long get() = tax * quantity
}

/**
 * موتور قیمت‌گذاری طلا مطابق روال رایج بازار ایران:
 *
 * ۱. بهای طلا  = وزن × نرخ هر گرم عیار مربوطه
 * ۲. اجرت ساخت = بهای طلا × درصد اجرت
 * ۳. سود فروشنده = (بهای طلا + اجرت) × درصد سود
 * ۴. مالیات بر ارزش افزوده = (اجرت + سود) × نرخ مالیات
 * ۵. قیمت نهایی = بهای طلا + اجرت + سود + مالیات + بهای نگین
 */
object PricingEngine {

    /** نرخ هر گرم طلا برای عیار دلخواه بر مبنای نرخ طلای ۱۸ عیار */
    fun ratePerGram(baseRate18: Long, karat: Int): Long {
        if (karat <= 0) return 0L
        return (baseRate18.toDouble() * karat / Karats.BASE).roundToLong()
    }

    fun calculate(input: PriceInput): PriceBreakdown {
        val quantity = input.quantity.coerceAtLeast(1)

        val goldValue: Long
        val wage: Long
        val profit: Long
        val stone: Long
        val taxBase: Long

        if (input.pricingMode == PricingMode.FIXED) {
            goldValue = 0L
            wage = 0L
            profit = 0L
            stone = 0L
            taxBase = if (input.taxable) input.fixedPrice.coerceAtLeast(0L) else 0L
        } else {
            val rate = ratePerGram(input.baseRatePerGram, input.karat)
            goldValue = (input.weightGrams.coerceAtLeast(0.0) * rate).roundToLong()
            wage = (goldValue * input.wagePercent.coerceAtLeast(0.0) / 100.0).roundToLong()
            profit = ((goldValue + wage) * input.profitPercent.coerceAtLeast(0.0) / 100.0).roundToLong()
            stone = input.stonePrice.coerceAtLeast(0L)
            taxBase = if (input.taxable) wage + profit else 0L
        }

        val tax = (taxBase * input.taxPercent.coerceAtLeast(0.0) / 100.0).roundToLong()
        val unitPrice = if (input.pricingMode == PricingMode.FIXED) {
            input.fixedPrice.coerceAtLeast(0L) + tax
        } else {
            goldValue + wage + profit + tax + stone
        }

        val lineSubtotal = unitPrice * quantity
        val discount = input.discount.coerceIn(0L, lineSubtotal)

        return PriceBreakdown(
            goldValue = goldValue,
            wage = wage,
            profit = profit,
            stone = stone,
            taxBase = taxBase,
            tax = tax,
            unitPrice = unitPrice,
            quantity = quantity,
            lineSubtotal = lineSubtotal,
            discount = discount,
            lineTotal = lineSubtotal - discount
        )
    }
}

/** جمع‌بندی کل فاکتور از روی ریز محاسبات ردیف‌ها */
data class InvoiceTotals(
    val goldValue: Long = 0L,
    val wage: Long = 0L,
    val profit: Long = 0L,
    val stone: Long = 0L,
    val tax: Long = 0L,
    val itemsDiscount: Long = 0L,
    val subtotal: Long = 0L,
    val invoiceDiscount: Long = 0L,
    val grandTotal: Long = 0L,
    val totalWeight: Double = 0.0,
    val itemCount: Int = 0
)

fun List<PriceBreakdown>.summarize(
    invoiceDiscount: Long = 0L,
    totalWeight: Double = 0.0
): InvoiceTotals {
    val subtotal = sumOf { it.lineTotal }
    val discount = invoiceDiscount.coerceIn(0L, subtotal)
    return InvoiceTotals(
        goldValue = sumOf { it.totalGoldValue },
        wage = sumOf { it.totalWage },
        profit = sumOf { it.totalProfit },
        stone = sumOf { it.totalStone },
        tax = sumOf { it.totalTax },
        itemsDiscount = sumOf { it.discount },
        subtotal = subtotal,
        invoiceDiscount = discount,
        grandTotal = subtotal - discount,
        totalWeight = totalWeight,
        itemCount = sumOf { it.quantity }
    )
}
