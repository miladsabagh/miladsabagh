package com.zarnegar.gold.domain.pricing

import com.zarnegar.gold.domain.model.InvoiceTotals
import com.zarnegar.gold.domain.model.Karats
import com.zarnegar.gold.domain.model.LineBreakdown
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.PricingSettings
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.WageMode
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * موتور محاسبهٔ قیمت طلا و جواهر مطابق رویهٔ رایج بازار ایران.
 *
 * فرمول هر قلم:
 * ```
 * نرخ هر گرم    = نرخ گرم طلای ۱۸ عیار × (عیار کالا ÷ ۷۵۰)
 * ارزش طلا      = (وزن کل − وزن سنگ) × نرخ هر گرم
 * اجرت ساخت     = ارزش طلا × درصد اجرت      (یا: مبلغ ثابت هر گرم × وزن طلا)
 * سود فروشنده   = (ارزش طلا + اجرت) × درصد سود
 * مالیات ا.ا.   = (اجرت + سود [+ ارزش سنگ]) × نرخ مالیات
 * قیمت واحد     = ارزش طلا + اجرت + سود + ارزش سنگ + مالیات
 * ```
 *
 * مطابق قانون مالیات بر ارزش افزوده، مالیات فقط به اجرت ساخت و سود فروشنده تعلق می‌گیرد
 * و وزن طلای خام از آن معاف است. شمول ارزش سنگ با تنظیم [PricingSettings.vatOnStone]
 * قابل تغییر است.
 */
object GoldPricing {

    /** نرخ هر گرم طلا بر اساس عیار کالا */
    fun ratePerGram(base18kRate: Long, karat: Int): Long =
        (base18kRate.toDouble() * karat / Karats.BASE).roundToLong()

    /**
     * محاسبهٔ یک سطر.
     *
     * @param extraDiscount تخفیف اضافه‌ای که از سطح فاکتور به این سطر تخصیص یافته است.
     */
    fun priceLine(
        item: SaleItem,
        settings: PricingSettings,
        extraDiscount: Long = 0,
    ): LineBreakdown {
        val quantity = item.quantity.coerceAtLeast(1)

        val goldWeight: Double
        val rate: Long
        val goldValue: Long
        val wage: Long
        val profit: Long
        val stoneValue: Long
        val unitTaxable: Long

        if (item.pricingMode == PricingMode.FIXED) {
            goldWeight = 0.0
            rate = 0
            goldValue = 0
            wage = 0
            profit = 0
            stoneValue = item.fixedPrice
            unitTaxable = if (item.vatExempt) 0 else item.fixedPrice
        } else {
            goldWeight = (item.weightGrams - item.stoneWeightGrams).coerceAtLeast(0.0)
            rate = ratePerGram(settings.goldRatePerGram18k, item.karat)
            val goldValueRaw = goldWeight * rate
            val wageRaw = when (item.wageMode) {
                WageMode.PERCENT -> goldValueRaw * item.wagePercent / 100.0
                WageMode.PER_GRAM -> item.wagePerGram * goldWeight
            }
            val profitRaw = (goldValueRaw + wageRaw) * item.profitPercent / 100.0

            goldValue = goldValueRaw.roundToLong()
            wage = wageRaw.roundToLong()
            profit = profitRaw.roundToLong()
            stoneValue = item.stoneValue
            unitTaxable = if (item.vatExempt) {
                0
            } else {
                wage + profit + if (settings.vatOnStone) stoneValue else 0
            }
        }

        val unitBeforeTax = goldValue + wage + profit + stoneValue
        val gross = unitBeforeTax * quantity
        val taxableBase = unitTaxable * quantity
        val discount = (item.discount + extraDiscount).coerceIn(0, gross)

        // تخفیف به نسبت بین بخش مشمول و بخش معاف سرشکن می‌شود.
        val discountRatio = if (gross > 0) discount.toDouble() / gross else 0.0
        val effectiveTaxable = taxableBase * (1.0 - discountRatio)
        val vat = (effectiveTaxable * settings.vatPercent / 100.0).roundToLong()

        return LineBreakdown(
            item = item.copy(quantity = quantity),
            goldWeightGrams = goldWeight,
            ratePerGram = rate,
            unitGoldValue = goldValue,
            unitWage = wage,
            unitProfit = profit,
            unitStoneValue = stoneValue,
            unitPriceBeforeTax = unitBeforeTax,
            quantity = quantity,
            grossBeforeTax = gross,
            taxableBase = taxableBase,
            discount = discount,
            vat = vat,
            total = gross - discount + vat,
        )
    }

