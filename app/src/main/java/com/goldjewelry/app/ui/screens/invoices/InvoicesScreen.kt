package com.goldjewelry.app.ui.screens.invoices

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goldjewelry.app.data.model.Invoice
import com.goldjewelry.app.data.model.InvoiceWithItems
import com.goldjewelry.app.ui.theme.GoldPrimary
import com.goldjewelry.app.ui.viewmodel.InvoicesViewModel
import com.goldjewelry.app.util.InvoicePdfGenerator
import com.goldjewelry.app.util.PersianFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesListScreen(
    viewModel: InvoicesViewModel,
    onInvoiceClick: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "فاکتورها",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (invoices.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "هنوز فاکتوری صادر نشده است",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(invoices) { invoice ->
                    InvoiceListItem(invoice = invoice, onClick = { onInvoiceClick(invoice.id) })
                }
            }
        }
    }
}

@Composable
private fun InvoiceListItem(invoice: Invoice, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("fa", "IR"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "فاکتور ${PersianFormatter.formatInvoiceNumber(invoice.invoiceNumber)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(invoice.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PersianFormatter.formatCurrency(invoice.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = invoice.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoicesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val invoiceWithItems by viewModel.selectedInvoice.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات فاکتور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        invoiceWithItems?.let { data ->
            InvoiceDetailContent(
                data = data,
                modifier = Modifier.padding(padding),
                onSharePdf = {
                    settings?.let { shopSettings ->
                        val uri = InvoicePdfGenerator.generate(context, data, shopSettings)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
                    }
                }
            )
        } ?: run {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("در حال بارگذاری...")
            }
        }
    }
}

@Composable
private fun InvoiceDetailContent(
    data: InvoiceWithItems,
    modifier: Modifier = Modifier,
    onSharePdf: () -> Unit
) {
    val invoice = data.invoice
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("fa", "IR"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "فاکتور ${PersianFormatter.formatInvoiceNumber(invoice.invoiceNumber)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(invoice.createdAt)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "مشتری: ${invoice.customerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (invoice.customerPhone.isNotBlank()) {
                        Text(
                            text = "تلفن: ${invoice.customerPhone}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "اقلام فاکتور",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(data.items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${item.category.label} • ${PersianFormatter.formatWeight(item.weightGrams)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "تعداد: ${PersianFormatter.toPersianDigits(item.quantity.toString())}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = PersianFormatter.formatCurrency(item.lineTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("جمع کل", PersianFormatter.formatCurrency(invoice.subtotal))
                    if (invoice.discount > 0) {
                        DetailRow("تخفیف", PersianFormatter.formatCurrency(invoice.discount))
                    }
                    if (invoice.tax > 0) {
                        DetailRow("مالیات", PersianFormatter.formatCurrency(invoice.tax))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        "مبلغ قابل پرداخت",
                        PersianFormatter.formatCurrency(invoice.total),
                        bold = true
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSharePdf,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("دانلود و اشتراک PDF")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) GoldPrimary else MaterialTheme.colorScheme.onSurface,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}
