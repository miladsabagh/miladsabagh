@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.export.InvoiceDocument
import ir.zarrin.goldshop.export.InvoiceSharing
import ir.zarrin.goldshop.ui.components.KeyValueRow
import ir.zarrin.goldshop.ui.formatCount
import ir.zarrin.goldshop.ui.formatGrams
import ir.zarrin.goldshop.ui.formatMoney
import ir.zarrin.goldshop.ui.theme.ZarrinColors
import ir.zarrin.goldshop.ui.viewmodel.InvoiceDetailViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun InvoiceDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: InvoiceDetailViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }

    val invoice = state.invoice
    val document = invoice?.let {
        InvoiceDocument(
            invoice = it,
            items = state.items,
            lineTotals = state.lineTotals,
            totals = state.totals,
            settings = state.settings
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("پیش‌نمایش فاکتور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (invoice != null) {
                        IconButton(onClick = { onEdit(invoice.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        bottomBar = {
            if (document != null) {
                Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedIconButton(
                            onClick = { InvoiceSharing.shareText(context, document) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Message,
                                contentDescription = "ارسال متن فاکتور",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        OutlinedIconButton(
                            onClick = {
                                scope.launch {
                                    runCatching { InvoiceSharing.print(context, document) }
                                        .onFailure { snackbarHostState.showSnackbar("چاپ در این دستگاه در دسترس نیست.") }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Print, contentDescription = "چاپ", modifier = Modifier.size(20.dp))
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    runCatching { InvoiceSharing.sharePdf(context, document) }
                                        .onFailure { snackbarHostState.showSnackbar("ساخت فایل PDF ناموفق بود.") }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("خروجی PDF", maxLines = 1)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.loading -> Box(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            document == null -> Box(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text("فاکتور یافت نشد.") }

            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp)
            ) {
                item { InvoicePaper(document) }
            }
        }
    }

    if (confirmDelete && invoice != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف فاکتور") },
            text = { Text("فاکتور شماره ${PersianNumbers.toPersianDigits(invoice.number)} حذف شود؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                Button(onClick = {
                    confirmDelete = false
                    viewModel.delete(onDeleted)
                }) { Text("حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun InvoicePaper(document: InvoiceDocument) {
    val invoice = document.invoice
    val currency = document.settings.currency
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        document.settings.shopName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (document.settings.phone.isNotBlank()) {
                        Text(
                            "تلفن: ${PersianNumbers.toPersianDigits(document.settings.phone)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (document.settings.address.isNotBlank()) {
                        Text(
                            document.settings.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "شماره: ${document.numberLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "تاریخ: ${PersianCalendar.fromEpochMillis(invoice.dateMillis).formatNumeric()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            Text("مشخصات خریدار", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(invoice.customerName.ifBlank { "مشتری متفرقه" }, style = MaterialTheme.typography.bodyMedium)
            if (invoice.customerPhone.isNotBlank()) {
                Text(
                    "تلفن: ${PersianNumbers.toPersianDigits(invoice.customerPhone)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (invoice.customerAddress.isNotBlank()) {
                Text(
                    invoice.customerAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "نرخ هر گرم طلای ۱۸ عیار: ${formatMoney(invoice.baseGoldRate, currency)} • " +
                    "پرداخت: ${invoice.paymentMethod.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))
            Text("اقلام فاکتور", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            document.items.forEachIndexed { index, item ->
                val line = document.lineTotals.getOrNull(index)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(formatCount(index + 1), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.title.ifBlank { item.kind.label },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            buildString {
                                if (item.weightGrams > 0.0) {
                                    append(formatGrams(item.weightGrams))
                                    append(" • عیار ")
                                    append(PersianNumbers.toPersianDigits(item.karat.toString()))
                                    append(" • ")
                                }
                                append("تعداد ${formatCount(item.quantity)}")
                                if (line != null && line.wage > 0L) {
                                    append(" • اجرت ${formatMoney(line.wage, currency)}")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        formatMoney(line?.total ?: 0L, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(Modifier.height(12.dp))
            KeyValueRow("جمع ارزش طلا", formatMoney(document.totals.goldValue, currency))
            KeyValueRow("جمع اجرت ساخت", formatMoney(document.totals.wage, currency))
            KeyValueRow("جمع سود فروشنده", formatMoney(document.totals.profit, currency))
            if (document.totals.stone > 0L) {
                KeyValueRow("نگین و سنگ", formatMoney(document.totals.stone, currency))
            }
            KeyValueRow("مالیات بر ارزش افزوده", formatMoney(document.totals.tax, currency))
            if (document.totals.discount > 0L) {
                KeyValueRow("تخفیف", "− ${formatMoney(document.totals.discount, currency)}")
            }
            KeyValueRow("وزن کل", formatGrams(document.totals.totalWeightGrams))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            KeyValueRow(
                label = "مبلغ قابل پرداخت",
                value = formatMoney(document.totals.payable, currency),
                emphasize = true,
                valueColor = MaterialTheme.colorScheme.primary
            )
            KeyValueRow("پرداخت‌شده", formatMoney(document.totals.paid, currency))
            KeyValueRow(
                label = if (document.totals.remaining > 0L) "مانده بدهی" else "وضعیت",
                value = if (document.totals.remaining > 0L) {
                    formatMoney(document.totals.remaining, currency)
                } else {
                    "تسویه کامل"
                },
                emphasize = true,
                valueColor = if (document.totals.remaining > 0L) ZarrinColors.Danger else ZarrinColors.Success
            )

            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "مبلغ به حروف: ${document.amountInWords()}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (invoice.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "توضیحات: ${invoice.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (document.settings.invoiceFooter.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    document.settings.invoiceFooter,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