    /**
     * محاسبهٔ کل فاکتور.
     *
     * تخفیف کلی فاکتور به نسبت مبلغ هر سطر بین سطرها سرشکن می‌شود تا مالیات هر سطر
     * روی مبلغ واقعی پس از تخفیف محاسبه شود.
     */
    fun priceInvoice(
        items: List<SaleItem>,
        settings: PricingSettings,
        invoiceDiscount: Long = 0,
    ): InvoiceTotals {
        if (items.isEmpty()) return InvoiceTotals.EMPTY

        val base = items.map { priceLine(it, settings) }
        val discountableTotal = base.sumOf { (it.grossBeforeTax - it.discount).coerceAtLeast(0) }
        val effectiveInvoiceDiscount = invoiceDiscount.coerceIn(0, discountableTotal)
        val allocations = allocateProportionally(
            amount = effectiveInvoiceDiscount,
            weights = base.map { (it.grossBeforeTax - it.discount).coerceAtLeast(0) },
        )

        val lines = items.mapIndexed { index, item ->
            priceLine(item, settings, extraDiscount = allocations[index])
        }

        val grossBeforeTax = lines.sumOf { it.grossBeforeTax }
        val itemDiscountTotal = lines.sumOf { it.item.discount }
        val vatTotal = lines.sumOf { it.vat }
        val netTotal = lines.sumOf { it.total }
        val rounded = roundAmount(netTotal, settings.roundTo)

        return InvoiceTotals(
            lines = lines,
            goldValueTotal = lines.sumOf { it.unitGoldValue * it.quantity },
            wageTotal = lines.sumOf { it.unitWage * it.quantity },
            profitTotal = lines.sumOf { it.unitProfit * it.quantity },
            stoneTotal = lines.sumOf { it.unitStoneValue * it.quantity },
            grossBeforeTax = grossBeforeTax,
            itemDiscountTotal = itemDiscountTotal,
            invoiceDiscount = effectiveInvoiceDiscount,
            discountTotal = itemDiscountTotal + effectiveInvoiceDiscount,
            vatTotal = vatTotal,
            netTotal = netTotal,
            roundingAdjustment = rounded - netTotal,
            payable = rounded,
            totalWeightGrams = lines.sumOf { it.item.weightGrams * it.quantity },
            itemCount = lines.sumOf { it.quantity },
        )
    }

    /** گرد کردن مبلغ به نزدیک‌ترین مضرب [unit]؛ `unit <= 1` یعنی بدون گرد کردن. */
    fun roundAmount(amount: Long, unit: Long): Long {
        if (unit <= 1L) return amount
        val remainder = amount % unit
        if (remainder == 0L) return amount
        return if (abs(remainder) * 2 >= unit) {
            amount - remainder + unit
        } else {
            amount - remainder
        }
    }

    /**
     * سرشکن کردن یک مبلغ صحیح بین چند سهم، به‌روش «بزرگ‌ترین باقی‌مانده»، طوری‌که
     * مجموع سهم‌ها دقیقاً برابر مبلغ اولیه بماند.
     */
    internal fun allocateProportionally(amount: Long, weights: List<Long>): List<Long> {
        val result = MutableList(weights.size) { 0L }
        if (amount <= 0) return result
        val totalWeight = weights.sum()
        if (totalWeight <= 0) return result

        var allocated = 0L
        val remainders = ArrayList<Pair<Int, Double>>(weights.size)
        for (index in weights.indices) {
            val exact = amount.toDouble() * weights[index] / totalWeight
            val floor = exact.toLong()
            result[index] = floor
            allocated += floor
            remainders.add(index to (exact - floor))
        }
        remainders.sortByDescending { it.second }
        var leftover = amount - allocated
        var cursor = 0
        while (leftover > 0 && remainders.isNotEmpty()) {
            val index = remainders[cursor % remainders.size].first
            if (weights[index] > 0) {
                result[index] = result[index] + 1
                leftover--
            }
            cursor++
            if (cursor > remainders.size * 2) break
        }
        return result
    }
}
