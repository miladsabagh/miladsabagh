package com.zarfam.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.data.db.Invoice
import com.zarfam.goldshop.domain.JalaliDate
import com.zarfam.goldshop.domain.parseMoney
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.domain.toMoneyToman
import com.zarfam.goldshop.domain.toPersianDigits
import com.zarfam.goldshop.ui.components.AppTextField
import com.zarfam.goldshop.ui.components.StatCard
import com.zarfam.goldshop.ui.viewmodel.InvoicesViewModel
import com.zarfam.goldshop.ui.viewmodel.SettingsViewModel

@Composable
fun DashboardScreen(
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onOpenProducts: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    invoicesViewModel: InvoicesViewModel = viewModel(factory = InvoicesViewModel.Factory),
) {
    val settings by settingsViewModel.settings.collectAsState()
    val invoices by invoicesViewModel.invoices.collectAsState()
    val todaySales by invoicesViewModel.todaySales.collectAsState()
    val totalSales by invoicesViewModel.totalSales.collectAsState()
    val productCount by invoicesViewModel.productCount.collectAsState()
    val invoiceCount by invoicesViewModel.invoiceCount.collectAsState()

    var showPriceDialog by remember { mutableStateOf(false) }

    if (showPriceDialog) {
        GoldPriceDialog(
            initial = settings.goldPricePerGram18,
            onDismiss = { showPriceDialog = false },
            onSave = {
                settingsViewModel.saveGoldPrice(it)
                showPriceDialog = false
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "قیمت روز هر گرم طلای ۱۸ عیار",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        IconButton(onClick = { showPriceDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "ویرایش قیمت طلا",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    Text(
                        if (settings.goldPricePerGram18 > 0) settings.goldPricePerGram18.toMoneyToman()
                        else "ثبت نشده — برای ثبت ضربه بزنید",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "فروش امروز",
                    todaySales.toMoneyToman(),
                    Icons.Default.Paid,
                    Modifier.weight(1f),
                )
                StatCard(
                    "کل فروش",
                    totalSales.toMoneyToman(),
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "تعداد محصولات",
                    productCount.toString().toPersianDigits(),
                    Icons.Default.Diamond,
                    Modifier.weight(1f),
                )
                StatCard(
                    "تعداد فاکتورها",
                    invoiceCount.toString().toPersianDigits(),
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNewInvoice,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("فاکتور جدید")
                }
                OutlinedButton(
                    onClick = onOpenProducts,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("محصولات")
                }
            }
        }

        item {
            Text(
                "آخرین فاکتورها",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (invoices.isEmpty()) {
            item {
                Text(
                    "هنوز فاکتوری صادر نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(invoices.take(5), key = { it.id }) { invoice ->
                RecentInvoiceRow(invoice, onClick = { onOpenInvoice(invoice.id) })
            }
        }
    }
}

@Composable
private fun RecentInvoiceRow(invoice: Invoice, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "فاکتور ${invoice.invoiceNumber.toString().toPersianDigits()} — ${invoice.customerName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    JalaliDate.format(invoice.dateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                invoice.grandTotal.toMoney(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun GoldPriceDialog(
    initial: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var text by remember { mutableStateOf(if (initial > 0) initial.toString() else "") }
    val parsed = text.parseMoney()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("قیمت روز طلا") },
        text = {
            Column {
                Text(
                    "قیمت هر گرم طلای ۱۸ عیار را به تومان وارد کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "قیمت (تومان)",
                    numeric = true,
                    supportingText = parsed?.let { "${it.toMoney()} تومان" },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onSave) },
                enabled = parsed != null && parsed > 0,
            ) { Text("ثبت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
