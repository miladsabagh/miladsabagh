package ir.zarin.faktor.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.core.JalaliDate
import ir.zarin.faktor.data.local.AppDatabase
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.PaymentStatus
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.repository.ProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InvoiceRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var invoiceRepository: InvoiceRepository
    private lateinit var productRepository: ProductRepository

    private val invoiceDate = JalaliCalendar.toEpochMillis(JalaliDate(1405, 5, 18))

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        invoiceRepository = InvoiceRepository(database)
        productRepository = ProductRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `صدور فاکتور شماره ماهانه تولید می کند و اقلام را ذخیره می کند`() = runTest {
        val productId = productRepository.save(
            Product(name = "انگشتر طلا", weightGrams = 4.2, wagePercent = 12.0, stockQty = 3),
        )

        val invoiceId = invoiceRepository.createInvoice(
            invoice = sampleInvoice(),
            items = listOf(
                InvoiceItem(productId = productId, name = "انگشتر طلا", quantity = 1, lineTotalRial = 90_000_000),
                InvoiceItem(name = "سرویس جواهر", quantity = 1, lineTotalRial = 60_000_000),
            ),
            invoicePrefix = "",
        )

        val stored = invoiceRepository.getWithItems(invoiceId)!!
        assertEquals("140505-001", stored.invoice.number)
        assertEquals(2, stored.items.size)
        assertEquals(invoiceId, stored.items.first().invoiceId)
        assertEquals(PaymentStatus.PAID, stored.invoice.status)

        assertEquals(2, productRepository.getById(productId)!!.stockQty)
    }

    @Test
    fun `شماره فاکتورهای بعدی در همان ماه افزایش می یابد`() = runTest {
        repeat(3) {
            invoiceRepository.createInvoice(sampleInvoice(), listOf(InvoiceItem(name = "قلم")), "")
        }

        val numbers = invoiceRepository.observeAll().first().map { it.number }
        assertEquals(listOf("140505-003", "140505-002", "140505-001"), numbers)
    }

    @Test
    fun `پیش شماره فروشگاه در شماره فاکتور اعمال می شود`() = runTest {
        val id = invoiceRepository.createInvoice(sampleInvoice(), listOf(InvoiceItem(name = "قلم")), "ZR")

        assertEquals("ZR140505-001", invoiceRepository.getWithItems(id)!!.invoice.number)
    }

    @Test
    fun `گزارش فروش و مانده بدهی محاسبه می شود`() = runTest {
        invoiceRepository.createInvoice(
            sampleInvoice(total = 100_000_000, paid = 100_000_000),
            listOf(InvoiceItem(name = "قلم")),
            "",
        )
        invoiceRepository.createInvoice(
            sampleInvoice(total = 50_000_000, paid = 20_000_000),
            listOf(InvoiceItem(name = "قلم")),
            "",
        )

        assertEquals(150_000_000L, invoiceRepository.observeSalesSince(0).first())
        assertEquals(2, invoiceRepository.observeCountSince(0).first())
        assertEquals(30_000_000L, invoiceRepository.observeOutstanding().first())
    }

    @Test
    fun `حذف فاکتور اقلام آن را نیز حذف می کند`() = runTest {
        val id = invoiceRepository.createInvoice(sampleInvoice(), listOf(InvoiceItem(name = "قلم")), "")
        val stored = invoiceRepository.getWithItems(id)!!

        invoiceRepository.delete(stored.invoice)

        assertEquals(0, database.invoiceDao().observeAll().first().size)
        assertEquals(null, invoiceRepository.getWithItems(id))
    }

    @Test
    fun `ثبت تسویه مانده را صفر می کند`() = runTest {
        val id = invoiceRepository.createInvoice(
            sampleInvoice(total = 80_000_000, paid = 0),
            listOf(InvoiceItem(name = "قلم")),
            "",
        )

        invoiceRepository.updatePaidAmount(id, 80_000_000)

        val stored = invoiceRepository.getWithItems(id)!!.invoice
        assertEquals(0L, stored.remainingRial)
        assertEquals(PaymentStatus.PAID, stored.status)
    }

    private fun sampleInvoice(total: Long = 150_000_000, paid: Long = 150_000_000) = Invoice(
        dateMillis = invoiceDate,
        customerName = "علی رضایی",
        customerPhone = "09123456789",
        goldRatePerGramRial = 30_000_000,
        profitPercent = 7.0,
        vatPercent = 10.0,
        grandTotalRial = total,
        paidAmountRial = paid,
    )
}
