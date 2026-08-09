package com.miladsabagh.goldinvoice.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.util.formatCurrency
import com.miladsabagh.goldinvoice.util.formatInvoiceDate
import com.miladsabagh.goldinvoice.util.parseLocalizedDouble
import com.miladsabagh.goldinvoice.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNewInvoice: () -> Unit,
    onNewProduct: () -> Unit,
    onNewCustomer: () -> Unit,
    onInvoiceClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showPriceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(state.shopName.ifBlank { stringResource(R.string.app_name) }) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GoldPriceCard(state.goldPricePerGram18k) { showPriceDialog = true } }
            item { StatsRow(state) }
            item { QuickActionsSection(onNewInvoice, onNewProduct, onNewCustomer) }
            item {
                Text(
                    text = stringResource(R.string.dashboard_recent_invoices),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (state.recentInvoices.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.invoices_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.recentInvoices) { invoice ->
                    RecentInvoiceRow(invoice, onClick = { onInvoiceClick(invoice.id) })
                }
            }
        }
    }

    if (showPriceDialog) {
        UpdateGoldPriceDialog(
            currentPrice = state.goldPricePerGram18k,
            onDismiss = { showPriceDialog = false },
            onConfirm = { newPrice ->
                viewModel.updateGoldPrice(newPrice)
                showPriceDialog = false
            }
        )
    }
}

@Composable
private fun GoldPriceCard(price: Double, onUpdateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.dashboard_gold_price_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatCurrency(price),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.unit_toman),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onUpdateClick) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.height(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.dashboard_update_price))
            }
        }
    }
}

@Composable
private fun StatsRow(state: DashboardUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.dashboard_today_sales),
            value = formatCurrency(state.todaySalesTotal) + " " + stringResource(R.string.unit_toman)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.dashboard_today_invoices_count),
            value = state.todayInvoiceCount.toString()
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.dashboard_total_customers),
            value = state.customerCount.toString()
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.dashboard_total_products),
            value = state.productCount.toString()
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNewInvoice: () -> Unit,
    onNewProduct: () -> Unit,
    onNewCustomer: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = stringResource(R.string.dashboard_new_invoice),
                onClick = onNewInvoice
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Add,
                label = stringResource(R.string.dashboard_new_product),
                onClick = onNewProduct
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PersonAddAlt,
                label = stringResource(R.string.dashboard_new_customer),
                onClick = onNewCustomer
            )
        }
    }
}

@Composable
private fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun RecentInvoiceRow(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.invoice_number)} ${invoice.invoiceNumber.toPersianDigits()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = invoice.customerNameSnapshot.ifBlank { stringResource(R.string.customer_walk_in) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatInvoiceDate(invoice.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(invoice.grandTotal) + " " + stringResource(R.string.unit_toman),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UpdateGoldPriceDialog(
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(if (currentPrice > 0) currentPrice.toLong().toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dashboard_update_price)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.invoice_gold_price)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                parseLocalizedDouble(text)?.let(onConfirm)
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
