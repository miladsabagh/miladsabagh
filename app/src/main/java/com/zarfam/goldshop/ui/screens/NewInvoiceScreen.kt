package com.zarfam.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.data.db.Customer
import com.zarfam.goldshop.data.db.Product
import com.zarfam.goldshop.domain.InvoiceCalculator
import com.zarfam.goldshop.domain.formatWeight
import com.zarfam.goldshop.domain.parseDecimal
import com.zarfam.goldshop.domain.parseMoney
import com.zarfam.goldshop.domain.toEnglishDigits
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.domain.toMoneyToman
import com.zarfam.goldshop.domain.toPersianDigits
import com.zarfam.goldshop.ui.components.AppTextField
import com.zarfam.goldshop.ui.components.PriceRow
import com.zarfam.goldshop.ui.viewmodel.DraftItem
import com.zarfam.goldshop.ui.viewmodel.NewInvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: NewInvoiceViewModel = viewModel(factory = NewInvoiceViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()

    var goldPriceText by remember { mutableStateOf("") }
    var priceInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(settings.goldPricePerGram18) {
        if (!priceInitialized && settings.goldPricePerGram18 > 0) {
            goldPriceText = settings.goldPricePerGram18.toString()
            priceInitialized = true
        }
    }

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerExpanded by remember { mutableStateOf(false) }
    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }

    val items = remember { mutableListOf<DraftItem>().toMutableStateList() }
    var discountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showProductPicker by remember { mutableStateOf(false) }
    var showManualItem by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val goldPrice = goldPriceText.parseMoney() ?: 0L
    val discount = discountText.parseMoney() ?: 0L
    val taxPercent = settings.taxPercent

    val computed = viewModel.computeItems(items, goldPrice, taxPercent)
    val itemsTotal = computed.sumOf { it.second.lineTotal }
    val rawTotal = computed.sumOf { it.second.rawGoldValue }
    val wageTotal = computed.sumOf { it.second.wageAmount }
    val profitTotal = computed.sumOf { it.second.profitAmount }
    val taxTotal = computed.sumOf { it.second.taxAmount }
    val grandTotal = InvoiceCalculator.grandTotal(itemsTotal, discount)

    val canSave = items.isNotEmpty() && goldPrice > 0 && !saving

    if (showProductPicker) {
        ProductPickerDialog(
            products = products,
            goldPrice = goldPrice,
            taxPercent = taxPercent,
            onDismiss = { showProductPicker = false },
            onPick = { product ->
                items.add(
                    DraftItem(
                        productId = product.id,
                        name = product.name,
                        weightGrams = product.weightGrams,
                        karat = product.karat,
                        wagePercent = product.wagePercent,
                        profitPercent = product.profitPercent,
                        quantity = 1,
                    ),
                )
                showProductPicker = false
            },
        )
    }

    if (showManualItem) {
        ManualItemDialog(
            onDismiss = { showManualItem = false },
            onAdd = {
                items.add(it)
                showManualItem = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صدور فاکتور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTextField(
                value = goldPriceText,
                onValueChange = { goldPriceText = it },
                label = "قیمت هر گرم طلای ۱۸ عیار *",
                numeric = true,
                suffix = "تومان",
                isError = goldPrice <= 0,
                supportingText = if (goldPrice > 0) "${goldPrice.toMoney()} تومان" else "قیمت روز طلا را وارد کنید",
            )

            ExposedDropdownMenuBox(
                expanded = customerExpanded,
                onExpandedChange = { customerExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "مشتری مهمان",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("مشتری") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.medium,
                )
                ExposedDropdownMenu(
                    expanded = customerExpanded,
                    onDismissRequest = { customerExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("مشتری مهمان") },
                        onClick = {
                            selectedCustomer = null
                            customerExpanded = false
                        },
                    )
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                selectedCustomer = customer
                                customerExpanded = false
                            },
                        )
                    }
                }
            }

            if (selectedCustomer == null) {
                AppTextField(guestName, { guestName = it }, "نام مشتری")
                AppTextField(guestPhone, { guestPhone = it }, "تلفن مشتری", numeric = true)
            }

            HorizontalDivider()

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showManualItem = true }) {
                        Text("افزودن دستی")
                    }
                    Button(onClick = { showProductPicker = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("از محصولات")
                    }
                }
            }

            if (items.isEmpty()) {
                Text(
                    "هنوز قلمی اضافه نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            computed.forEachIndexed { index, (draft, amounts) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                draft.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "${draft.weightGrams.formatWeight()} گرم | عیار ${draft.karat.toString().toPersianDigits()} | تعداد ${draft.quantity.toString().toPersianDigits()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                if (goldPrice > 0) amounts.lineTotal.toMoneyToman() else "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(onClick = { items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف قلم", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            HorizontalDivider()

            AppTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = "تخفیف",
                numeric = true,
                suffix = "تومان",
            )
            AppTextField(note, { note = it }, "توضیحات (اختیاری)", singleLine = false)

            if (items.isNotEmpty() && goldPrice > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        PriceRow("ارزش طلای خام", rawTotal.toMoneyToman())
                        PriceRow("اجرت ساخت", wageTotal.toMoneyToman())
                        PriceRow("سود فروشنده", profitTotal.toMoneyToman())
                        PriceRow("مالیات (٪${taxPercent.formatWeight()})", taxTotal.toMoneyToman())
                        if (discount > 0) PriceRow("تخفیف", "${discount.toMoney()}- تومان")
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        PriceRow("مبلغ قابل پرداخت", grandTotal.toMoneyToman(), highlight = true)
                    }
                }
            }

            Button(
                onClick = {
                    saving = true
                    viewModel.saveInvoice(
                        customer = selectedCustomer,
                        guestName = guestName,
                        guestPhone = guestPhone,
                        items = items.toList(),
                        goldPrice = goldPrice,
                        taxPercent = taxPercent,
                        discount = discount,
                        note = note.trim(),
                        onSaved = onSaved,
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(if (saving) "در حال ثبت..." else "ثبت و صدور فاکتور", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProductPickerDialog(
    products: List<Product>,
    goldPrice: Long,
    taxPercent: Double,
    onDismiss: () -> Unit,
    onPick: (Product) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب محصول") },
        text = {
            if (products.isEmpty()) {
                Text("محصولی ثبت نشده است. ابتدا از بخش محصولات اضافه کنید.")
            } else {
                LazyColumn(
                    Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(products, key = { it.id }) { product ->
                        Card(
                            onClick = { onPick(product) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${product.weightGrams.formatWeight()} گرم | عیار ${product.karat.toString().toPersianDigits()} | موجودی ${product.stockCount.toString().toPersianDigits()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (goldPrice > 0) {
                                    val amounts = InvoiceCalculator.calculateLine(
                                        pricePerGram18 = goldPrice,
                                        weightGrams = product.weightGrams,
                                        karat = product.karat,
                                        wagePercent = product.wagePercent,
                                        profitPercent = product.profitPercent,
                                        taxPercent = taxPercent,
                                    )
                                    Text(
                                        amounts.lineTotal.toMoney(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
    )
}

@Composable
private fun ManualItemDialog(
    onDismiss: () -> Unit,
    onAdd: (DraftItem) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var karatText by remember { mutableStateOf("18") }
    var wage by remember { mutableStateOf("10") }
    var profit by remember { mutableStateOf("7") }
    var qty by remember { mutableStateOf("1") }

    val weightVal = weight.parseDecimal()
    val karatVal = karatText.toEnglishDigits().trim().toIntOrNull()
    val valid = name.isNotBlank() && weightVal != null && weightVal > 0 && karatVal != null && karatVal > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن قلم دستی") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(name, { name = it }, "شرح کالا *")
                AppTextField(weight, { weight = it }, "وزن *", decimal = true, suffix = "گرم")
                AppTextField(karatText, { karatText = it }, "عیار", numeric = true)
                AppTextField(wage, { wage = it }, "اجرت ساخت", decimal = true, suffix = "٪")
                AppTextField(profit, { profit = it }, "سود فروش", decimal = true, suffix = "٪")
                AppTextField(qty, { qty = it }, "تعداد", numeric = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onAdd(
                        DraftItem(
                            name = name.trim(),
                            weightGrams = weightVal ?: 0.0,
                            karat = karatVal ?: 18,
                            wagePercent = wage.parseDecimal() ?: 0.0,
                            profitPercent = profit.parseDecimal() ?: 0.0,
                            quantity = (qty.toEnglishDigits().trim().toIntOrNull() ?: 1).coerceAtLeast(1),
                        ),
                    )
                },
            ) { Text("افزودن") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
