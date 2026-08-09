package com.zarrin.goldshop.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.domain.formatDateFa
import com.zarrin.goldshop.domain.formatPercentFa
import com.zarrin.goldshop.domain.formatTomanFa
import com.zarrin.goldshop.domain.formatWeightFa
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.EmptyState
import com.zarrin.goldshop.ui.components.ListDivider
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.components.ScreenScaffold
import com.zarrin.goldshop.ui.components.SectionLabel

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val invoiceWithItems by viewModel.selectedInvoice.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    val data = invoiceWithItems
    if (data == null || data.invoice.id != invoiceId) {
        ScreenScaffold(title = "فاکتور", onBack = onBack) {
            EmptyState("در حال بارگذاری فاکتور...")
        }
        return
    }

    val invoice = data.invoice
    ScreenScaffold(
        title = "جزئیات فاکتور",
        subtitle = toPersianDigits(invoice.invoiceNumber),
        onBack = onBack
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Text("خریدار: ${invoice.customerName}", style = MaterialTheme.typography.titleMedium)
            if (invoice.customerPhone.isNotBlank()) {
                Text(
                    "تلفن: ${toPersianDigits(invoice.customerPhone)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "تاریخ: ${formatDateFa(invoice.createdAt)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "قیمت روز طلای ۱۸: ${formatTomanFa(invoice.goldPrice18PerGram)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            SectionLabel("اقلام")
            data.items.forEachIndexed { index, item ->
                Text(
                    "${toPersianDigits((index + 1).toString())}. ${item.productName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "کد ${toPersianDigits(item.productCode)} · ${formatWeightFa(item.weightGrams)} · عیار ${toPersianDigits(item.purityKarat.toString())} · اجرت ${formatPercentFa(item.makingFeePercent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(formatTomanFa(item.lineTotal), style = MaterialTheme.typography.bodyLarge)
                ListDivider()
            }

            SectionLabel("مبالغ")
            Text("جمع اقلام: ${formatTomanFa(invoice.subtotal)}")
            Text("سود (${formatPercentFa(invoice.profitPercent)}): ${formatTomanFa(invoice.profitAmount)}")
            Text("مالیات (${formatPercentFa(invoice.vatPercent)}): ${formatTomanFa(invoice.vatAmount)}")
            Spacer(Modifier.height(8.dp))
            Text(
                "مبلغ کل: ${formatTomanFa(invoice.totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text("پرداخت‌شده: ${formatTomanFa(invoice.paidAmount)}")
            Text(
                "مانده: ${formatTomanFa((invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0))}"
            )

            if (invoice.notes.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("توضیحات: ${invoice.notes}")
            }

            Spacer(Modifier.height(20.dp))
            PrimaryActionButton(
                text = "اشتراک‌گذاری PDF فاکتور",
                onClick = {
                    val intent = viewModel.shareInvoice(invoice.id)
                    if (intent != null) {
                        context.startActivity(Intent.createChooser(intent, "اشتراک فاکتور"))
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
