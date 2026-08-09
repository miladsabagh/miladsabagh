package com.miladsabagh.zarrin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.miladsabagh.zarrin.ui.screens.CustomersContent
import com.miladsabagh.zarrin.ui.screens.DashboardContent
import com.miladsabagh.zarrin.ui.screens.InvoiceDetailContent
import com.miladsabagh.zarrin.ui.screens.InvoicesContent
import com.miladsabagh.zarrin.ui.screens.NewInvoiceContent
import com.miladsabagh.zarrin.ui.screens.ProductsContent
import com.miladsabagh.zarrin.ui.screens.SettingsContent
import com.miladsabagh.zarrin.ui.theme.ZarrinTheme
import org.junit.Rule
import org.junit.Test

class ScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6.copy(locale = "fa"),
        theme = "android:Theme.Material.Light.NoActionBar"
    )

    private fun shot(name: String, content: @Composable () -> Unit) {
        paparazzi.snapshot(name = name) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ZarrinTheme {
                    content()
                }
            }
        }
    }

    @Test
    fun dashboard() = shot("dashboard") {
        DashboardContent(
            settings = SnapshotSamples.settings,
            todaySales = 12_450_000L to 3,
            totalSales = 87_300_000L to 12,
            productCount = 8,
            onNewInvoice = {},
            onOpenSettings = {},
            onGoldPriceSave = {}
        )
    }

    @Test
    fun products() = shot("products") {
        ProductsContent(
            products = SnapshotSamples.products,
            onSave = {},
            onDelete = {}
        )
    }

    @Test
    fun customers() = shot("customers") {
        CustomersContent(
            customers = SnapshotSamples.customers,
            onSave = {},
            onDelete = {}
        )
    }

    @Test
    fun newInvoice() = shot("new_invoice") {
        NewInvoiceContent(
            settings = SnapshotSamples.settings,
            customers = SnapshotSamples.customers,
            products = SnapshotSamples.products,
            selectedCustomer = SnapshotSamples.customers.first(),
            lines = SnapshotSamples.cartLines,
            error = null,
            onSelectCustomer = {},
            onAddProduct = {},
            onAddManual = { _, _, _, _ -> },
            onRemoveLine = {},
            onSaveInvoice = {},
            onErrorShown = {}
        )
    }

    @Test
    fun invoices() = shot("invoices") {
        InvoicesContent(
            invoices = SnapshotSamples.invoices,
            onOpenInvoice = {}
        )
    }

    @Test
    fun invoiceDetail() = shot("invoice_detail") {
        InvoiceDetailContent(
            entry = SnapshotSamples.invoice,
            onBack = {},
            onShare = {}
        )
    }

    @Test
    fun settings() = shot("settings") {
        SettingsContent(
            settings = SnapshotSamples.settings,
            onSave = { _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
