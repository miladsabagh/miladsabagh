package ir.zarrin.goldshop

import ir.zarrin.goldshop.core.PersianNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersianNumbersTest {

    @Test
    fun `converts digits in both directions`() {
        assertEquals("۱۴۰۳/۰۵/۱۹", PersianNumbers.toPersianDigits("1403/05/19"))
        assertEquals("1403", PersianNumbers.toEnglishDigits("۱۴۰۳"))
        assertEquals("12.5", PersianNumbers.toEnglishDigits("۱۲٫۵"))
    }

    @Test
    fun `groups thousands`() {
        assertEquals("1,234,567", PersianNumbers.groupDigits(1_234_567L))
        assertEquals("500", PersianNumbers.groupDigits(500L))
        assertEquals("-1,000", PersianNumbers.groupDigits(-1000L))
        assertEquals("۱۲,۵۰۰,۰۰۰", PersianNumbers.formatAmount(12_500_000L))
    }

    @Test
    fun `formats weights without trailing zeros`() {
        assertEquals("۱۲٫۵", PersianNumbers.formatWeight(12.5))
        assertEquals("۳", PersianNumbers.formatWeight(3.0))
        assertEquals("۰٫۲۵", PersianNumbers.formatWeight(0.25))
    }

    @Test
    fun `parses amounts typed with Persian digits or separators`() {
        assertEquals(1_250_000L, PersianNumbers.parseLong("۱,۲۵۰,۰۰۰"))
        assertEquals(3500000L, PersianNumbers.parseLong("3500000"))
        assertNull(PersianNumbers.parseLong("طلا"))
        assertEquals(12.5, PersianNumbers.parseDouble("۱۲٫۵")!!, 0.0001)
    }

    @Test
    fun `spells amounts out in Persian`() {
        assertEquals("صفر", PersianNumbers.toWords(0))
        assertEquals("صد و بیست و سه", PersianNumbers.toWords(123))
        assertEquals("دوازده هزار و پانصد", PersianNumbers.toWords(12_500))
        assertEquals("یک هزار و چهارصد و سه", PersianNumbers.toWords(1_403))
        assertEquals("یک میلیون", PersianNumbers.toWords(1_000_000))
        assertEquals(
            "بیست میلیون و نهصد و هفت هزار و دویست و پنجاه",
            PersianNumbers.toWords(20_907_250)
        )
    }
}
