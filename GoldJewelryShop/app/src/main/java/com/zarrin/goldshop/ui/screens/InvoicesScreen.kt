package com.zarrin.goldshop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.domain.formatDateFa
import com.zarrin.goldshop.domain.formatTomanFa
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.EmptyState
import com.zarrin.goldshop.ui.components.ListDivider
import com.zarrin.goldshop.ui.components.ScreenScaffold

@Composable
fun InvoicesScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "فاکتورها",
        subtitle = "تاریخچه فروش و صدور مجدد",
        onBack = onBack
    ) {
        if (invoices.isEmpty()) {
            EmptyState("هنوز فاکتوری صادر نشده است")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(invoices, key = { it.id }) { invoice ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenInvoice(invoice.id) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            toPersianDigits(invoice.invoiceNumber),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${invoice.customerName} · ${formatDateFa(invoice.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatTomanFa(invoice.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { viewModel.deleteInvoice(invoice) }) {
                            Text("حذف", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    ListDivider()
                }
            }
        }
    }
}
