package ir.zarin.faktor.core

import kotlin.math.roundToLong

/**
 * واحد نمایش مبالغ. همه مبالغ در پایگاه داده به «ریال» ذخیره می‌شوند و فقط هنگام
 * نمایش یا دریافت ورودی به واحد انتخابی کاربر تبدیل می‌شوند.
 */
enum class CurrencyUnit(val rialsPerUnit: Long) {
    RIAL(1L),
    TOMAN(10L),
    ;

    fun fromRial(amountRial: Long): Long =
        if (rialsPerUnit == 1L) amountRial else (amountRial.toDouble() / rialsPerUnit).roundToLong()

    fun toRial(amount: Long): Long = amount * rialsPerUnit

    companion object {
        fun fromNameOrDefault(name: String?): CurrencyUnit =
            entries.firstOrNull { it.name == name } ?: TOMAN
    }
}

object Money {

    /** مبلغ ریالی را به رشته‌ای با جداکننده هزارگان در واحد انتخابی تبدیل می‌کند. */
    fun format(amountRial: Long, unit: CurrencyUnit, persianDigits: Boolean = true): String {
        val grouped = PersianNumbers.group(unit.fromRial(amountRial))
        return if (persianDigits) PersianNumbers.toPersianDigits(grouped) else grouped
    }

    /** همان [format] به همراه نام واحد پول. */
    fun formatWithUnit(
        amountRial: Long,
        unit: CurrencyUnit,
        unitLabel: String,
        persianDigits: Boolean = true,
    ): String = "${format(amountRial, unit, persianDigits)} $unitLabel"

    /** «مبلغ به حروف» بر اساس واحد انتخابی. */
    fun inWords(amountRial: Long, unit: CurrencyUnit, unitLabel: String): String =
        "${NumberToPersianWords.convert(unit.fromRial(amountRial))} $unitLabel"

    fun formatNumber(value: Long, persianDigits: Boolean = true): String {
        val grouped = PersianNumbers.group(value)
        return if (persianDigits) PersianNumbers.toPersianDigits(grouped) else grouped
    }

    fun formatWeight(grams: Double, persianDigits: Boolean = true): String {
        val text = PersianNumbers.decimal(grams, maxDecimals = 3)
        return if (persianDigits) PersianNumbers.toPersianDigits(text) else text
    }

    fun formatPercent(value: Double, persianDigits: Boolean = true): String {
        val text = PersianNumbers.decimal(value, maxDecimals = 2)
        return if (persianDigits) PersianNumbers.toPersianDigits(text) else text
    }

    fun formatDate(date: JalaliDate, persianDigits: Boolean = true): String =
        if (persianDigits) PersianNumbers.toPersianDigits(date.format()) else date.format()
}
