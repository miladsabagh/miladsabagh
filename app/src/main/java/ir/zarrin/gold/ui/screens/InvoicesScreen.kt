package ir.zarrin.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import ir.zarrin.gold.ui.components.EmptyState
import ir.zarrin.gold.util.JalaliDate
import ir.zarrin.gold.util.PersianFormat

private enum class InvoiceFilter(val label: String) {
    ALL("همه"), UNPAID("پرداخت نشده"), PAID("پرداخت شده")
}

@Composable
fun InvoicesScreen(
    viewModel: AppViewModel,
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
) {
    val invoices by viewModel.invoices.collectAsState()
    var filter by remember { mutableStateOf(InvoiceFilter.ALL) }

    val filtered = when (filter) {
        InvoiceFilter.ALL -> invoices
        InvoiceFilter.PAID -> invoices.filter { it.invoice.paid }
        InvoiceFilter.UNPAID -> invoices.filter { !it.invoice.paid }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewInvoice,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("فاکتور جدید") },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "فاکتورها",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InvoiceFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (filtered.isEmpty()) {
                EmptyState("فاکتوری یافت نشد.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filtered, key = { it.invoice.id }) { inv ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenInvoice(inv.invoice.id) },
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "فاکتور ${PersianFormat.toPersianDigits(inv.invoice.number.toString())} — ${inv.invoice.customerName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        JalaliDate.format(inv.invoice.date) +
                                            "  •  ${PersianFormat.formatNumber(inv.items.size.toLong())} قلم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        PersianFormat.formatCurrency(inv.invoice.total),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        if (inv.invoice.paid) "پرداخت شده" else "پرداخت نشده",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (inv.invoice.paid) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
