package ir.zarrin.goldshop.domain

import ir.zarrin.goldshop.core.PersianNumbers.roundToLong
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode

/** Everything needed to price a single invoice line. */
data class LineInput(
    val kind: ItemKind = ItemKind.MANUFACTURED,
    val karat: Int = Karat.K18,
    val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    val goldRatePerGram: Long = 0L,
    val wageMode: WageMode = WageMode.PERCENT,
    val wageValue: Double = 0.0,
    val profitPercent: Double = 0.0,
    val stonePrice: Long = 0L,
    val taxBasis: TaxBasis = TaxBasis.WAGE_AND_PROFIT,
    val taxPercent: Double = 0.0,
    /** Set for coins, gems or services that are sold at a flat price instead of by weight. */
    val unitPriceOverride: Long? = null
)

/** Per-line breakdown; every field is for a single piece except [total]. */
data class LineTotals(
    val goldValue: Long = 0L,
    val wage: Long = 0L,
    val profit: Long = 0L,
    val stone: Long = 0L,
    val taxableBase: Long = 0L,
    val tax: Long = 0L,
    val unitPrice: Long = 0L,
    val total: Long = 0L
)

/** Aggregated totals for a whole invoice. */
data class InvoiceTotals(
    val goldValue: Long = 0L,
    val wage: Long = 0L,
    val profit: Long = 0L,
    val stone: Long = 0L,
    val tax: Long = 0L,
    val gross: Long = 0L,
    val discount: Long = 0L,
    val payable: Long = 0L,
    val paid: Long = 0L,
    val remaining: Long = 0L,
    val totalWeightGrams: Double = 0.0,
    val pieceCount: Int = 0
)

/**
 * Pricing rules used by Iranian gold retailers.
 *
 * ```
 * gold value = weight × rate of the line karat
 * making charge (اجرت) = percentage of the gold value, an amount per gram, or a flat amount
 * profit (سود) = percentage of (gold value + making charge)
 * VAT (مالیات) = percentage of the taxable base, which for finished gold is (اجرت + سود)
 * line price = gold value + making charge + profit + stone price + VAT
 * ```
 */
object GoldCalculator {

    /**
     * Converts the shop's quoted 18 karat (750) gram rate to the rate of another purity.
     * A 24 karat gram is worth `999 / 750` of an 18 karat gram.
     */
    fun rateForKarat(baseRatePerGram18K: Long, karat: Int): Long {
        if (karat <= 0) return 0L
        return roundToLong(baseRatePerGram18K.toDouble() * karat / Karat.K18)
    }

    fun calculateLine(input: LineInput): LineTotals {
        val quantity = input.quantity.coerceAtLeast(0)
        val goldValue = input.unitPriceOverride
            ?: roundToLong(input.weightGrams.coerceAtLeast(0.0) * input.goldRatePerGram)

        val wage = when (input.wageMode) {
            WageMode.PERCENT -> roundToLong(goldValue * input.wageValue / 100.0)
            WageMode.PER_GRAM -> roundToLong(input.weightGrams.coerceAtLeast(0.0) * input.wageValue)
            WageMode.FIXED -> roundToLong(input.wageValue)
        }.coerceAtLeast(0L)

        val profit = roundToLong((goldValue + wage) * input.profitPercent / 100.0).coerceAtLeast(0L)
        val stone = input.stonePrice.coerceAtLeast(0L)

        val taxableBase = when (input.taxBasis) {
            TaxBasis.WAGE_AND_PROFIT -> wage + profit
            TaxBasis.FULL_PRICE -> goldValue + wage + profit + stone
            TaxBasis.EXEMPT -> 0L
        }
        val tax = roundToLong(taxableBase * input.taxPercent / 100.0).coerceAtLeast(0L)

        val unitPrice = goldValue + wage + profit + stone + tax
        return LineTotals(
            goldValue = goldValue,
            wage = wage,
            profit = profit,
            stone = stone,
            taxableBase = taxableBase,
            tax = tax,
            unitPrice = unitPrice,
            total = unitPrice * quantity
        )
    }

    fun summarize(
        lines: List<Pair<LineInput, LineTotals>>,
        discount: Long = 0L,
        paid: Long = 0L
    ): InvoiceTotals {
        var goldValue = 0L
        var wage = 0L
        var profit = 0L
        var stone = 0L
        var tax = 0L
        var gross = 0L
        var weight = 0.0
        var pieces = 0

        for ((input, totals) in lines) {
            val quantity = input.quantity.coerceAtLeast(0)
            goldValue += totals.goldValue * quantity
            wage += totals.wage * quantity
            profit += totals.profit * quantity
            stone += totals.stone * quantity
            tax += totals.tax * quantity
            gross += totals.total
            weight += input.weightGrams.coerceAtLeast(0.0) * quantity
            pieces += quantity
        }

        val safeDiscount = discount.coerceIn(0L, gross)
        val payable = gross - safeDiscount
        val safePaid = paid.coerceAtLeast(0L)
        return InvoiceTotals(
            goldValue = goldValue,
            wage = wage,
            profit = profit,
            stone = stone,
            tax = tax,
            gross = gross,
            discount = safeDiscount,
            payable = payable,
            paid = safePaid,
            remaining = payable - safePaid,
            totalWeightGrams = weight,
            pieceCount = pieces
        )
    }
}
