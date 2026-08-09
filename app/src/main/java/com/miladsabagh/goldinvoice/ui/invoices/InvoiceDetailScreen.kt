package com.miladsabagh.goldinvoice.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.entity.InvoiceItem
import com.miladsabagh.goldinvoice.ui.components.SectionCard
import com.miladsabagh.goldinvoice.util.formatCurrency
import com.miladsabagh.goldinvoice.util.formatInvoiceDate
import com.miladsabagh.goldinvoice.util.formatWeight
import com.miladsabagh.goldinvoice.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceDetailViewModel,
    onBack: () -> Unit
) {
    val data by viewModel.invoiceWithItems.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shareAsPdf(context) }) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.invoice_share_pdf))
                    }
                }
            )
        }
    ) { padding ->
        val invoiceData = data
        if (invoiceData == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {}
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Text(
                    text = "${stringResource(R.string.invoice_number)} ${invoiceData.invoice.invoiceNumber.toPersianDigits()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatInvoiceDate(invoiceData.invoice.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = invoiceData.invoice.customerNameSnapshot.ifBlank { stringResource(R.string.customer_walk_in) },
                    style = MaterialTheme.typography.bodyLarge
                )
                if (invoiceData.invoice.customerPhoneSnapshot.isNotBlank()) {
                    Text(
                        text = invoiceData.invoice.customerPhoneSnapshot.toPersianDigits(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionCard(title = stringResource(R.string.invoice_items)) {
                invoiceData.items.forEach { item ->
                    InvoiceItemDetailRow(item)
                    HorizontalDivider()
                }
            }

            SectionCard(title = "جمع‌بندی") {
                DetailTotalRow(stringResource(R.string.invoice_subtotal), invoiceData.invoice.subtotal)
                DetailTotalRow(stringResource(R.string.invoice_discount), invoiceData.invoice.discountAmount)
                DetailTotalRow(stringResource(R.string.invoice_grand_total), invoiceData.invoice.grandTotal, emphasize = true)
                DetailTotalRow(stringResource(R.string.invoice_paid_amount), invoiceData.invoice.paidAmount)
                val remaining = (invoiceData.invoice.grandTotal - invoiceData.invoice.paidAmount).coerceAtLeast(0.0)
                DetailTotalRow(stringResource(R.string.invoice_remaining), remaining)
            }

            if (invoiceData.invoice.notes.isNotBlank()) {
                SectionCard(title = stringResource(R.string.invoice_notes)) {
                    Text(text = invoiceData.invoice.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(onClick = { viewModel.shareAsPdf(context) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text("  " + stringResource(R.string.invoice_share_pdf))
            }
        }
    }
}

@Composable
private fun InvoiceItemDetailRow(item: InvoiceItem) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${formatCurrency(item.lineTotal)} تومان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "عیار ${item.karat.toString().toPersianDigits()} · ${formatWeight(item.weightGrams)} گرم × ${item.quantity.toString().toPersianDigits()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${stringResource(R.string.invoice_base_gold_value)}: ${formatCurrency(item.baseGoldValue)} · " +
                "${stringResource(R.string.invoice_labor_fee_amount)}: ${formatCurrency(item.laborFeeAmount)} · " +
                "${stringResource(R.string.invoice_profit_amount)}: ${formatCurrency(item.profitAmount)} · " +
                "${stringResource(R.string.invoice_tax_amount)}: ${formatCurrency(item.taxAmount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailTotalRow(label: String, value: Double, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${formatCurrency(value)} تومان",
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
