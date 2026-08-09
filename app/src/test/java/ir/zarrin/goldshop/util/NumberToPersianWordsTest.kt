package ir.zarrin.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberToPersianWordsTest {

    @Test
    fun `اعداد کوچک`() {
        assertEquals("صفر", NumberToPersianWords.convert(0))
        assertEquals("یک", NumberToPersianWords.convert(1))
        assertEquals("پانزده", NumberToPersianWords.convert(15))
        assertEquals("بیست و یک", NumberToPersianWords.convert(21))
        assertEquals("نود و نه", NumberToPersianWords.convert(99))
    }

    @Test
    fun `صدگان و هزارگان`() {
        assertEquals("صد", NumberToPersianWords.convert(100))
        assertEquals("صد و پنج", NumberToPersianWords.convert(105))
        assertEquals("نهصد و نود و نه", NumberToPersianWords.convert(999))
        assertEquals("هزار", NumberToPersianWords.convert(1_000))
        assertEquals("هزار و پانصد", NumberToPersianWords.convert(1_500))
        assertEquals("دو هزار و سیصد و چهل و پنج", NumberToPersianWords.convert(2_345))
    }

    @Test
    fun `مبالغ بزرگ فاکتور`() {
        assertEquals(
            "دوازده میلیون و پانصد هزار",
            NumberToPersianWords.convert(12_500_000)
        )
        assertEquals(
            "بیست و هفت میلیون و نهصد و پنجاه و پنج هزار و پانصد و نود و چهار",
            NumberToPersianWords.convert(27_955_594)
        )
        assertEquals(
            "یک میلیارد و دویست میلیون",
            NumberToPersianWords.convert(1_200_000_000)
        )
    }

    @Test
    fun `اعداد منفی`() {
        assertEquals("منفی پانصد", NumberToPersianWords.convert(-500))
    }

    @Test
    fun `مبلغ همراه با واحد پول`() {
        assertEquals(
            "دوازده میلیون و پانصد هزار تومان",
            NumberToPersianWords.amountToWords(12_500_000, "تومان")
        )
    }
}
