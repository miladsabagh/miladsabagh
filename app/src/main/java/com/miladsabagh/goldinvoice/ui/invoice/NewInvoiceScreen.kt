package com.miladsabagh.goldinvoice.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.entity.AVAILABLE_KARATS
import com.miladsabagh.goldinvoice.data.entity.Customer
import com.miladsabagh.goldinvoice.data.entity.Product
import com.miladsabagh.goldinvoice.ui.components.LabeledDropdown
import com.miladsabagh.goldinvoice.ui.components.LabeledNumberField
import com.miladsabagh.goldinvoice.ui.components.SectionCard
import com.miladsabagh.goldinvoice.util.formatCurrency
import com.miladsabagh.goldinvoice.util.formatWeight
import com.miladsabagh.goldinvoice.util.parseLocalizedDouble
import com.miladsabagh.goldinvoice.util.parseLocalizedInt
import com.miladsabagh.goldinvoice.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    viewModel: NewInvoiceViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.savedInvoiceId) {
        state.savedInvoiceId?.let { onSaved(it) }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoice_new_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = stringResource(R.string.invoice_select_customer)) {
                OutlinedButton(onClick = { showCustomerDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(state.customer?.fullName ?: stringResource(R.string.customer_walk_in))
                }
            }

            SectionCard {
                LabeledNumberField(
                    label = stringResource(R.string.invoice_gold_price),
                    value = state.goldPricePerGram18k,
                    onValueChange = viewModel::setGoldPrice,
                    suffix = stringResource(R.string.unit_toman)
                )
            }

            SectionCard(title = stringResource(R.string.invoice_items)) {
                if (state.items.isEmpty()) {
                    Text(
                        text = "هنوز ردیفی اضافه نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.lineBreakdowns.forEach { (draft, breakdown) ->
                        InvoiceLineRow(
                            name = draft.itemName,
                            weightGrams = draft.weightGrams,
                            karat = draft.karat,
                            quantity = draft.quantity,
                            lineTotal = breakdown.lineTotal,
                            onRemove = { viewModel.removeLine(draft.localId) }
                        )
                    }
                }
                OutlinedButton(onClick = { showAddItemDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.invoice_add_item))
                }
            }

            SectionCard {
                LabeledNumberField(
                    label = stringResource(R.string.invoice_discount) + " (٪)",
                    value = state.discountPercent,
                    onValueChange = viewModel::setDiscountPercent
                )
                LabeledNumberField(
                    label = stringResource(R.string.invoice_paid_amount),
                    value = state.paidAmount,
                    onValueChange = viewModel::setPaidAmount,
                    suffix = stringResource(R.string.unit_toman)
                )
                LabeledNumberField(
                    label = stringResource(R.string.invoice_notes),
                    value = state.notes,
                    onValueChange = viewModel::setNotes
                )
            }

            InvoiceTotalsCard(state)

            Button(onClick = viewModel::saveInvoice, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.invoice_save))
            }
        }
    }

    if (showCustomerDialog) {
        CustomerPickerDialog(
            customers = state.customers,
            onSelect = { customer ->
                viewModel.setCustomer(customer)
                showCustomerDialog = false
            },
            onDismiss = { showCustomerDialog = false }
        )
    }

    if (showAddItemDialog) {
        AddInvoiceItemDialog(
            products = state.products,
            defaultLaborFeePercent = state.defaultLaborFeePercent,
            defaultProfitPercent = state.defaultProfitPercent,
            defaultTaxPercent = state.defaultTaxPercent,
            onAddFromProduct = { product, qty ->
                viewModel.addLineFromProduct(product, qty)
                showAddItemDialog = false
            },
            onAddManual = { name, weight, karat, labor, profit, tax, qty ->
                viewModel.addManualLine(name, weight, karat, labor, profit, tax, qty)
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }
}

@Composable
private fun InvoiceLineRow(
    name: String,
    weightGrams: Double,
    karat: Int,
    quantity: Int,
    lineTotal: Double,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "عیار ${karat.toString().toPersianDigits()} · ${formatWeight(weightGrams)} گرم × ${quantity.toString().toPersianDigits()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatCurrency(lineTotal)} تومان",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun InvoiceTotalsCard(state: NewInvoiceUiState) {
    val totals = state.totals
    SectionCard(title = "جمع‌بندی") {
        TotalRow(stringResource(R.string.invoice_subtotal), totals.subtotal)
        TotalRow(stringResource(R.string.invoice_discount), totals.discountAmount)
        TotalRow(stringResource(R.string.invoice_grand_total), totals.grandTotal, emphasize = true)
        val remaining = (totals.grandTotal - state.paidAmountValue).coerceAtLeast(0.0)
        TotalRow(stringResource(R.string.invoice_remaining), remaining)
    }
}

@Composable
private fun TotalRow(label: String, value: Double, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${formatCurrency(value)} تومان",
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CustomerPickerDialog(
    customers: List<Customer>,
    onSelect: (Customer?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.invoice_select_customer)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.customer_walk_in))
                }
                customers.forEach { customer ->
                    TextButton(onClick = { onSelect(customer) }, modifier = Modifier.fillMaxWidth()) {
                        Text(customer.fullName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun AddInvoiceItemDialog(
    products: List<Product>,
    defaultLaborFeePercent: Double,
    defaultProfitPercent: Double,
    defaultTaxPercent: Double,
    onAddFromProduct: (Product, Int) -> Unit,
    onAddManual: (String, Double, Int, Double, Double, Double, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var useManual by remember { mutableStateOf(products.isEmpty()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }

    var manualName by remember { mutableStateOf("") }
    var manualWeight by remember { mutableStateOf("") }
    var manualKarat by remember { mutableStateOf(18) }
    var manualLabor by remember { mutableStateOf(defaultLaborFeePercent.toString()) }
    var manualProfit by remember { mutableStateOf(defaultProfitPercent.toString()) }
    var manualTax by remember { mutableStateOf(defaultTaxPercent.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.invoice_add_item)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { useManual = false },
                        label = { Text(stringResource(R.string.invoice_add_from_product)) },
                        enabled = products.isNotEmpty()
                    )
                    AssistChip(
                        onClick = { useManual = true },
                        label = { Text(stringResource(R.string.invoice_add_manual)) }
                    )
                }

                if (!useManual && products.isNotEmpty()) {
                    LabeledDropdown(
                        label = stringResource(R.string.product_name),
                        options = products,
                        selected = selectedProduct ?: products.first(),
                        optionLabel = { it.name },
                        onSelected = { selectedProduct = it }
                    )
                    LabeledNumberField(
                        label = stringResource(R.string.product_quantity),
                        value = quantityText,
                        onValueChange = { quantityText = it }
                    )
                } else {
                    LabeledNumberField(label = stringResource(R.string.product_name), value = manualName, onValueChange = { manualName = it })
                    LabeledNumberField(label = stringResource(R.string.product_weight), value = manualWeight, onValueChange = { manualWeight = it }, suffix = stringResource(R.string.unit_gram))
                    LabeledDropdown(
                        label = stringResource(R.string.product_karat),
                        options = AVAILABLE_KARATS,
                        selected = manualKarat,
                        optionLabel = { it.toString().toPersianDigits() },
                        onSelected = { manualKarat = it }
                    )
                    LabeledNumberField(label = stringResource(R.string.product_labor_fee), value = manualLabor, onValueChange = { manualLabor = it }, suffix = "٪")
                    LabeledNumberField(label = stringResource(R.string.product_profit), value = manualProfit, onValueChange = { manualProfit = it }, suffix = "٪")
                    LabeledNumberField(label = stringResource(R.string.product_tax), value = manualTax, onValueChange = { manualTax = it }, suffix = "٪")
                    LabeledNumberField(label = stringResource(R.string.product_quantity), value = quantityText, onValueChange = { quantityText = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val quantity = parseLocalizedInt(quantityText) ?: 1
                if (!useManual && selectedProduct != null) {
                    onAddFromProduct(selectedProduct!!, quantity)
                } else {
                    val weight = parseLocalizedDouble(manualWeight) ?: 0.0
                    if (manualName.isNotBlank() && weight > 0) {
                        onAddManual(
                            manualName,
                            weight,
                            manualKarat,
                            parseLocalizedDouble(manualLabor) ?: 0.0,
                            parseLocalizedDouble(manualProfit) ?: 0.0,
                            parseLocalizedDouble(manualTax) ?: 0.0,
                            quantity
                        )
                    }
                }
            }) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
