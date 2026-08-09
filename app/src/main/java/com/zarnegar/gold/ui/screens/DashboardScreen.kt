package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.ui.components.KeyValueRow
import com.zarnegar.gold.ui.components.MoneyText
import com.zarnegar.gold.ui.components.SectionCard
import com.zarnegar.gold.ui.components.StatCard
import com.zarnegar.gold.ui.vm.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNewSale: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingRate by remember { mutableStateOf(false) }
    val currency = state.settings.currencyLabel
    val today = JalaliDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.settings.shopName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${JalaliDate.weekDayName(System.currentTimeMillis())}، " +
                                today.formatLong(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GoldRateCard(
                    rate = state.settings.goldRatePerGram18k,
                    currency = currency,
                    onEdit = { editingRate = true },
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "فروش امروز",
                        value = "${PersianText.formatNumber(state.todaySales)} $currency",
                        icon = Icons.Filled.Payments,
                        modifier = Modifier.weight(1f),
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    StatCard(
                        title = "فاکتورهای امروز",
                        value = PersianText.formatNumber(state.todayInvoices.size.toLong()),
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "وزن فروش امروز",
                        value = PersianText.formatGrams(state.todayWeight),
                        icon = Icons.Filled.Scale,
                        modifier = Modifier.weight(1f),
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    StatCard(
                        title = "مانده مطالبات",
                        value = "${PersianText.formatNumber(state.totalReceivable)} $currency",
                        icon = Icons.Filled.Warning,
                        modifier = Modifier.weight(1f),
                        container = if (state.totalReceivable > 0) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        onContainer = if (state.totalReceivable > 0) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            item {
                SectionCard(title = "دسترسی سریع") {
                    Button(
                        onClick = onNewSale,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("  صدور فاکتور جدید")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onOpenProducts,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.Diamond, contentDescription = null)
                            Text("  کالاها")
                        }
                        OutlinedButton(
                            onClick = onOpenInvoices,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                            Text("  فاکتورها")
                        }
                    }
                }
            }

            item {
                SectionCard(title = "وضعیت انبار") {
                    KeyValueRow(
                        "تعداد کالاها",
                        PersianText.formatNumber(state.products.size.toLong()),
                    )
                    KeyValueRow(
                        "موجودی کل (عدد)",
                        PersianText.formatNumber(state.products.sumOf { it.stock }.toLong()),
                    )
                    KeyValueRow(
                        "مشتریان ثبت‌شده",
                        PersianText.formatNumber(state.customerCount.toLong()),
                    )
                    if (state.lowStock.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "  ${state.lowStock.size} کالا رو به اتمام است",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "آخرین فاکتورها",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (state.recentInvoices.isEmpty()) {
                item {
                    SectionCard {
                        Text(
                            text = "هنوز فاکتوری صادر نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.recentInvoices, key = { it.id }) { invoice ->
                    InvoiceRow(
                        invoice = invoice,
                        currency = currency,
                        onClick = { onOpenInvoice(invoice.id) },
                    )
                }
            }
        }
    }

    if (editingRate) {
        GoldRateDialog(
            initial = state.settings.goldRatePerGram18k,
            currency = currency,
            onDismiss = { editingRate = false },
            onConfirm = {
                viewModel.updateGoldRate(it)
                editingRate = false
            },
        )
    }
}

@Composable
private fun GoldRateCard(rate: Long, currency: String, onEdit: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "نرخ هر گرم طلای ۱۸ عیار",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Text(
                    text = "${PersianText.formatNumber(rate)} $currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "مبنای همهٔ محاسبات فاکتور",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            Icon(
                Icons.Filled.Edit,
                contentDescription = "ویرایش نرخ طلا",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp),
                    )
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun InvoiceRow(
    invoice: com.zarnegar.gold.domain.model.Invoice,
    currency: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(invoice.customerName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "فاکتور ${PersianText.toPersianDigits(invoice.number)} • " +
                        JalaliDate.fromEpochMillis(invoice.createdAt).formatNumeric(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MoneyText(invoice.payable, currency, bold = true)
        }
    }
}

@Composable
private fun GoldRateDialog(
    initial: Long,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("به‌روزرسانی نرخ طلا") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "نرخ هر گرم طلای ۱۸ عیار (خام) را وارد کنید.",
                    style = MaterialTheme.typography.bodySmall,
                )
                com.zarnegar.gold.ui.components.AmountField(
                    label = "نرخ هر گرم",
                    value = value,
                    onValueChange = { value = it },
                    suffix = currency,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }) { Text("ذخیره") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
