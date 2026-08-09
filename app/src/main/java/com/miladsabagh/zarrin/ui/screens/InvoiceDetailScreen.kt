package com.miladsabagh.zarrin.ui.screens

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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miladsabagh.zarrin.data.db.InvoiceWithItems
import com.miladsabagh.zarrin.ui.InvoiceDetailViewModel
import com.miladsabagh.zarrin.ui.components.LabeledAmountRow
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.InvoicePdf
import com.miladsabagh.zarrin.util.formatDateTime
import com.miladsabagh.zarrin.util.formatGram
import com.miladsabagh.zarrin.util.formatInvoiceNumber
import com.miladsabagh.zarrin.util.formatToman
import com.miladsabagh.zarrin.util.toPersianDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceDetailViewModel,
    onBack: () -> Unit
) {
    val entry by viewModel.invoice.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    InvoiceDetailContent(
        entry = entry,
        onBack = onBack,
        onShare = { data ->
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    InvoicePdf.generate(context, data, settings)
                }
                InvoicePdf.share(context, file, data.invoice.invoiceNumber)
            }
        }
    )
}

@Composable
fun InvoiceDetailContent(
    entry: InvoiceWithItems?,
    onBack: () -> Unit,
    onShare: (InvoiceWithItems) -> Unit
) {
    Scaffold(
        topBar = {
            ZarrinTopBar(
                title = entry?.let { "فاکتور ${it.invoice.invoiceNumber.formatInvoiceNumber()}" } ?: "فاکتور",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { entry?.let { onShare(it) } },
                        enabled = entry != null
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "اشتراک‌گذاری PDF")
                    }
                }
            )
        }
    ) { padding ->
        val data = entry
        if (data == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("در حال بارگذاری…")
            }
        } else {
            val invoice = data.invoice
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("خریدار", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(invoice.createdAt.formatDateTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(invoice.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(invoice.customerPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "نرخ روز طلای ۱۸ عیار: ${invoice.goldPricePerGram18k.formatToman()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text("اقلام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                data.items.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "عیار ${item.karat} • ${item.weightGrams.formatGram()} • نرخ هر گرم ${item.goldPricePerGram.formatToman(false)}".toPersianDigits(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            LabeledAmountRow("طلای خام", item.goldValue.formatToman())
                            LabeledAmountRow("اجرت ساخت", item.wageAmount.formatToman())
                            LabeledAmountRow("سود", item.profitAmount.formatToman())
                            LabeledAmountRow("مالیات", item.taxAmount.formatToman())
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            LabeledAmountRow("مبلغ قلم", item.lineTotal.formatToman(), emphasize = true)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LabeledAmountRow("جمع طلای خام", invoice.goldValue.formatToman())
                        LabeledAmountRow("جمع اجرت ساخت", invoice.wageTotal.formatToman())
                        LabeledAmountRow(
                            "سود فروشنده (${invoice.profitPercent.toString().toPersianDigits()}٪)",
                            invoice.profitAmount.formatToman()
                        )
                        LabeledAmountRow(
                            "مالیات ارزش افزوده (${invoice.taxPercent.toString().toPersianDigits()}٪)",
                            invoice.taxAmount.formatToman()
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        LabeledAmountRow("مبلغ قابل پرداخت", invoice.grandTotal.formatToman(), emphasize = true)
                    }
                }

                Text(
                    "برای اشتراک‌گذاری نسخه PDF فاکتور، دکمه اشتراک بالای صفحه را بزنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
