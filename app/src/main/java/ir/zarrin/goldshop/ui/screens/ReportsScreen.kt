@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.KeyValueRow
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.StatTile
import ir.zarrin.goldshop.ui.formatCount
import ir.zarrin.goldshop.ui.formatMoney
import ir.zarrin.goldshop.ui.theme.ZarrinColors
import ir.zarrin.goldshop.ui.viewmodel.ReportRange
import ir.zarrin.goldshop.ui.viewmodel.ReportsViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    viewModel: ReportsViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currency = state.settings.currency

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارش فروش") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRange.entries.forEach { range ->
                        FilterChip(
                            selected = state.range == range,
                            onClick = { viewModel.onRangeChange(range) },
                            label = { Text(range.label) }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        title = "فروش",
                        value = formatMoney(state.salesTotal, currency),
                        caption = "${formatCount(state.invoiceCount)} فاکتور",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        container = ZarrinColors.SuccessSoft,
                        contentColor = ZarrinColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "دریافتی",
                        value = formatMoney(state.receivedTotal, currency),
                        caption = "وجه نقد و کارت",
                        icon = Icons.Filled.Payments,
                        container = ZarrinColors.InfoSoft,
                        contentColor = ZarrinColors.Info,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        title = "مانده طلب",
                        value = formatMoney(state.outstanding, currency),
                        caption = "دریافت‌نشده",
                        icon = Icons.Filled.HourglassBottom,
                        container = ZarrinColors.DangerSoft,
                        contentColor = ZarrinColors.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "خرید",
                        value = formatMoney(state.purchaseTotal, currency),
                        caption = "فاکتورهای خرید",
                        icon = Icons.Filled.BarChart,
                        container = ZarrinColors.GoldSoft,
                        contentColor = ZarrinColors.Gold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (state.topCustomers.isNotEmpty()) {
                item {
                    SectionCard(title = "مشتریان برتر", icon = Icons.Filled.BarChart) {
                        Column {
                            state.topCustomers.forEach { (name, total) ->
                                KeyValueRow(name, formatMoney(total, currency))
                            }
                        }
                    }
                }
            }
            item {
                Text("فاکتورهای این بازه", style = MaterialTheme.typography.titleMedium)
            }
            if (state.invoices.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "فاکتوری در این بازه نیست",
                        subtitle = "بازه دیگری را انتخاب کنید."
                    )
                }
            } else {
                items(state.invoices, key = { it.id }) { invoice ->
                    InvoiceRow(
                        invoice = invoice,
                        currency = currency,
                        onClick = { onOpenInvoice(invoice.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
