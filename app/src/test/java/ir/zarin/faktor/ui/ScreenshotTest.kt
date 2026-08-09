package ir.zarin.faktor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import ir.zarin.faktor.TestSamples
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.ui.common.DisplayFormat
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.ZarinTopBar
import ir.zarin.faktor.ui.customers.CustomersContent
import ir.zarin.faktor.ui.customers.CustomersUiState
import ir.zarin.faktor.ui.dashboard.DashboardContent
import ir.zarin.faktor.ui.dashboard.DashboardUiState
import ir.zarin.faktor.ui.invoices.InvoiceDetailContent
import ir.zarin.faktor.ui.invoices.InvoicesContent
import ir.zarin.faktor.ui.invoices.InvoicesUiState
import ir.zarin.faktor.ui.navigation.MainScaffold
import ir.zarin.faktor.ui.navigation.Routes
import ir.zarin.faktor.ui.products.ProductsContent
import ir.zarin.faktor.ui.products.ProductsUiState
import ir.zarin.faktor.ui.sale.ItemEditorForm
import ir.zarin.faktor.ui.sale.NewSaleContent
import ir.zarin.faktor.ui.sale.NewSaleUiState
import ir.zarin.faktor.ui.sale.SaleDraft
import ir.zarin.faktor.ui.settings.SettingsContent
import ir.zarin.faktor.ui.theme.ZarinFaktorTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * تصویربرداری از صفحه‌های اصلی برنامه با داده نمونه تا چیدمان راست‌چین فارسی
 * به‌صورت بصری قابل بررسی باشد.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenshotTest {

    private val displayFormat = DisplayFormat(
        unit = TestSamples.settings.currencyUnit,
        persianDigits = TestSamples.settings.persianDigits,
    )

    @Test
    fun dashboard() = capture("01_dashboard") {
        MainScaffold(
            title = "زرین‌فاکتور",
            selectedRoute = Routes.DASHBOARD,
            onTabSelected = {},
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                    text = { Text("فاکتور جدید") },
                )
            },
        ) { modifier ->
            DashboardContent(
                state = DashboardUiState(
                    settings = TestSamples.settings,
                    todaySalesRial = 3_855_512_320,
                    monthSalesRial = 41_260_000_000,
                    todayInvoiceCount = 3,
                    outstandingRial = 9_800_000_000,
                    recentInvoices = TestSamples.recentInvoices,
                    todayMillis = TestSamples.invoiceDateMillis,
                ),
                onNewSale = {},
                onOpenProducts = {},
                onOpenCustomers = {},
                onOpenInvoices = {},
                onOpenSettings = {},
                onOpenInvoice = {},
                onGoldRateChange = {},
                modifier = modifier,
            )
        }
    }

    @Test
    fun products() = capture("02_products") {
        MainScaffold(
            title = "کالاها",
            selectedRoute = Routes.PRODUCTS,
            onTabSelected = {},
            floatingActionButton = {
                FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, contentDescription = null) }
            },
        ) { modifier ->
            ProductsContent(
                state = ProductsUiState(
                    products = TestSamples.products,
                    pricingContext = TestSamples.settings.pricingContext,
                ),
                onQueryChange = {},
                onEditProduct = {},
                onDeleteProduct = {},
                modifier = modifier,
            )
        }
    }

    @Test
    fun newSale() = capture("03_new_sale") {
        Scaffold(topBar = { ZarinTopBar(title = "فاکتور فروش جدید", onBack = {}) }) { padding ->
            NewSaleContent(
                state = NewSaleUiState(
                    draft = SaleDraft(
                        customer = TestSamples.customer,
                        lines = TestSamples.saleLines,
                        invoiceDiscountRial = 2_000_000,
                        paymentMethod = PaymentMethod.CARD,
                        fullyPaid = true,
                    ),
                    settings = TestSamples.settings,
                    products = TestSamples.products,
                    customers = TestSamples.customers,
                ),
                onSelectCustomer = {},
                onAddCustomer = {},
                onAddLine = {},
                onUpdateLine = {},
                onRemoveLine = {},
                onInvoiceDiscountChange = {},
                onFullyPaidChange = {},
                onPaidAmountChange = {},
                onPaymentMethodChange = {},
                onNoteChange = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h1500dp-xhdpi")
    fun itemEditor() = capture("04_item_editor") {
        ItemEditorForm(
            initial = TestSamples.saleLines[1],
            pricingContext = TestSamples.settings.pricingContext,
            products = TestSamples.products,
            onConfirm = {},
            maxHeight = 1500.dp,
        )
    }

    @Test
    fun invoices() = capture("05_invoices") {
        MainScaffold(
            title = "فاکتورها",
            selectedRoute = Routes.INVOICES,
            onTabSelected = {},
        ) { modifier ->
            InvoicesContent(
                state = InvoicesUiState(invoices = TestSamples.recentInvoices),
                onQueryChange = {},
                onOpenInvoice = {},
                modifier = modifier,
            )
        }
    }

    @Test
    fun invoiceDetail() = capture("06_invoice_detail") {
        Scaffold(topBar = { ZarinTopBar(title = "فاکتور فروش", onBack = {}) }) { padding ->
            InvoiceDetailContent(
                data = TestSamples.invoice(paidRatio = 0.5),
                settings = TestSamples.settings,
                onMarkPaid = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    @Test
    fun customers() = capture("07_customers") {
        Scaffold(topBar = { ZarinTopBar(title = "مشتریان", onBack = {}) }) { padding ->
            CustomersContent(
                state = CustomersUiState(customers = TestSamples.customers),
                onQueryChange = {},
                onEditCustomer = {},
                onDeleteCustomer = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    @Test
    fun settings() = capture("08_settings") {
        MainScaffold(
            title = "تنظیمات",
            selectedRoute = Routes.SETTINGS,
            onTabSelected = {},
        ) { modifier ->
            SettingsContent(initial = TestSamples.settings, onSave = {}, modifier = modifier)
        }
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        captureRoboImage(filePath = "build/outputs/screenshots/$name.png") {
            ZarinFaktorTheme(darkTheme = false) {
                CompositionLocalProvider(LocalDisplayFormat provides displayFormat) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
