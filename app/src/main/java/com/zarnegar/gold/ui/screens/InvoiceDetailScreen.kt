package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.Karats
import com.zarnegar.gold.pdf.InvoiceSharing
import com.zarnegar.gold.ui.components.AmountField
import com.zarnegar.gold.ui.components.KeyValueRow
import com.zarnegar.gold.ui.components.SectionCard
import com.zarnegar.gold.ui.components.StatusChip
import com.zarnegar.gold.ui.vm.InvoicesViewModel
import com.zarnegar.gold.ui.vm.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoicesViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId, reloadKey) {
        invoice = viewModel.load(invoiceId)
    }

    val current = invoice ?: return
    val currency = settings.currencyLabel

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("فاکتور ${PersianText.toPersianDigits(current.number)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            androidx.compose.material3.Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { InvoiceSharing.share(context, current, settings) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text("  اشتراک PDF")
                    }
                    OutlinedButton(
                        onClick = { InvoiceSharing.print(context, current, settings) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null)
                        Text("  چاپ")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "فاکتور فروش کالا و خدمات",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusChip(
                            text = current.status.label,
                            container = when (current.status) {
                                InvoiceStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
                                InvoiceStatus.PARTIAL -> MaterialTheme.colorScheme.primaryContainer
                                InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer
                            },
                            content = when (current.status) {
                                InvoiceStatus.PAID -> MaterialTheme.colorScheme.onSecondaryContainer
                                InvoiceStatus.PARTIAL ->
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.onErrorContainer
                            },
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    KeyValueRow("شمارهٔ فاکتور", PersianText.toPersianDigits(current.number))
                    KeyValueRow(
                        "تاریخ صدور",
                        JalaliDate.fromEpochMillis(current.createdAt).formatLong() +
                            " ساعت " + JalaliDate.formatTime(current.createdAt),
                    )
                    KeyValueRow("خریدار", current.customerName)
                    if (current.customerPhone.isNotBlank()) {
                        KeyValueRow(
                            "تلفن",
                            PersianText.toPersianDigits(current.customerPhone),
                        )
                    }
                    KeyValueRow(
                        "نرخ پایهٔ طلای ۱۸ عیار",
                        "${PersianText.formatNumber(current.goldRateSnapshot)} $currency",
                    )
                }
            }

            item {
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium)
            }

            itemsIndexed(current.lines) { index, line ->
                InvoiceLineCard(index = index, line = line, currency = currency)
            }

            item {
                SectionCard(title = "جمع‌بندی") {
                    KeyValueRow("وزن کل", PersianText.formatGrams(current.totalWeightGrams))
                    KeyValueRow(
                        "جمع پیش از مالیات",
                        "${PersianText.formatNumber(current.grossBeforeTax)} $currency",
                    )
                    val discount = current.itemDiscountTotal + current.invoiceDiscount
                    if (discount > 0) {
                        KeyValueRow(
                            "تخفیف",
                            "${PersianText.formatNumber(discount)} $currency",
                            valueColor = MaterialTheme.colorScheme.error,
                        )
                    }
                    KeyValueRow(
                        "مالیات بر ارزش افزوده " +
                            "(${PersianText.formatPercent(current.vatPercentSnapshot)})",
                        "${PersianText.formatNumber(current.vatTotal)} $currency",
                    )
                    if (current.roundingAdjustment != 0L) {
                        KeyValueRow(
                            "گرد کردن",
                            "${PersianText.formatNumber(current.roundingAdjustment)} $currency",
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    KeyValueRow(
                        "مبلغ قابل پرداخت",
                        "${PersianText.formatNumber(current.payable)} $currency",
                        emphasize = true,
                        valueColor = MaterialTheme.colorScheme.primary,
                    )
                    KeyValueRow(
                        "پرداخت‌شده (${current.paymentMethod.label})",
                        "${PersianText.formatNumber(current.paidAmount)} $currency",
                    )
                    if (current.remaining > 0) {
                        KeyValueRow(
                            "مانده",
                            "${PersianText.formatNumber(current.remaining)} $currency",
                            emphasize = true,
                            valueColor = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "به حروف: ${PersianText.numberToWords(current.payable)} $currency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (current.note.isNotBlank()) {
                        Text(
                            text = "توضیحات: ${current.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (current.remaining > 0) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.Payments, contentDescription = null)
                            Text("  ثبت پرداخت")
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        var amount by remember { mutableStateOf(current.remaining) }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("ثبت پرداخت") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "مانده: ${PersianText.formatNumber(current.remaining)} $currency",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    AmountField(
                        label = "مبلغ دریافتی",
                        value = amount,
                        onValueChange = { amount = it },
                        suffix = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.registerPayment(current, amount) {
                        reloadKey++
                    }
                    showPaymentDialog = false
                    scope.launch { snackbarHostState.showSnackbar("پرداخت ثبت شد") }
                }) { Text("ثبت") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPaymentDialog = false }) { Text("انصراف") }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف فاکتور") },
            text = { Text("این فاکتور برای همیشه حذف شود؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(current)
                    confirmDelete = false
                    onBack()
                }) { Text("حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun InvoiceLineCard(index: Int, line: InvoiceLine, currency: String) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${PersianText.formatNumber((index + 1).toLong())}. ${line.title}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${PersianText.formatNumber(line.total)} $currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (line.weightGrams > 0) {
            KeyValueRow(
                "وزن × تعداد",
                "${PersianText.formatGrams(line.weightGrams)} × " +
                    PersianText.formatNumber(line.quantity.toLong()),
            )
            KeyValueRow("عیار", Karats.label(line.karat))
            KeyValueRow(
                "نرخ هر گرم",
                "${PersianText.formatNumber(line.ratePerGram)} $currency",
            )
            KeyValueRow("ارزش طلا", "${PersianText.formatNumber(line.goldValue)} $currency")
            KeyValueRow("اجرت ساخت", "${PersianText.formatNumber(line.wage)} $currency")
            KeyValueRow("سود فروشنده", "${PersianText.formatNumber(line.profit)} $currency")
        } else {
            KeyValueRow("تعداد", PersianText.formatNumber(line.quantity.toLong()))
        }
        if (line.stoneValue > 0) {
            KeyValueRow("ارزش سنگ", "${PersianText.formatNumber(line.stoneValue)} $currency")
        }
        if (line.discount > 0) {
            KeyValueRow(
                "تخفیف",
                "${PersianText.formatNumber(line.discount)} $currency",
                valueColor = MaterialTheme.colorScheme.error,
            )
        }
        KeyValueRow("مالیات بر ارزش افزوده", "${PersianText.formatNumber(line.vat)} $currency")
    }
}
