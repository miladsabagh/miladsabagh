package com.goldgallery.app.logic

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PersianFormat {

    private val enDigits = ('0'..'9').toList()
    private val faDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            val i = enDigits.indexOf(c)
            sb.append(if (i >= 0) faDigits[i] else c)
        }
        return sb.toString()
    }

    fun toEnglishDigits(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            val i = faDigits.indexOf(c)
            sb.append(if (i >= 0) enDigits[i] else c)
        }
        return sb.toString()
    }

    private val us = DecimalFormatSymbols(Locale.US)
    private val grouping = DecimalFormat("#,###", us)
    private val decimal = DecimalFormat("#.###", us)

    fun number(value: Long): String =
        toPersianDigits(grouping.format(value)).replace(',', '٬')

    fun money(amount: Long): String = "${number(amount)} تومان"

    fun weight(grams: Double): String = "${weightRaw(grams)} گرم"

    fun weightRaw(grams: Double): String =
        toPersianDigits(decimal.format(grams)).replace('.', '٫')

    fun karat(karat: Int): String = "عیار ${toPersianDigits(karat.toString())}"
}
