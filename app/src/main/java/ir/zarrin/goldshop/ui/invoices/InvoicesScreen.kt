package ir.zarrin.goldshop.ui.invoices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.InvoiceWithItems
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.domain.InvoiceStatus
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.ChipRow
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.SearchField
import ir.zarrin.goldshop.ui.components.StatCard
import ir.zarrin.goldshop.ui.components.StatusPill
import ir.zarrin.goldshop.ui.theme.SuccessGreen
import ir.zarrin.goldshop.ui.theme.WarningAmber
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toJalaliDateLabel
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: InvoicesViewModel = viewModel {
        InvoicesViewModel(container.invoiceRepository, container.settingsRepository)
    }
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("فاکتورهای فروش") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewInvoice,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "فاکتور جدید")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "جستجوی شماره فاکتور یا نام مشتری"
                )
                Spacer(Modifier.height(10.dp))
                ChipRow(
                    items = InvoiceStatus.entries.toList(),
                    selected = state.status,
                    labelOf = { it.label },
                    onSelect = viewModel::onStatusChange
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "جمع فاکتورها",
                        value = state.settings.money(state.totalAmount),
                        subtitle = "${state.invoices.size.toPersianDigits()} فاکتور",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "مانده دریافتی",
                        value = state.settings.money(state.unpaidAmount),
                        subtitle = "پرداخت نشده",
                        accent = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.invoices.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Description,
                    title = "فاکتوری یافت نشد",
                    message = "برای صدور فاکتور جدید روی دکمه + بزنید."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.invoices, key = { it.invoice.id }) { data ->
                        InvoiceRow(
                            data = data,
                            settings = state.settings,
                            onClick = { onOpenInvoice(data.invoice.id) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(
    data: InvoiceWithItems,
    settings: AppSettings,
    onClick: () -> Unit
) {
    val invoice = data.invoice
    val status = InvoiceStatus.fromName(invoice.status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(invoice.customerName, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "فاکتور ${invoice.number.toPersianDigits()} • ${invoice.dateMillis.toJalaliDateLabel()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    StatusPill(
                        text = status.label,
                        color = when (status) {
                            InvoiceStatus.PAID -> SuccessGreen
                            InvoiceStatus.PARTIAL -> WarningAmber
                            InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    settings.money(invoice.grandTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    buildString {
                        append(data.items.sumOf { it.quantity }.toPersianDigits())
                        append(" قلم")
                        if (invoice.totalWeight > 0) {
                            append(" • ")
                            append(invoice.totalWeight.formatWeight())
                            append(" گرم")
                        }
                        append(" • ")
                        append(PaymentMethod.fromName(invoice.paymentMethod).label)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
