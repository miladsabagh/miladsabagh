package ir.goldshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PersianNumbersTest {

    @Test
    fun `toPersianDigits converts english digits to persian`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", "1234567890".toPersianDigits())
        assertEquals("قیمت ۵۰۰۰ تومان", "قیمت 5000 تومان".toPersianDigits())
    }

    @Test
    fun `toEnglishDigits converts persian and arabic digits to english`() {
        assertEquals("1234567890", "۱۲۳۴۵۶۷۸۹۰".toEnglishDigits())
        assertEquals("1234567890", "١٢٣٤٥٦٧٨٩٠".toEnglishDigits())
    }

    @Test
    fun `formatToman groups thousands and uses persian digits`() {
        assertEquals("۱۰۰٬۰۰۰", 100_000.0.formatToman())
        assertEquals("۱٬۲۳۴٬۵۶۷", 1_234_567.0.formatToman())
        assertEquals("۰", 0.0.formatToman())
    }

    @Test
    fun `formatWeight preserves up to three decimal places`() {
        assertEquals("۲٫۵", 2.5.formatWeight())
        assertEquals("۱۰", 10.0.formatWeight())
    }
}
