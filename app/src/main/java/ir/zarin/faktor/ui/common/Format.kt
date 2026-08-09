package ir.zarin.faktor.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import ir.zarin.faktor.R
import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.core.Money
import ir.zarin.faktor.core.PersianNumbers

/** قالب‌بندی مبالغ، اعداد و تاریخ بر اساس تنظیمات کاربر. */
@Immutable
data class DisplayFormat(
    val unit: CurrencyUnit = CurrencyUnit.TOMAN,
    val persianDigits: Boolean = true,
) {
    fun money(amountRial: Long): String = Money.format(amountRial, unit, persianDigits)

    fun number(value: Long): String = Money.formatNumber(value, persianDigits)

    fun count(value: Int): String = Money.formatNumber(value.toLong(), persianDigits)

    fun weight(grams: Double): String = Money.formatWeight(grams, persianDigits)

    fun percent(value: Double): String = Money.formatPercent(value, persianDigits)

    fun digits(text: String): String =
        if (persianDigits) PersianNumbers.toPersianDigits(text) else text

    fun date(millis: Long): String = digits(JalaliCalendar.fromEpochMillis(millis).format())

    fun dateLong(millis: Long): String = digits(JalaliCalendar.fromEpochMillis(millis).formatLong())

    fun time(millis: Long): String = digits(JalaliCalendar.formatTime(millis))

    /** مقدار ورودی فیلدهای مبلغ در واحد نمایش (نه ریال). */
    fun amountForInput(amountRial: Long): String =
        if (amountRial == 0L) "" else unit.fromRial(amountRial).toString()

    fun inputToRial(text: String): Long =
        PersianNumbers.parseLong(text)?.let { unit.toRial(it) } ?: 0L
}

val LocalDisplayFormat = staticCompositionLocalOf { DisplayFormat() }

@Composable
fun currencyLabel(unit: CurrencyUnit = LocalDisplayFormat.current.unit): String =
    stringResource(if (unit == CurrencyUnit.RIAL) R.string.rial else R.string.toman)
