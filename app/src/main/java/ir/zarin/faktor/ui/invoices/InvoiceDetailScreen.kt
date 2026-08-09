package ir.zarin.faktor.ui.invoices

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.core.Money
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.model.PaymentStatus
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.pdf.InvoiceDocument
import ir.zarin.faktor.ui.common.ConfirmDialog
import ir.zarin.faktor.ui.common.KeyValueRow
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.SectionDivider
import ir.zarin.faktor.ui.common.StatusChip
import ir.zarin.faktor.ui.common.ZarinTopBar
import ir.zarin.faktor.ui.common.color
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun InvoiceDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoiceDetailViewModel = viewModel(factory = InvoiceDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmingDelete by remember { mutableStateOf(false) }
    val pdfErrorText = stringResource(R.string.pdf_error)

    fun withPdf(action: (java.io.File) -> Unit) {
        val data = state.invoice ?: return
        val settings = state.settings
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching { InvoiceDocument.create(context, data, settings) }.getOrNull()
            }
            if (file == null) {
                Toast.makeText(context, pdfErrorText, Toast.LENGTH_SHORT).show()
            } else {
                action(file)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ZarinTopBar(
                title = stringResource(R.string.invoice_detail),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { withPdf { file -> InvoiceDocument.share(context, file) } }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_pdf))
                    }
                    IconButton(
                        onClick = {
                            withPdf { file ->
                                InvoiceDocument.print(
                                    context,
                                    file,
                                    state.invoice?.invoice?.number.orEmpty(),
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Default.Print, contentDescription = stringResource(R.string.print))
                    }
                    IconButton(onClick = { confirmingDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                },
            )
        },
    ) { padding ->
        state.invoice?.let { data ->
            InvoiceDetailContent(
                data = data,
                settings = state.settings,
                onMarkPaid = viewModel::markAsPaid,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    if (confirmingDelete) {
        ConfirmDialog(
            text = stringResource(R.string.delete_invoice_confirm),
            onConfirm = {
                confirmingDelete = false
                viewModel.delete(onBack)
            },
            onDismiss = { confirmingDelete = false },
            confirmLabel = stringResource(R.string.delete),
        )
    }
}

@Composable
fun InvoiceDetailContent(
    data: InvoiceWithItems,
    settings: AppSettings,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    val invoice = data.invoice

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = settings.shopName.ifBlank { stringResource(R.string.app_name) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (settings.shopPhone.isNotBlank()) {
                            Text(
                                text = format.code(settings.shopPhone),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    StatusChip(text = invoice.status.label(), color = invoice.status.color)
                }
                SectionDivider()
                KeyValueRow(
                    label = stringResource(R.string.invoice_number),
                    value = format.code(invoice.number),
                )
                KeyValueRow(
                    label = stringResource(R.string.invoice_date),
                    value = "${format.date(invoice.dateMillis)} - ${format.time(invoice.dateMillis)}",
                )
                KeyValueRow(
                    label = stringResource(R.string.customer),
                    value = invoice.customerName.ifBlank { stringResource(R.string.walk_in_customer) },
                )
                if (invoice.customerPhone.isNotBlank()) {
                    KeyValueRow(
                        label = stringResource(R.string.phone),
                        value = format.code(invoice.customerPhone),
                    )
                }
                KeyValueRow(
                    label = stringResource(R.string.gold_rate_title),
                    value = "${format.money(invoice.goldRatePerGramRial)} ${currencyLabel()}",
                )
                KeyValueRow(
                    label = stringResource(R.string.payment_method),
                    value = invoice.paymentMethod.label(),
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.items),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        items(data.items, key = { it.id }) { item ->
            InvoiceItemCard(item = item)
        }

        item {
            SectionCard(title = stringResource(R.string.subtotal)) {
                KeyValueRow(
                    label = stringResource(R.string.gold_value),
                    value = "${format.money(invoice.goldTotalRial)} ${currencyLabel()}",
                )
                if (invoice.wageTotalRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.wage),
                        value = "${format.money(invoice.wageTotalRial)} ${currencyLabel()}",
                    )
                }
                if (invoice.profitTotalRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.profit),
                        value = "${format.money(invoice.profitTotalRial)} ${currencyLabel()}",
                    )
                }
                if (invoice.stoneTotalRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.stone),
                        value = "${format.money(invoice.stoneTotalRial)} ${currencyLabel()}",
                    )
                }
                if (invoice.vatTotalRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.vat),
                        value = "${format.money(invoice.vatTotalRial)} ${currencyLabel()}",
                    )
                }
                val discount = invoice.itemsDiscountRial + invoice.invoiceDiscountRial
                if (discount > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.discount),
                        value = "${format.money(discount)} ${currencyLabel()}",
                        valueColor = MaterialTheme.colorScheme.error,
                    )
                }
                SectionDivider()
                KeyValueRow(
                    label = stringResource(R.string.payable),
                    value = "${format.money(invoice.grandTotalRial)} ${currencyLabel()}",
                    emphasize = true,
                )
                KeyValueRow(
                    label = stringResource(R.string.paid_amount),
                    value = "${format.money(invoice.paidAmountRial)} ${currencyLabel()}",
                )
                if (invoice.remainingRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.remaining),
                        value = "${format.money(invoice.remainingRial)} ${currencyLabel()}",
                        valueColor = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.amount_in_words,
                        Money.inWords(invoice.grandTotalRial, settings.currencyUnit, currencyLabel()),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (invoice.note.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(R.string.note)}: ${invoice.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (invoice.status != PaymentStatus.PAID) {
            item {
                Button(onClick = onMarkPaid, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.mark_fully_paid))
                }
            }
        }
    }
}

@Composable
private fun InvoiceItemCard(item: InvoiceItem, modifier: Modifier = Modifier) {
    val format = LocalDisplayFormat.current
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${format.money(item.lineTotalRial)} ${currencyLabel()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (item.pricingMode == PricingMode.BY_WEIGHT) {
            KeyValueRow(
                label = stringResource(R.string.weight_gram),
                value = "${format.weight(item.weightGrams)} ${stringResource(R.string.gram)}",
            )
            KeyValueRow(label = stringResource(R.string.karat), value = format.count(item.karat))
            KeyValueRow(
                label = stringResource(R.string.wage_percent),
                value = "${format.percent(item.wagePercent)}٪",
            )
        } else {
            KeyValueRow(
                label = stringResource(R.string.unit_price),
                value = "${format.money(item.unitFixedPriceRial)} ${currencyLabel()}",
            )
        }
        KeyValueRow(label = stringResource(R.string.quantity), value = format.count(item.quantity))
        if (item.stoneRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.stone),
                value = "${format.money(item.stoneRial)} ${currencyLabel()}",
            )
        }
        if (item.discountRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.discount),
                value = "${format.money(item.discountRial)} ${currencyLabel()}",
                valueColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
