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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarrin.gold.pdf.InvoicePdfGenerator
import ir.zarrin.gold.ui.AppViewModel
import ir.zarrin.gold.ui.components.ConfirmDialog
import ir.zarrin.gold.ui.components.KeyValueRow
import ir.zarrin.gold.util.JalaliDate
import ir.zarrin.gold.util.PersianFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    viewModel: AppViewModel,
    invoiceId: Long,
    onBack: () -> Unit,
) {
    val invoices by viewModel.invoices.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val data = invoices.firstOrNull { it.invoice.id == invoiceId }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        data?.let { "فاکتور ${PersianFormat.toPersianDigits(it.invoice.number.toString())}" }
                            ?: "فاکتور"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "حذف فاکتور",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (data == null) {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("فاکتور یافت نشد.")
            }
            return@Scaffold
        }
        val inv = data.invoice
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    KeyValueRow("خریدار", inv.customerName)
                    if (inv.customerPhone.isNotBlank()) {
                        KeyValueRow("تلفن", PersianFormat.toPersianDigits(inv.customerPhone))
                    }
                    KeyValueRow("تاریخ", JalaliDate.formatLong(inv.date))
                    KeyValueRow(
                        "مبنای محاسبه (گرم ۱۸ عیار)",
                        PersianFormat.formatCurrency(inv.goldPricePerGram18k),
                    )
                    KeyValueRow(
                        "وضعیت",
                        if (inv.paid) "پرداخت شده" else "پرداخت نشده",
                    )
                }
            }

            Text("اقلام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            data.items.forEach { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "عیار ${PersianFormat.toPersianDigits(item.karat.toString())} • " +
                                PersianFormat.formatWeight(item.weightGrams) +
                                " • تعداد ${PersianFormat.formatNumber(item.quantity.toLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        KeyValueRow("بهای طلا", PersianFormat.formatCurrency(item.goldValue))
                        KeyValueRow("اجرت ساخت", PersianFormat.formatCurrency(item.wage))
                        KeyValueRow("سود", PersianFormat.formatCurrency(item.profit))
                        KeyValueRow("مالیات", PersianFormat.formatCurrency(item.tax))
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        KeyValueRow("جمع قلم", PersianFormat.formatCurrency(item.lineTotal), emphasize = true)
                    }
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(Modifier.padding(12.dp)) {
                    KeyValueRow("جمع بهای طلا", PersianFormat.formatCurrency(inv.goldValue))
                    KeyValueRow("جمع اجرت", PersianFormat.formatCurrency(inv.wage))
                    KeyValueRow("سود فروشنده", PersianFormat.formatCurrency(inv.profit))
                    KeyValueRow("مالیات", PersianFormat.formatCurrency(inv.tax))
                    if (inv.discount > 0) {
                        KeyValueRow("تخفیف", "− " + PersianFormat.formatCurrency(inv.discount))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    KeyValueRow("مبلغ قابل پرداخت", PersianFormat.formatCurrency(inv.total), emphasize = true)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val file = InvoicePdfGenerator.generate(context, data, settings)
                            launch(Dispatchers.Main) {
                                InvoicePdfGenerator.share(context, file)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Text("  خروجی PDF")
                }
                FilledTonalButton(
                    onClick = { viewModel.setInvoicePaid(inv.id, !inv.paid) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (inv.paid) "علامت: پرداخت نشده" else "ثبت پرداخت")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف فاکتور",
            message = "این فاکتور برای همیشه حذف می‌شود. ادامه می‌دهید؟",
            onConfirm = {
                confirmDelete = false
                viewModel.deleteInvoice(invoiceId)
                onBack()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
