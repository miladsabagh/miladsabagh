package ir.zarin.faktor.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersianNumbersTest {

    @Test
    fun `ارقام لاتین به فارسی تبدیل می شود`() {
        assertEquals("۱۴۰۵/۰۵/۱۸", PersianNumbers.toPersianDigits("1405/05/18"))
        assertEquals("۱۲,۳۴۵ تومان", PersianNumbers.toPersianDigits("12,345 تومان"))
    }

    @Test
    fun `ارقام فارسی و عربی به لاتین تبدیل می شود`() {
        assertEquals("1234", PersianNumbers.toLatinDigits("۱۲۳۴"))
        assertEquals("1234", PersianNumbers.toLatinDigits("١٢٣٤"))
        assertEquals("12.5", PersianNumbers.toLatinDigits("۱۲٫۵"))
    }

    @Test
    fun `جداکننده هزارگان اضافه می شود`() {
        assertEquals("0", PersianNumbers.group(0))
        assertEquals("999", PersianNumbers.group(999))
        assertEquals("1,000", PersianNumbers.group(1_000))
        assertEquals("1,234,567", PersianNumbers.group(1_234_567))
        assertEquals("-12,345", PersianNumbers.group(-12_345))
    }

    @Test
    fun `اعداد اعشاری بدون صفر اضافه نمایش داده می شوند`() {
        assertEquals("3", PersianNumbers.decimal(3.0))
        assertEquals("2.5", PersianNumbers.decimal(2.5))
        assertEquals("1,000.25", PersianNumbers.decimal(1000.25))
        assertEquals("4.125", PersianNumbers.decimal(4.125))
    }

    @Test
    fun `ورودی کاربر با ارقام فارسی تجزیه می شود`() {
        assertEquals(12_345L, PersianNumbers.parseLong("۱۲,۳۴۵"))
        assertEquals(7L, PersianNumbers.parseLong(" ۷ "))
        assertEquals(2.75, PersianNumbers.parseDouble("۲.۷۵")!!, 0.0001)
        assertNull(PersianNumbers.parseLong(""))
        assertNull(PersianNumbers.parseLong("سلام"))
    }
}

class NumberToPersianWordsTest {

    @Test
    fun `اعداد کوچک به حروف تبدیل می شوند`() {
        assertEquals("صفر", NumberToPersianWords.convert(0))
        assertEquals("یک", NumberToPersianWords.convert(1))
        assertEquals("پانزده", NumberToPersianWords.convert(15))
        assertEquals("بیست و یک", NumberToPersianWords.convert(21))
        assertEquals("صد و پنج", NumberToPersianWords.convert(105))
        assertEquals("نهصد و نود و نه", NumberToPersianWords.convert(999))
    }

    @Test
    fun `اعداد بزرگ با یکای مقیاس تبدیل می شوند`() {
        assertEquals("یک هزار", NumberToPersianWords.convert(1_000))
        assertEquals(
            "یک میلیون و دویست و سی و چهار هزار و پانصد و شصت و هفت",
            NumberToPersianWords.convert(1_234_567),
        )
        assertEquals(
            "هجده میلیون و دویست و هفتاد و سه هزار و ششصد",
            NumberToPersianWords.convert(18_273_600),
        )
        assertEquals("دو میلیارد", NumberToPersianWords.convert(2_000_000_000))
    }

    @Test
    fun `عدد منفی با پیشوند منفی تبدیل می شود`() {
        assertEquals("منفی پانصد", NumberToPersianWords.convert(-500))
    }
}

class MoneyTest {

    @Test
    fun `تبدیل ریال به تومان انجام می شود`() {
        assertEquals(18_273_600L, CurrencyUnit.TOMAN.fromRial(182_736_000))
        assertEquals(182_736_000L, CurrencyUnit.RIAL.fromRial(182_736_000))
        assertEquals(182_736_000L, CurrencyUnit.TOMAN.toRial(18_273_600))
    }

    @Test
    fun `قالب بندی مبلغ با ارقام فارسی انجام می شود`() {
        assertEquals("۱۸,۲۷۳,۶۰۰", Money.format(182_736_000, CurrencyUnit.TOMAN))
        assertEquals("182,736,000", Money.format(182_736_000, CurrencyUnit.RIAL, persianDigits = false))
        assertEquals("۱۸,۲۷۳,۶۰۰ تومان", Money.formatWithUnit(182_736_000, CurrencyUnit.TOMAN, "تومان"))
    }

    @Test
    fun `مبلغ به حروف با واحد پول ساخته می شود`() {
        assertEquals(
            "هجده میلیون و دویست و هفتاد و سه هزار و ششصد تومان",
            Money.inWords(182_736_000, CurrencyUnit.TOMAN, "تومان"),
        )
    }

    @Test
    fun `وزن و درصد قالب بندی می شوند`() {
        assertEquals("۴.۵", Money.formatWeight(4.5))
        assertEquals("۱۲", Money.formatPercent(12.0))
        assertEquals("7.5", Money.formatPercent(7.5, persianDigits = false))
    }
}
