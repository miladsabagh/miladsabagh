package com.miladsabagh.goldshop.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.data.local.entity.Product
import com.miladsabagh.goldshop.data.local.entity.ProductCategory
import com.miladsabagh.goldshop.ui.LocalViewModelFactory
import com.miladsabagh.goldshop.ui.components.NumberField
import com.miladsabagh.goldshop.ui.components.SummaryRow
import com.miladsabagh.goldshop.ui.customers.CustomerViewModel
import com.miladsabagh.goldshop.ui.products.ProductViewModel
import com.miladsabagh.goldshop.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    onBack: () -> Unit,
    onInvoiceSaved: (Long) -> Unit,
    viewModel: NewInvoiceViewModel = viewModel(factory = LocalViewModelFactory.current),
    productViewModel: ProductViewModel = viewModel(factory = LocalViewModelFactory.current),
    customerViewModel: CustomerViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCustomItemDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedInvoiceId) {
        state.savedInvoiceId?.let {
            onInvoiceSaved(it)
            viewModel.resetSavedFlag()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فاکتور جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer section
            Card {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مشخصات مشتری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showCustomerPicker = true }) {
                            Icon(Icons.Filled.PersonSearch, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("انتخاب از لیست")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.customer != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(state.customer!!.fullName, fontWeight = FontWeight.Bold)
                                Text(state.customer!!.phone, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { viewModel.selectCustomer(null) }) {
                                Icon(Icons.Filled.Close, contentDescription = "حذف انتخاب")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.walkInName,
                            onValueChange = viewModel::setWalkInName,
                            label = { Text("نام مشتری") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.walkInPhone,
                            onValueChange = viewModel::setWalkInPhone,
                            label = { Text("شماره تماس (اختیاری)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Gold price / tax section
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("نرخ طلا و مالیات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            label = "نرخ هر گرم طلا ۱۸ عيار (تومان)",
                            value = state.goldPricePerGram,
                            onValueChange = viewModel::updateGoldPrice,
                            modifier = Modifier.weight(1f)
                        )
                        NumberField(
                            label = "مالیات ارزش افزوده",
                            value = state.taxPercent,
                            onValueChange = viewModel::updateTaxPercent,
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Items
            Card {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))

                    if (state.items.isEmpty()) {
                        Text(
                            "هنوز کالایی به فاکتور اضافه نشده است",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        state.items.forEach { item ->
                            val breakdown = state.breakdownFor(item)
                            InvoiceItemRow(
                                name = item.name,
                                detail = "${item.category.displayName} • عیار ${PersianFormat.toPersianDigits(item.karat.toString())} • ${PersianFormat.formatWeight(item.weightGrams)} × ${PersianFormat.toPersianDigits(item.quantity.toString())}",
                                total = PersianFormat.formatToman(breakdown.lineTotal),
                                onDelete = { viewModel.removeItem(item.localId) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedButton(onClick = { showAddProductDialog = true }, modifier = Modifier.weight(1f)) {
                            Text("افزودن از انبار")
                        }
                        OutlinedButton(onClick = { showAddCustomItemDialog = true }, modifier = Modifier.weight(1f)) {
                            Text("افزودن قلم دستی")
                        }
                    }
                }
            }

            // Discount / paid / notes
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("تخفیف و پرداخت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            label = "تخفیف (تومان)",
                            value = state.discountAmount,
                            onValueChange = viewModel::updateDiscount,
                            modifier = Modifier.weight(1f)
                        )
                        NumberField(
                            label = "مبلغ پرداختی (تومان)",
                            value = state.paidAmount,
                            onValueChange = viewModel::updatePaidAmount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("توضیحات فاکتور") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Summary
            Card {
                Column(Modifier.padding(12.dp)) {
                    SummaryRow("مجموع وزن", PersianFormat.formatWeight(state.totalWeight))
                    SummaryRow("جمع اجناس (بدون مالیات)", PersianFormat.formatToman(state.subtotal))
                    SummaryRow("مالیات ارزش افزوده", PersianFormat.formatToman(state.totalTax))
                    SummaryRow("تخفیف", "- " + PersianFormat.formatToman(state.discountAmount))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    SummaryRow("مبلغ نهایی قابل پرداخت", PersianFormat.formatToman(state.grandTotal), emphasize = true)
                    SummaryRow("مانده حساب", PersianFormat.formatToman(state.remaining))
                }
            }

            Button(
                onClick = viewModel::saveInvoice,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("صدور و ذخیره فاکتور")
            }

            if (!state.canSave) {
                Text(
                    text = if (state.items.isEmpty()) "برای صدور فاکتور حداقل یک قلم کالا اضافه کنید" else "نرخ روز طلا را وارد کنید",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showAddProductDialog) {
        AddFromInventoryDialog(
            productViewModel = productViewModel,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { product, quantity ->
                viewModel.addItemFromProduct(product, quantity)
                showAddProductDialog = false
            }
        )
    }

    if (showAddCustomItemDialog) {
        AddCustomItemDialog(
            defaultLaborFee = 7.0,
            defaultProfit = 7.0,
            onDismiss = { showAddCustomItemDialog = false },
            onConfirm = { name, category, weight, karat, laborFee, profit, stone, qty ->
                viewModel.addCustomItem(name, category, weight, karat, laborFee, profit, stone, qty)
                showAddCustomItemDialog = false
            }
        )
    }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customerViewModel = customerViewModel,
            onDismiss = { showCustomerPicker = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerPicker = false
            }
        )
    }
}

@Composable
private fun InvoiceItemRow(name: String, detail: String, total: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Text(total, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "حذف قلم", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFromInventoryDialog(
    productViewModel: ProductViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Product, Int) -> Unit
) {
    val state by productViewModel.uiState.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن کالا از انبار") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = productViewModel::onQueryChange,
                    label = { Text("جستجو") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.height(240.dp)) {
                    if (state.products.isEmpty()) {
                        Text("کالایی یافت نشد", color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyColumn {
                            items(state.products, key = { it.id }) { product ->
                                val isSelected = selected?.id == product.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = isSelected,
                                        onClick = { selected = product }
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${PersianFormat.formatWeight(product.weightGrams)} • موجودی ${PersianFormat.toPersianDigits(product.quantity.toString())}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (selected != null) {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = quantity.toString(),
                        onValueChange = { quantity = PersianFormat.toEnglishDigits(it).filter { c -> c.isDigit() }.toIntOrNull() ?: 1 },
                        label = { Text("تعداد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let { onConfirm(it, quantity.coerceAtLeast(1)) } },
                enabled = selected != null
            ) { Text("افزودن") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomItemDialog(
    defaultLaborFee: Double,
    defaultProfit: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, ProductCategory, Double, Int, Double, Double, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.RING) }
    var weight by remember { mutableStateOf(0.0) }
    var karat by remember { mutableStateOf(18) }
    var laborFee by remember { mutableStateOf(defaultLaborFee) }
    var profit by remember { mutableStateOf(defaultProfit) }
    var stone by remember { mutableStateOf(0.0) }
    var quantity by remember { mutableStateOf(1) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن قلم دستی") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام کالا") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ProductCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
                NumberField(label = "وزن (گرم)", value = weight, onValueChange = { weight = it })
                OutlinedTextField(
                    value = PersianFormat.toPersianDigits(karat.toString()),
                    onValueChange = { karat = PersianFormat.toEnglishDigits(it).filter { c -> c.isDigit() }.toIntOrNull() ?: 18 },
                    label = { Text("عيار") },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(label = "اجرت ساخت (%)", value = laborFee, onValueChange = { laborFee = it })
                NumberField(label = "سود فروشنده (%)", value = profit, onValueChange = { profit = it })
                NumberField(label = "قیمت سنگ (تومان)", value = stone, onValueChange = { stone = it })
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = PersianFormat.toEnglishDigits(it).filter { c -> c.isDigit() }.toIntOrNull() ?: 1 },
                    label = { Text("تعداد") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, category, weight, karat, laborFee, profit, stone, quantity.coerceAtLeast(1)) },
                enabled = name.isNotBlank() && weight > 0
            ) { Text("افزودن") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun CustomerPickerDialog(
    customerViewModel: CustomerViewModel,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    val state by customerViewModel.uiState.collectAsStateWithLifecycle()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب مشتری") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = customerViewModel::onQueryChange,
                    label = { Text("جستجو") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.height(280.dp)) {
                    if (state.customers.isEmpty()) {
                        Text("مشتری‌ای یافت نشد", color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyColumn {
                            items(state.customers, key = { it.id }) { customer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(customer.fullName, fontWeight = FontWeight.Bold)
                                        if (customer.phone.isNotBlank()) {
                                            Text(customer.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                    TextButton(onClick = { onSelect(customer) }) {
                                        Text("انتخاب")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}
