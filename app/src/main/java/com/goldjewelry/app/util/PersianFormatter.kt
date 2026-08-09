package com.goldjewelry.app.util

import java.text.NumberFormat
import java.util.Locale

object PersianFormatter {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        return input.map { char ->
            if (char in '0'..'9') persianDigits[char - '0'] else char
        }.joinToString("")
    }

    fun formatCurrency(amount: Long): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
        return toPersianDigits(formatted) + " ریال"
    }

    fun formatWeight(grams: Double): String {
        val formatted = String.format(Locale.US, "%.3f", grams)
        return toPersianDigits(formatted) + " گرم"
    }

    fun formatKarat(karat: Int): String = toPersianDigits(karat.toString()) + " عیار"

    fun formatInvoiceNumber(number: String): String = toPersianDigits(number)
}
