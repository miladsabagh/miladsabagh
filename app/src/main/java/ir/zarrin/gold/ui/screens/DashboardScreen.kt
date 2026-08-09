package ir.zarrin.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.zarrin.gold.ui.AppViewModel
import ir.zarrin.gold.ui.components.AppTextField
import ir.zarrin.gold.ui.components.KeyValueRow
import ir.zarrin.gold.util.JalaliDate
import ir.zarrin.gold.util.PersianFormat
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNewInvoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val products by viewModel.products.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    var showPriceDialog by remember { mutableStateOf(false) }

    val startOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayInvoices = invoices.filter { it.invoice.date >= startOfDay }
    val todaySales = todayInvoices.sumOf { it.invoice.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(settings.storeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    JalaliDate.formatLong(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
            }
        }

        // کارت قیمت روز طلا
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "قیمت روز طلای ۱۸ عیار (هر گرم)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    IconButton(onClick = { showPriceDialog = true }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "ویرایش قیمت",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Text(
                    if (settings.goldPricePerGram18k > 0) {
                        PersianFormat.formatCurrency(settings.goldPricePerGram18k)
                    } else {
                        "تعیین نشده — برای محاسبه قیمت‌ها وارد کنید"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // آمار امروز
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("فروش امروز", PersianFormat.formatCurrency(todaySales), Modifier.weight(1f))
            StatCard("فاکتور امروز", PersianFormat.formatNumber(todayInvoices.size.toLong()), Modifier.weight(1f))
            StatCard("محصولات", PersianFormat.formatNumber(products.size.toLong()), Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onNewInvoice, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  فاکتور جدید")
            }
            FilledTonalButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                Text("تنظیمات فروشگاه")
            }
        }

        Text("آخرین فاکتورها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (invoices.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "هنوز فاکتوری صادر نشده است",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            invoices.take(5).forEach { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenInvoice(inv.invoice.id) },
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        KeyValueRow(
                            label = "فاکتور ${PersianFormat.toPersianDigits(inv.invoice.number.toString())} — ${inv.invoice.customerName}",
                            value = PersianFormat.formatCurrency(inv.invoice.total),
                        )
                        Text(
                            JalaliDate.format(inv.invoice.date) +
                                if (inv.invoice.paid) "  •  پرداخت شده" else "  •  پرداخت نشده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showPriceDialog) {
        GoldPriceDialog(
            current = settings.goldPricePerGram18k,
            onDismiss = { showPriceDialog = false },
            onSave = {
                viewModel.setGoldPrice(it)
                showPriceDialog = false
            },
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun GoldPriceDialog(
    current: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var text by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    val parsed = PersianFormat.parseLong(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("قیمت روز طلای ۱۸ عیار") },
        text = {
            Column {
                AppTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "قیمت هر گرم",
                    numeric = true,
                    suffix = "تومان",
                )
                if (parsed != null && parsed > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        PersianFormat.formatCurrency(parsed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onSave) },
                enabled = parsed != null && parsed > 0,
            ) { Text("ثبت") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
