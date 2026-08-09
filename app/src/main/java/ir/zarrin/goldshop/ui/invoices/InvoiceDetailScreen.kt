package ir.zarrin.goldshop.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.InvoiceItemEntity
import ir.zarrin.goldshop.data.db.InvoiceWithItems
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.domain.InvoiceStatus
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.pdf.InvoiceOutput
import ir.zarrin.goldshop.pdf.InvoicePdfGenerator
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.LabeledRow
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.StatusPill
import ir.zarrin.goldshop.ui.components.asAmount
import ir.zarrin.goldshop.ui.theme.SuccessGreen
import ir.zarrin.goldshop.ui.theme.WarningAmber
import ir.zarrin.goldshop.util.NumberToPersianWords
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toJalaliDateTimeLabel
import ir.zarrin.goldshop.util.toPersianDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: InvoiceDetailViewModel = viewModel {
        InvoiceDetailViewModel(container.invoiceRepository, container.settingsRepository, invoiceId)
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val data = state.invoice
    val settings = state.settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
        topBar = {
            TopAppBar(
                title = { Text("جزئیات فاکتور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        enabled = data != null,
                        onClick = {
                            data?.let { invoice ->
                                scope.launch {
                                    runCatching {
                                        val file = withContext(Dispatchers.IO) {
                                            InvoicePdfGenerator.generate(context, invoice, settings)
                                        }
                                        InvoiceOutput.print(context, file, invoice.invoice.number)
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("چاپ ناموفق بود")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "چاپ")
                    }
                    IconButton(
                        enabled = data != null,
                        onClick = {
                            data?.let { invoice ->
                                scope.launch {
                                    runCatching {
                                        val file = withContext(Dispatchers.IO) {
                                            InvoicePdfGenerator.generate(context, invoice, settings)
                                        }
                                        InvoiceOutput.share(context, file, invoice.invoice.number)
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("ساخت فایل PDF ناموفق بود")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری PDF")
                    }
                    IconButton(enabled = data != null, onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (data == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("فاکتور یافت نشد", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        val invoice = data.invoice
        val status = InvoiceStatus.fromName(invoice.status)
        val remaining = (invoice.grandTotal - invoice.paidAmount).coerceAtLeast(0L)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    settings.shopName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "فاکتور فروش طلا و جواهر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            StatusPill(
                                text = status.label,
                                color = when (status) {
                                    InvoiceStatus.PAID -> SuccessGreen
                                    InvoiceStatus.PARTIAL -> WarningAmber
                                    InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LabeledRow("شماره فاکتور", invoice.number.toPersianDigits())
                        LabeledRow("تاریخ صدور", invoice.dateMillis.toJalaliDateTimeLabel())
                        LabeledRow("خریدار", invoice.customerName)
                        if (invoice.customerPhone.isNotBlank()) {
                            LabeledRow("تلفن", invoice.customerPhone.toPersianDigits())
                        }
                        LabeledRow("نرخ طلای ۱۸ عیار", settings.money(invoice.goldRate18))
                        LabeledRow(
                            "شیوه پرداخت",
                            PaymentMethod.fromName(invoice.paymentMethod).label
                        )
                    }
                }
            }

            item {
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium)
            }

            items(data.items, key = { it.id }) { item ->
                InvoiceItemCard(item = item, settings = settings)
            }

            item {
                SectionCard(title = "جمع کل") {
                    Column {
                        LabeledRow("بهای طلا", settings.money(invoice.totalGoldValue))
                        LabeledRow("اجرت ساخت", settings.money(invoice.totalWage))
                        LabeledRow("سود فروشنده", settings.money(invoice.totalProfit))
                        if (invoice.totalStone > 0) {
                            LabeledRow("بهای نگین و سنگ", settings.money(invoice.totalStone))
                        }
                        LabeledRow(
                            "مالیات بر ارزش افزوده (${invoice.taxPercent.toInt().toPersianDigits()}٪)",
                            settings.money(invoice.totalTax)
                        )
                        if (invoice.itemsDiscount + invoice.invoiceDiscount > 0) {
                            LabeledRow(
                                "تخفیف",
                                settings.money(invoice.itemsDiscount + invoice.invoiceDiscount)
                            )
                        }
                        if (invoice.totalWeight > 0) {
                            LabeledRow("جمع وزن", "${invoice.totalWeight.formatWeight()} گرم")
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        LabeledRow(
                            "مبلغ قابل پرداخت",
                            settings.money(invoice.grandTotal),
                            emphasize = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        LabeledRow("پرداخت شده", settings.money(invoice.paidAmount))
                        LabeledRow(
                            "مانده",
                            settings.money(remaining),
                            valueColor = if (remaining > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                SuccessGreen
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "مبلغ به حروف: ${
                                NumberToPersianWords.amountToWords(
                                    settings.display(invoice.grandTotal),
                                    settings.currencyLabel
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        if (invoice.note.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "توضیحات: ${invoice.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            data.let { invoiceData ->
                                scope.launch {
                                    runCatching {
                                        val file = withContext(Dispatchers.IO) {
                                            InvoicePdfGenerator.generate(context, invoiceData, settings)
                                        }
                                        InvoiceOutput.share(context, file, invoiceData.invoice.number)
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("ساخت فایل PDF ناموفق بود")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.height(4.dp))
                        Text("ارسال فاکتور PDF")
                    }
                    if (remaining > 0) {
                        OutlinedButton(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("ثبت پرداخت") }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        if (showPaymentDialog) {
            var paidText by remember {
                mutableStateOf(settings.display(invoice.grandTotal).toString())
            }
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = { Text("ثبت پرداخت") },
                text = {
                    Column {
                        Text(
                            "مانده فعلی: ${settings.money(remaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        AmountField(
                            value = paidText,
                            onValueChange = { paidText = it },
                            label = "مجموع مبلغ پرداخت شده",
                            suffix = settings.currencyLabel
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.registerPayment(
                            invoice.id,
                            paidText.asAmount() / settings.currency.multiplier,
                            invoice.grandTotal
                        )
                        showPaymentDialog = false
                    }) { Text("ذخیره") }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentDialog = false }) { Text("انصراف") }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("حذف فاکتور") },
                text = { Text("با حذف فاکتور، کالاهای آن به موجودی انبار بازگردانده می‌شود.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.delete(invoice.id, onBack)
                    }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("انصراف") }
                }
            )
        }
    }
}

@Composable
private fun InvoiceItemCard(item: InvoiceItemEntity, settings: AppSettings) {
    val byWeight = PricingMode.fromName(item.pricingMode) == PricingMode.BY_WEIGHT
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    settings.money(item.lineTotal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            if (byWeight) {
                LabeledRow(
                    "وزن و عیار",
                    "${item.weightGrams.formatWeight()} گرم • ${item.karat.toPersianDigits()} عیار"
                )
                LabeledRow("نرخ هر گرم", settings.money(item.ratePerGram))
                LabeledRow("بهای طلا", settings.money(item.goldValue))
                LabeledRow("اجرت ساخت", settings.money(item.wage))
                LabeledRow("سود فروشنده", settings.money(item.profit))
                if (item.stonePrice > 0) LabeledRow("بهای نگین", settings.money(item.stonePrice))
            } else {
                LabeledRow("قیمت مقطوع", settings.money(item.fixedPrice))
            }
            LabeledRow("مالیات", settings.money(item.tax))
            LabeledRow("تعداد", item.quantity.toPersianDigits())
            if (item.discount > 0) LabeledRow("تخفیف", settings.money(item.discount))
        }
    }
}
