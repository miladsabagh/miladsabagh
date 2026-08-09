package com.zarnegar.gold

import com.zarnegar.gold.core.PersianText
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianTextTest {

    @Test
    fun `converts digits in both directions`() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", PersianText.toPersianDigits("0123456789"))
        assertEquals("0123456789", PersianText.toEnglishDigits("۰۱۲۳۴۵۶۷۸۹"))
        assertEquals("0912", PersianText.toEnglishDigits("٠٩١٢")) // ارقام عربی
        assertEquals("کد ۱۲", PersianText.toPersianDigits("کد 12"))
    }

    @Test
    fun `groups thousands`() {
        assertEquals("۱٬۲۳۴٬۵۶۷", PersianText.formatNumber(1_234_567))
        assertEquals("۹۹۹", PersianText.formatNumber(999))
        assertEquals("۰", PersianText.formatNumber(0))
    }

    @Test
    fun `negative amounts keep the minus sign on the left`() {
        // در متن راست‌چین، عدد منفی داخل «جزیرهٔ چپ‌به‌راست» قرار می‌گیرد.
        assertEquals("\u2066-۱٬۰۰۰\u2069", PersianText.formatNumber(-1000))
        assertEquals("\u2066x\u2069", PersianText.isolateLtr("x"))
        assertEquals("", PersianText.isolateLtr(""))
    }

    @Test
    fun `formats grams with three decimals`() {
        assertEquals("۴٫۵۰۰ گرم", PersianText.formatGrams(4.5))
        assertEquals("۱۲٫۱۴۰ گرم", PersianText.formatGrams(12.14))
        assertEquals("۰٫۰۰۰", PersianText.formatGrams(0.0, withUnit = false))
    }

    @Test
    fun `formats percentages compactly`() {
        assertEquals("۷٪", PersianText.formatPercent(7.0))
        assertEquals("۷٫۵٪", PersianText.formatPercent(7.5))
        assertEquals("۱۰٪", PersianText.formatPercent(10.0))
    }

    @Test
    fun `spells numbers in persian words`() {
        assertEquals("صفر", PersianText.numberToWords(0))
        assertEquals("هفت", PersianText.numberToWords(7))
        assertEquals("پانزده", PersianText.numberToWords(15))
        assertEquals("بیست و یک", PersianText.numberToWords(21))
        assertEquals("صد", PersianText.numberToWords(100))
        assertEquals("سیصد و چهل و پنج", PersianText.numberToWords(345))
        assertEquals("یک هزار", PersianText.numberToWords(1000))
        assertEquals(
            "یک میلیون و دویست و پنجاه هزار",
            PersianText.numberToWords(1_250_000),
        )
        assertEquals(
            "نود و دو میلیون و پانصد هزار",
            PersianText.numberToWords(92_500_000),
        )
        assertEquals(
            "یک میلیارد و دویست میلیون",
            PersianText.numberToWords(1_200_000_000),
        )
        assertEquals("منفی پنجاه", PersianText.numberToWords(-50))
    }
}
