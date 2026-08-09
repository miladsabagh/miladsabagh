package ir.zarrin.goldshop.ui

import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.domain.model.Currency

fun formatMoney(value: Long, currency: Currency): String =
    "${PersianNumbers.formatAmount(value)} ${currency.shortLabel}"

fun formatCount(value: Int): String = PersianNumbers.toPersianDigits(value.toString())

fun formatGrams(value: Double): String = "${PersianNumbers.formatWeight(value)} گرم"
