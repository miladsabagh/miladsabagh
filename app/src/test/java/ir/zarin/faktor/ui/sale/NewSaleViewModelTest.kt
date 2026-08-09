package ir.zarin.faktor.ui.sale

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ir.zarin.faktor.R
import ir.zarin.faktor.TestSamples
import ir.zarin.faktor.data.local.AppDatabase
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PaymentStatus
import ir.zarin.faktor.data.repository.CustomerRepository
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.repository.ProductRepository
import ir.zarin.faktor.data.settings.SettingsRepository
import ir.zarin.faktor.domain.GoldPricing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NewSaleViewModelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: AppDatabase
    private lateinit var invoiceRepository: InvoiceRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        invoiceRepository = InvoiceRepository(database)
        settingsRepository = SettingsRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `فاکتور با اقلام و جمع کل درست ثبت می شود`() = runTest {
        settingsRepository.update(TestSamples.settings)
        val viewModel = createViewModel()
        awaitGoldRate(viewModel)

        viewModel.selectCustomer(TestSamples.customer)
        TestSamples.saleLines.forEach(viewModel::addLine)
        viewModel.setInvoiceDiscount(2_000_000)
        viewModel.setPaymentMethod(PaymentMethod.CARD)

        val expectedTotal = GoldPricing.totals(
            TestSamples.saleLines.map { it.breakdown(TestSamples.settings.pricingContext) },
            invoiceDiscountRial = 2_000_000,
        ).grandTotalRial
        assertEquals(expectedTotal, viewModel.uiState.value.grandTotalRial)

        viewModel.submit()
        val invoiceId = awaitSavedInvoiceId(viewModel)

        val stored = invoiceRepository.getWithItems(invoiceId)!!
        assertEquals(3, stored.items.size)
        assertEquals(expectedTotal, stored.invoice.grandTotalRial)
        assertEquals("مریم احمدی", stored.invoice.customerName)
        assertEquals(PaymentMethod.CARD, stored.invoice.paymentMethod)
        assertEquals(PaymentStatus.PAID, stored.invoice.status)
        assertEquals(TestSamples.settings.goldRatePerGramRial, stored.invoice.goldRatePerGramRial)
        assertTrue("شماره فاکتور تولید نشد", stored.invoice.number.endsWith("-001"))
    }

    @Test
    fun `پرداخت جزئی مانده فاکتور را ثبت می کند`() = runTest {
        settingsRepository.update(TestSamples.settings)
        val viewModel = createViewModel()
        awaitGoldRate(viewModel)

        viewModel.addLine(TestSamples.saleLines.first())
        viewModel.setFullyPaid(false)
        viewModel.setPaidAmount(10_000_000)

        val total = viewModel.uiState.value.grandTotalRial
        assertEquals(total - 10_000_000, viewModel.uiState.value.remainingRial)

        viewModel.submit()
        val invoiceId = awaitSavedInvoiceId(viewModel)

        val stored = invoiceRepository.getWithItems(invoiceId)!!.invoice
        assertEquals(10_000_000L, stored.paidAmountRial)
        assertEquals(PaymentStatus.PARTIAL, stored.status)
    }

    @Test
    fun `بدون قلم کالا فاکتور صادر نمی شود`() = runTest {
        settingsRepository.update(TestSamples.settings)
        val viewModel = createViewModel()
        awaitGoldRate(viewModel)

        viewModel.submit()

        assertNull(viewModel.savedInvoiceId.value)
        assertEquals(R.string.add_at_least_one_item, viewModel.message.value)
    }

    @Test
    fun `بدون نرخ روز طلا فاکتور وزنی صادر نمی شود`() = runTest {
        settingsRepository.update(TestSamples.settings.copy(goldRatePerGramRial = 0))
        val viewModel = createViewModel()
        realTime { viewModel.uiState.first() }

        viewModel.addLine(TestSamples.saleLines.first())
        viewModel.submit()

        assertNull(viewModel.savedInvoiceId.value)
        assertEquals(R.string.set_gold_rate_first, viewModel.message.value)
    }

    /**
     * ViewModel را می‌سازد و یک مشترک دائمی برای [NewSaleViewModel.uiState] نگه می‌دارد؛
     * درست مانند صفحه‌ای که در حال نمایش است.
     */
    private fun TestScope.createViewModel(): NewSaleViewModel {
        val viewModel = NewSaleViewModel(
            invoiceRepository = invoiceRepository,
            productRepository = ProductRepository(database),
            customerRepository = CustomerRepository(database),
            settingsRepository = settingsRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }

    /** تا رسیدن تنظیمات از DataStore صبر می‌کند تا محاسبه قیمت معتبر باشد. */
    private suspend fun awaitGoldRate(viewModel: NewSaleViewModel) = realTime {
        viewModel.uiState.first { it.settings.goldRatePerGramRial > 0 }
    }

    private suspend fun awaitSavedInvoiceId(viewModel: NewSaleViewModel): Long =
        realTime { viewModel.savedInvoiceId.first { it != null }!! }

    /**
     * DataStore و Room روی نخ‌های واقعی کار می‌کنند، بنابراین انتظار باید با زمان
     * واقعی انجام شود نه زمان مجازی [runTest].
     */
    private suspend fun <T> realTime(block: suspend () -> T): T =
        withContext(Dispatchers.Default) { withTimeout(TIMEOUT_MILLIS) { block() } }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000L
    }
}
