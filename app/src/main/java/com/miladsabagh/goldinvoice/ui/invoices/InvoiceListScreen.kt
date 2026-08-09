package com.miladsabagh.goldinvoice.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.entity.PaymentStatus
import com.miladsabagh.goldinvoice.ui.components.EmptyState
import com.miladsabagh.goldinvoice.ui.theme.SuccessGreen
import com.miladsabagh.goldinvoice.ui.theme.WarningAmber
import com.miladsabagh.goldinvoice.util.formatCurrency
import com.miladsabagh.goldinvoice.util.formatInvoiceDate
import com.miladsabagh.goldinvoice.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceListViewModel,
    onNewInvoice: () -> Unit,
    onInvoiceClick: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.invoices_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewInvoice) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.invoice_new_title))
            }
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.invoices_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(invoices, key = { it.id }) { invoice ->
                    InvoiceRow(invoice, onClick = { onInvoiceClick(invoice.id) })
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stringResource(R.string.invoice_number)} ${invoice.invoiceNumber.toPersianDigits()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = invoice.customerNameSnapshot.ifBlank { stringResource(R.string.customer_walk_in) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatInvoiceDate(invoice.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = "${formatCurrency(invoice.grandTotal)} تومان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(height = 4)
                PaymentStatusBadge(invoice.paymentStatus)
            }
        }
    }
}

@Composable
private fun Spacer(height: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height.dp))
}

@Composable
private fun PaymentStatusBadge(status: PaymentStatus) {
    val (label, color) = when (status) {
        PaymentStatus.PAID -> stringResource(R.string.invoice_status_paid) to SuccessGreen
        PaymentStatus.PARTIAL -> stringResource(R.string.invoice_status_partial) to WarningAmber
        PaymentStatus.UNPAID -> stringResource(R.string.invoice_status_unpaid) to MaterialTheme.colorScheme.error
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
