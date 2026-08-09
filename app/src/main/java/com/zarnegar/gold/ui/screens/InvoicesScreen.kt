package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.ui.components.EmptyState
import com.zarnegar.gold.ui.components.StatCard
import com.zarnegar.gold.ui.components.StatusChip
import com.zarnegar.gold.ui.vm.InvoicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    viewModel: InvoicesViewModel,
    onOpenInvoice: (Long) -> Unit,
    onNewSale: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = state.settings.currencyLabel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فاکتورها") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewSale,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("فاکتور جدید") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = "جمع فروش",
                    value = "${PersianText.formatNumber(state.totalSales)} $currency",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "مانده مطالبات",
                    value = "${PersianText.formatNumber(state.totalReceivable)} $currency",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f),
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("جستجوی شماره فاکتور یا نام مشتری") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.status == null,
                    onClick = { viewModel.onStatusChange(null) },
                    label = { Text("همه") },
                )
                InvoiceStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.status == status,
                        onClick = { viewModel.onStatusChange(status) },
                        label = { Text(status.label) },
                    )
                }
            }

            val invoices = state.filtered
            if (invoices.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "فاکتوری یافت نشد",
                    message = "با دکمهٔ «فاکتور جدید» اولین فروش خود را ثبت کنید.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(invoices, key = { it.id }) { invoice ->
                        InvoiceListCard(
                            invoice = invoice,
                            currency = currency,
                            onClick = { onOpenInvoice(invoice.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceListCard(invoice: Invoice, currency: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "شماره ${PersianText.formatCode(invoice.number)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    text = invoice.status.label,
                    container = when (invoice.status) {
                        InvoiceStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
                        InvoiceStatus.PARTIAL -> MaterialTheme.colorScheme.primaryContainer
                        InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer
                    },
                    content = when (invoice.status) {
                        InvoiceStatus.PAID -> MaterialTheme.colorScheme.onSecondaryContainer
                        InvoiceStatus.PARTIAL -> MaterialTheme.colorScheme.onPrimaryContainer
                        InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.onErrorContainer
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = JalaliDate.fromEpochMillis(invoice.createdAt).formatLong() +
                        " • ساعت " + JalaliDate.formatTime(invoice.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${PersianText.formatNumber(invoice.payable)} $currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
