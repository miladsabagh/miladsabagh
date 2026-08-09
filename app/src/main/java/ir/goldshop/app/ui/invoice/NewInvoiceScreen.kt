package ir.goldshop.app.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.goldshop.app.data.entity.Customer
import ir.goldshop.app.data.entity.PaymentMethod
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.domain.PricingCalculator
import ir.goldshop.app.ui.components.NumberField
import ir.goldshop.app.ui.components.SectionCard
import ir.goldshop.app.ui.components.SummaryRow
import ir.goldshop.app.util.formatToman
import ir.goldshop.app.util.formatWeight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    onInvoiceCreated: (Long) -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val scope = rememberCoroutineScope()

    var useManualCustomer by remember { mutableStateOf(false) }
    var customerFieldQuery by remember { mutableStateOf("") }
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetDraft()
        viewModel.initDraftFromSettings()
    }

    val filteredCustomers = remember(customers, customerFieldQuery) {
        if (customerFieldQuery.isBlank()) customers
        else customers.filter { it.fullName.contains(customerFieldQuery) || it.phoneNumber.contains(customerFieldQuery) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صدور فاکتور جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "مشخصات مشتری") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !useManualCustomer,
                            onClick = { useManualCustomer = false },
                            label = { Text("انتخاب از لیست مشتریان") }
                        )
                        FilterChip(
                            selected = useManualCustomer,
                            onClick = { useManualCustomer = true },
                            label = { Text("مشتری جدید / موقت") }
                        )
                    }

                    if (!useManualCustomer) {
                        ExposedDropdownMenuBox(
                            expanded = customerMenuExpanded,
                            onExpandedChange = { customerMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = customerFieldQuery.ifBlank { draft.customerName },
                                onValueChange = {
                                    customerFieldQuery = it
                                    customerMenuExpanded = true
                                },
                                label = { Text("جستجوی مشتری") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = customerMenuExpanded && filteredCustomers.isNotEmpty(),
                                onDismissRequest = { customerMenuExpanded = false }
                            ) {
                                filteredCustomers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text("${customer.fullName} ${if (customer.phoneNumber.isNotBlank()) "· ${customer.phoneNumber}" else ""}") },
                                        onClick = {
                                            viewModel.selectCustomer(customer)
                                            customerFieldQuery = customer.fullName
                                            customerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        if (draft.customerName.isNotBlank()) {
                            Text(
                                "مشتری انتخاب‌شده: ${draft.customerName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = draft.customerName,
                            onValueChange = { viewModel.updateManualCustomer(it, draft.customerPhone, draft.customerAddress) },
                            label = { Text("نام مشتری") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = draft.customerPhone,
                            onValueChange = { viewModel.updateManualCustomer(draft.customerName, it, draft.customerAddress) },
                            label = { Text("شماره تماس (اختیاری)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                SectionCard(title = "نرخ روز طلا") {
                    NumberField(
                        label = "نرخ هر گرم طلای ۱۸ عیار",
                        value = draft.goldPricePerGram,
                        onValueChange = viewModel::updateGoldPrice,
                        suffix = "تومان"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showProductPicker = true }) {
                            Text("از انبار")
                        }
                        OutlinedButton(onClick = { viewModel.addBlankItem() }) {
                            Text("+ قلم دستی")
                        }
                    }
                }
            }

            if (draft.items.isEmpty()) {
                item {
                    Text(
                        "هنوز قلمی به فاکتور اضافه نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(draft.items, key = { it.localId }) { item ->
                InvoiceItemCard(
                    item = item,
                    goldPricePerGram = draft.goldPricePerGram.toDoubleOrNull() ?: 0.0,
                    onChange = viewModel::updateItem,
                    onRemove = { viewModel.removeItem(item.localId) }
                )
            }

            item {
                SectionCard(title = "تخفیف و پرداخت") {
                    NumberField(
                        label = "تخفیف",
                        value = draft.discountAmount,
                        onValueChange = viewModel::updateDiscount,
                        suffix = "تومان"
                    )
                    Text("روش پرداخت", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = draft.paymentMethod == method,
                                onClick = { viewModel.updatePaymentMethod(method) },
                                label = { Text(method.persianLabel) }
                            )
                        }
                    }
                    NumberField(
                        label = "مبلغ پرداخت‌شده (خالی = پرداخت کامل)",
                        value = draft.paidAmount,
                        onValueChange = viewModel::updatePaidAmount,
                        suffix = "تومان"
                    )
                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("توضیحات فاکتور (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("مجموع وزن", "${draft.totalWeight.formatWeight()} گرم")
                        SummaryRow("جمع اقلام", "${draft.subtotal.formatToman()} تومان")
                        SummaryRow("تخفیف", "${draft.discount.formatToman()} تومان")
                        HorizontalDivider()
                        SummaryRow("مبلغ نهایی قابل پرداخت", "${draft.total.formatToman()} تومان", emphasize = true)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (!saving) {
                            saving = true
                            scope.launch {
                                val id = viewModel.submitInvoice()
                                saving = false
                                if (id != null) onInvoiceCreated(id)
                            }
                        }
                    },
                    enabled = draft.isValid && !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(if (saving) "در حال ذخیره..." else "صدور و ذخیره فاکتور")
                }
            }
        }
    }

    if (showProductPicker) {
        ProductPickerDialog(
            products = products,
            onDismiss = { showProductPicker = false },
            onSelect = { product ->
                viewModel.addItemFromProduct(product)
                showProductPicker = false
            }
        )
    }
}

@Composable
private fun InvoiceItemCard(
    item: DraftInvoiceItem,
    goldPricePerGram: Double,
    onChange: (DraftInvoiceItem) -> Unit,
    onRemove: () -> Unit
) {
    val result = item.result
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.itemName,
                    onValueChange = { onChange(item.copy(itemName = it)) },
                    label = { Text("نام قلم") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "حذف قلم")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(
                    label = "عیار",
                    value = item.karat.toString(),
                    onValueChange = { text ->
                        val karat = text.toIntOrNull() ?: item.karat
                        onChange(item.copy(karat = karat))
                    },
                    allowDecimal = false,
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "وزن (گرم)",
                    value = if (item.weightGrams == 0.0) "" else item.weightGrams.toString(),
                    onValueChange = { text ->
                        onChange(item.copy(weightGrams = text.toDoubleOrNull() ?: 0.0))
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "تعداد",
                    value = item.quantity.toString(),
                    onValueChange = { text ->
                        onChange(item.copy(quantity = (text.toIntOrNull() ?: 1).coerceAtLeast(1)))
                    },
                    allowDecimal = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "نرخ هر گرم (این قلم)",
                    value = if (item.pricePerGram == 0.0) "" else item.pricePerGram.toString(),
                    onValueChange = { text -> onChange(item.copy(pricePerGram = text.toDoubleOrNull() ?: 0.0)) },
                    suffix = "تومان",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val suggested = PricingCalculator.pricePerGramForKarat(goldPricePerGram, item.karat)
                    onChange(item.copy(pricePerGram = suggested))
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "محاسبه خودکار نرخ بر اساس عیار")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(
                    label = "اجرت",
                    value = item.laborPercent.toString(),
                    onValueChange = { text -> onChange(item.copy(laborPercent = text.toDoubleOrNull() ?: 0.0)) },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "سود",
                    value = item.profitPercent.toString(),
                    onValueChange = { text -> onChange(item.copy(profitPercent = text.toDoubleOrNull() ?: 0.0)) },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "مالیات",
                    value = item.taxPercent.toString(),
                    onValueChange = { text -> onChange(item.copy(taxPercent = text.toDoubleOrNull() ?: 0.0)) },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            SummaryRow("قیمت پایه", "${result.baseAmount.formatToman()} تومان")
            SummaryRow("اجرت", "${result.laborAmount.formatToman()} تومان")
            SummaryRow("سود", "${result.profitAmount.formatToman()} تومان")
            SummaryRow("مالیات", "${result.taxAmount.formatToman()} تومان")
            SummaryRow("جمع این قلم", "${result.lineTotal.formatToman()} تومان", emphasize = true)
        }
    }
}

@Composable
private fun ProductPickerDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelect: (Product) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب کالا از انبار") },
        text = {
            if (products.isEmpty()) {
                Text("کالایی در انبار ثبت نشده است.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    products.forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "عیار ${product.karat} · ${product.weightGrams.formatWeight()} گرم · موجودی ${product.quantityInStock}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(onClick = { onSelect(product) }, label = { Text("افزودن") })
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
