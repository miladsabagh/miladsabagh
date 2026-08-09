package ir.zarin.faktor.domain

import ir.zarin.faktor.core.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceNumberingTest {

    private val date = JalaliDate(1405, 5, 18)

    @Test
    fun `اولین فاکتور هر ماه از یک شروع می شود`() {
        assertEquals("140505-001", InvoiceNumbering.next(userPrefix = "", date = date, lastNumber = null))
    }

    @Test
    fun `شماره بعدی از آخرین شماره همان ماه ساخته می شود`() {
        assertEquals("140505-008", InvoiceNumbering.next("", date, "140505-007"))
        assertEquals("140505-100", InvoiceNumbering.next("", date, "140505-099"))
    }

    @Test
    fun `پیش شماره دلخواه فروشگاه اعمال می شود`() {
        assertEquals("A140505-001", InvoiceNumbering.next("A", date, null))
        assertEquals("A140505-013", InvoiceNumbering.next("A", date, "A140505-012"))
        assertEquals("A140505-", InvoiceNumbering.monthlyPrefix("A", date))
    }

    @Test
    fun `شماره ماه قبل شمارنده ماه جاری را جابه جا نمی کند`() {
        assertEquals("140505-001", InvoiceNumbering.next("", date, "140504-042"))
    }

    @Test
    fun `شماره فاکتورها در هر ماه به ترتیب الفبایی مرتب می مانند`() {
        val numbers = listOf(
            InvoiceNumbering.next("", date, null),
            InvoiceNumbering.next("", date, "140505-001"),
            InvoiceNumbering.next("", date, "140505-009"),
        )
        assertEquals(numbers, numbers.sorted())
    }
}
