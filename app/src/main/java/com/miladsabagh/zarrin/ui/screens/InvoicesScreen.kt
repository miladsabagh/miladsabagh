package com.miladsabagh.zarrin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miladsabagh.zarrin.ui.InvoicesViewModel
import com.miladsabagh.zarrin.ui.components.EmptyState
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.formatDateTime
import com.miladsabagh.zarrin.util.formatInvoiceNumber
import com.miladsabagh.zarrin.util.formatToman
import com.miladsabagh.zarrin.util.toPersianDigits

@Composable
fun InvoicesScreen(
    viewModel: InvoicesViewModel,
    onOpenInvoice: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    InvoicesContent(invoices = invoices, onOpenInvoice = onOpenInvoice)
}

@Composable
fun InvoicesContent(
    invoices: List<com.miladsabagh.zarrin.data.db.InvoiceWithItems>,
    onOpenInvoice: (Long) -> Unit
) {
    Scaffold(
        topBar = { ZarrinTopBar(title = "فاکتورها") }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (invoices.isEmpty()) {
                EmptyState("هنوز فاکتوری صادر نشده است", Icons.AutoMirrored.Filled.ReceiptLong)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices, key = { it.invoice.id }) { entry ->
                        Card(
                            onClick = { onOpenInvoice(entry.invoice.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "فاکتور ${entry.invoice.invoiceNumber.formatInvoiceNumber()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        entry.invoice.customerName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${entry.invoice.createdAt.formatDateTime()} • ${entry.items.size.toString().toPersianDigits()} قلم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    entry.invoice.grandTotal.formatToman(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
