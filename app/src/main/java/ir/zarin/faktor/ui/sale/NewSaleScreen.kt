package ir.zarin.faktor.ui.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.domain.PriceBreakdown
import ir.zarin.faktor.ui.common.ChoiceChips
import ir.zarin.faktor.ui.common.EmptyState
import ir.zarin.faktor.ui.common.KeyValueRow
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.NumericField
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.SectionDivider
import ir.zarin.faktor.ui.common.ZarinTextField
import ir.zarin.faktor.ui.common.ZarinTopBar
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.label

@Composable
fun NewSaleScreen(
    onBack: () -> Unit,
    onInvoiceCreated: (Long) -> Unit,
    onAddCustomer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewSaleViewModel = viewModel(factory = NewSaleViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val savedInvoiceId by viewModel.savedInvoiceId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(savedInvoiceId) {
        savedInvoiceId?.let(onInvoiceCreated)
    }

    val messageText = message?.let { stringResource(it) }
    LaunchedEffect(messageText) {
        messageText?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { ZarinTopBar(title = stringResource(R.string.new_sale_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SaleBottomBar(
                totalRial = state.grandTotalRial,
                enabled = state.canSubmit,
                onSubmit = viewModel::submit,
            )
        },
    ) { padding ->
        NewSaleContent(
            state = state,
            onSelectCustomer = viewModel::selectCustomer,
            onAddCustomer = onAddCustomer,
            onAddLine = viewModel::addLine,
            onUpdateLine = viewModel::updateLine,
            onRemoveLine = viewModel::removeLine,
            onInvoiceDiscountChange = viewModel::setInvoiceDiscount,
            onFullyPaidChange = viewModel::setFullyPaid,
            onPaidAmountChange = viewModel::setPaidAmount,
            onPaymentMethodChange = viewModel::setPaymentMethod,
            onNoteChange = viewModel::setNote,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
fun NewSaleContent(
    state: NewSaleUiState,
    onSelectCustomer: (Customer?) -> Unit,
    onAddCustomer: () -> Unit,
    onAddLine: (SaleLine) -> Unit,
    onUpdateLine: (SaleLine) -> Unit,
    onRemoveLine: (String) -> Unit,
    onInvoiceDiscountChange: (Long) -> Unit,
    onFullyPaidChange: (Boolean) -> Unit,
    onPaidAmountChange: (Long) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    var editingLine by remember { mutableStateOf<SaleLine?>(null) }
    var isNewLine by remember { mutableStateOf(true) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val breakdowns = state.breakdowns
    val totals = state.totals

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard(title = stringResource(R.string.customer)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(8.dp))
                        Column {
                            Text(
                                text = state.draft.customer?.name
                                    ?: stringResource(R.string.walk_in_customer),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            state.draft.customer?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                Text(
                                    text = format.code(phone),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    TextButton(onClick = { showCustomerPicker = true }) {
                        Text(stringResource(R.string.select_customer))
                    }
                }
            }
        }

        item {
            SectionCard(
                title = stringResource(R.string.items),
                trailing = {
                    TextButton(
                        onClick = {
                            isNewLine = true
                            editingLine = SaleLine()
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(" ${stringResource(R.string.add_item)}")
                    }
                },
            ) {
                if (state.draft.lines.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ShoppingBag,
                        text = stringResource(R.string.no_items),
                    )
                }
            }
        }

        items(state.draft.lines.size, key = { index -> state.draft.lines[index].key }) { index ->
            val line = state.draft.lines[index]
            SaleLineCard(
                line = line,
                breakdown = breakdowns[index],
                onEdit = {
                    isNewLine = false
                    editingLine = line
                },
                onRemove = { onRemoveLine(line.key) },
            )
        }

        item {
            SectionCard(title = stringResource(R.string.payment_method)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChoiceChips(
                        options = PaymentMethod.entries,
                        selected = state.draft.paymentMethod,
                        onSelect = onPaymentMethodChange,
                        label = { it.label() },
                    )
                    NumericField(
                        value = format.amountForInput(state.draft.invoiceDiscountRial),
                        onValueChange = { onInvoiceDiscountChange(format.inputToRial(it)) },
                        label = stringResource(R.string.invoice_discount),
                        suffix = currencyLabel(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.mark_fully_paid),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(checked = state.draft.fullyPaid, onCheckedChange = onFullyPaidChange)
                    }
                    if (!state.draft.fullyPaid) {
                        NumericField(
                            value = format.amountForInput(state.draft.paidAmountRial),
                            onValueChange = { onPaidAmountChange(format.inputToRial(it)) },
                            label = stringResource(R.string.paid_amount),
                            suffix = currencyLabel(),
                        )
                    }
                    ZarinTextField(
                        value = state.draft.note,
                        onValueChange = onNoteChange,
                        label = stringResource(R.string.note),
                        singleLine = false,
                        minLines = 2,
                    )
                }
            }
        }

        item {
            SectionCard(title = stringResource(R.string.subtotal)) {
                KeyValueRow(
                    label = stringResource(R.string.gold_value),
                    value = "${format.money(totals.goldTotalRial)} ${currencyLabel()}",
                )
                KeyValueRow(
                    label = stringResource(R.string.wage),
                    value = "${format.money(totals.wageTotalRial)} ${currencyLabel()}",
                )
                KeyValueRow(
                    label = stringResource(R.string.profit),
                    value = "${format.money(totals.profitTotalRial)} ${currencyLabel()}",
                )
                if (totals.stoneTotalRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.stone),
                        value = "${format.money(totals.stoneTotalRial)} ${currencyLabel()}",
                    )
                }
                KeyValueRow(
                    label = stringResource(R.string.vat),
                    value = "${format.money(totals.vatTotalRial)} ${currencyLabel()}",
                )
                val discount = totals.itemsDiscountRial + totals.invoiceDiscountRial
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
                    value = "${format.money(totals.grandTotalRial)} ${currencyLabel()}",
                    emphasize = true,
                )
                if (state.remainingRial > 0) {
                    KeyValueRow(
                        label = stringResource(R.string.remaining),
                        value = "${format.money(state.remainingRial)} ${currencyLabel()}",
                        valueColor = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    editingLine?.let { line ->
        ItemEditorSheet(
            initial = line,
            pricingContext = state.settings.pricingContext,
            products = state.products,
            onConfirm = { updated ->
                if (isNewLine) onAddLine(updated) else onUpdateLine(updated)
                editingLine = null
            },
            onDismiss = { editingLine = null },
        )
    }

    if (showCustomerPicker) {
        CustomerPickerSheet(
            customers = state.customers,
            onSelect = { customer ->
                onSelectCustomer(customer)
                showCustomerPicker = false
            },
            onAddCustomer = {
                showCustomerPicker = false
                onAddCustomer()
            },
            onDismiss = { showCustomerPicker = false },
        )
    }
}

@Composable
private fun SaleLineCard(
    line: SaleLine,
    breakdown: PriceBreakdown,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lineSubtitle(line),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${format.money(breakdown.totalRial)} ${currencyLabel()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun lineSubtitle(line: SaleLine): String {
    val format = LocalDisplayFormat.current
    val parts = buildList {
        add(line.category.label())
        if (line.pricingMode == PricingMode.BY_WEIGHT) {
            add("${format.weight(line.weightGrams)} ${stringResource(R.string.gram)}")
            add("${stringResource(R.string.karat)} ${format.count(line.karat)}")
            add("${stringResource(R.string.wage)} ${format.percent(line.wagePercent)}٪")
        } else {
            add(stringResource(R.string.pricing_fixed))
        }
        add("${stringResource(R.string.quantity)} ${format.count(line.quantity)}")
    }
    return parts.joinToString(" • ")
}

@Composable
private fun SaleBottomBar(
    totalRial: Long,
    enabled: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.payable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${format.money(totalRial)} ${currencyLabel()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Text(stringResource(R.string.submit_invoice))
            }
        }
    }
}