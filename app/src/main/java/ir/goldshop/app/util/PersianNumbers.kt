package ir.goldshop.app.util

import java.text.DecimalFormat
import kotlin.math.roundToLong

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** تبدیل ارقام انگلیسی به فارسی، برای نمایش زیباتر اعداد در رابط کاربری. */
fun String.toPersianDigits(): String = buildString {
    for (ch in this@toPersianDigits) {
        if (ch in '0'..'9') append(persianDigits[ch - '0']) else append(ch)
    }
}

/** تبدیل ارقام فارسی/عربی به انگلیسی، برای پردازش ورودی کاربر. */
fun String.toEnglishDigits(): String = buildString {
    for (ch in this@toEnglishDigits) {
        when (ch) {
            in '۰'..'۹' -> append(('0' + (ch - '۰')))
            in '٠'..'٩' -> append(('0' + (ch - '٠')))
            else -> append(ch)
        }
    }
}

private val groupingFormat = DecimalFormat("#,###")
private val weightFormat = DecimalFormat("#,##0.###")

private fun String.toPersianSeparators(): String = this
    .replace(",", "٬")
    .replace(".", "٫")

/** فرمت عدد با جداکننده هزارگان فارسی و ارقام فارسی، مناسب نمایش مبالغ تومانی. */
fun Double.formatToman(): String {
    val rounded = this.roundToLong()
    return groupingFormat.format(rounded).toPersianDigits().toPersianSeparators()
}

fun Double.formatWeight(): String {
    return weightFormat.format(this).toPersianDigits().toPersianSeparators()
}

fun Int.toPersianString(): String = this.toString().toPersianDigits()
