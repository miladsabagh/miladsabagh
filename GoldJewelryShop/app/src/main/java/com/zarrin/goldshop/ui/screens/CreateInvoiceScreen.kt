package com.zarrin.goldshop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.domain.categoryLabel
import com.zarrin.goldshop.domain.formatPercentFa
import com.zarrin.goldshop.domain.formatTomanFa
import com.zarrin.goldshop.domain.formatWeightFa
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.AppTextField
import com.zarrin.goldshop.ui.components.ListDivider
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.components.QuantityStepper
import com.zarrin.goldshop.ui.components.ScreenScaffold
import com.zarrin.goldshop.ui.components.SectionLabel

@Composable
fun CreateInvoiceScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onInvoiceCreated: (Long) -> Unit
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val preview by viewModel.draftPreview.collectAsStateWithLifecycle()

    val createdId = draft.lastCreatedInvoiceId
    if (createdId != null) {
        ScreenScaffold(
            title = "فاکتور صادر شد",
            subtitle = "آماده مشاهده و اشتراک‌گذاری PDF"
        ) {
            PrimaryActionButton(
                text = "مشاهده فاکتور",
                onClick = {
                    viewModel.clearDraft()
                    onInvoiceCreated(createdId)
                }
            )
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = {
                viewModel.clearDraft()
                onBack()
            }) { Text("بازگشت به خانه") }
        }
        return
    }

    ScreenScaffold(
        title = "صدور فاکتور",
        subtitle = "محاسبه بر اساس وزن، عیار، اجرت، سود و مالیات",
        onBack = onBack
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SectionLabel("خریدار")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = draft.selectedCustomer == null,
                    onClick = { viewModel.selectCustomer(null) },
                    label = { Text("مشتری جدید") }
                )
                customers.take(4).forEach { customer ->
                    FilterChip(
                        selected = draft.selectedCustomer?.id == customer.id,
                        onClick = { viewModel.selectCustomer(customer) },
                        label = { Text(customer.fullName) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            AppTextField(
                value = draft.customerName,
                onValueChange = { viewModel.updateDraftCustomer(it, draft.customerPhone) },
                label = "نام خریدار"
            )
            Spacer(Modifier.height(10.dp))
            AppTextField(
                value = draft.customerPhone,
                onValueChange = { viewModel.updateDraftCustomer(draft.customerName, it) },
                label = "تلفن",
                keyboardType = KeyboardType.Phone
            )

            Spacer(Modifier.height(18.dp))
            SectionLabel("افزودن کالا")
            Text(
                "قیمت روز: ${formatTomanFa(settings.goldPrice18PerGram)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addProductToDraft(product) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${categoryLabel(product.category)} · ${formatWeightFa(product.weightGrams)} · عیار ${toPersianDigits(product.purityKarat.toString())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { viewModel.addProductToDraft(product) }) {
                        Text("افزودن")
                    }
                }
                ListDivider()
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("اقلام فاکتور")
            if (draft.items.isEmpty()) {
                Text(
                    "هنوز کالایی انتخاب نشده است",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                draft.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "اجرت ${formatPercentFa(item.product.makingFeePercent)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        QuantityStepper(
                            quantity = item.quantity,
                            onChange = { viewModel.changeDraftQuantity(item.product.id, it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("خلاصه مالی")
            SummaryRow("جمع اقلام", formatTomanFa(preview.subtotal))
            SummaryRow(
                "سود (${formatPercentFa(settings.profitPercent)})",
                formatTomanFa(preview.profitAmount)
            )
            SummaryRow(
                "مالیات (${formatPercentFa(settings.vatPercent)})",
                formatTomanFa(preview.vatAmount)
            )
            SummaryRow("مبلغ کل", formatTomanFa(preview.totalAmount), emphasize = true)

            Spacer(Modifier.height(12.dp))
            AppTextField(
                value = draft.paidAmountText,
                onValueChange = { viewModel.updatePaidAndNotes(it, draft.notes) },
                label = "مبلغ پرداخت‌شده (تومان)",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(10.dp))
            AppTextField(
                value = draft.notes,
                onValueChange = { viewModel.updatePaidAndNotes(draft.paidAmountText, it) },
                label = "توضیحات فاکتور",
                singleLine = false
            )

            draft.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(18.dp))
            PrimaryActionButton(
                text = "ثبت و صدور فاکتور",
                onClick = { viewModel.createInvoice() }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (emphasize) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            value,
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            }
        )
    }
}
