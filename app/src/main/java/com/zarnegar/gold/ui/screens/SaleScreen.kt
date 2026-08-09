package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.domain.model.Karats
import com.zarnegar.gold.domain.model.LineBreakdown
import com.zarnegar.gold.domain.model.PaymentMethod
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.ui.components.AmountField
import com.zarnegar.gold.ui.components.EmptyState
import com.zarnegar.gold.ui.components.KeyValueRow
import com.zarnegar.gold.ui.components.SectionCard
import com.zarnegar.gold.ui.components.TextInputField
import com.zarnegar.gold.ui.vm.SaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    viewModel: SaleViewModel,
    onInvoiceCreated: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedInvoiceId by viewModel.savedInvoiceId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    val currency = state.settings.currencyLabel
    val totals = state.totals

    LaunchedEffect(savedInvoiceId) {
        savedInvoiceId?.let {
            viewModel.consumeSavedInvoice()
            onInvoiceCreated(it)
        }
    }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("صدور فاکتور فروش") },
                actions = {
                    if (state.cart.items.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "خالی کردن سبد")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (state.cart.items.isNotEmpty()) {
                SubmitBar(
                    payable = totals.payable,
                    currency = currency,
                    onSubmit = viewModel::submit,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CustomerCard(
                    customer = state.cart.customer,
                    onPick = { showCustomerPicker = true },
                    onClear = { viewModel.setCustomer(null) },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { showProductPicker = true },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("  افزودن کالا")
                    }
                }
            }

            if (totals.lines.isEmpty()) {
                item {
                    SectionCard {
                        EmptyState(
                            icon = Icons.Filled.PointOfSale,
                            title = "سبد فروش خالی است",
                            message = "برای شروع، کالاها را از انبار به فاکتور اضافه کنید.",
                        )
                    }
                }
            } else {
                itemsIndexed(totals.lines) { index, line ->
                    CartLineCard(
                        index = index,
                        line = line,
                        currency = currency,
                        onIncrease = { viewModel.changeQuantity(index, 1) },
                        onDecrease = { viewModel.changeQuantity(index, -1) },
                        onRemove = { viewModel.removeItem(index) },
                        onDiscountChange = { viewModel.setLineDiscount(index, it) },
                    )
                }
            }

            if (state.cart.items.isNotEmpty()) {
                item {
                    SectionCard(title = "تخفیف و پرداخت") {
                        AmountField(
                            label = "تخفیف کلی فاکتور",
                            value = state.cart.invoiceDiscount,
                            onValueChange = viewModel::setInvoiceDiscount,
                            suffix = currency,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("روش پرداخت", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = state.cart.paymentMethod == method,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    label = { Text(method.label) },
                                )
                            }
                        }
                        AmountField(
                            label = "مبلغ پرداخت‌شده",
                            value = state.effectivePaidAmount,
                            onValueChange = { viewModel.setPaidAmount(it) },
                            suffix = currency,
                            supportingText = "برای فروش نسیه، مبلغ کمتر از مبلغ فاکتور وارد کنید",
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextInputField(
                            label = "توضیحات فاکتور",
                            value = state.cart.note,
                            onValueChange = viewModel::setNote,
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    TotalsCard(
                        goldValue = totals.goldValueTotal,
                        wage = totals.wageTotal,
                        profit = totals.profitTotal,
                        stone = totals.stoneTotal,
                        discount = totals.discountTotal,
                        vat = totals.vatTotal,
                        vatPercent = state.settings.vatPercent,
                        rounding = totals.roundingAdjustment,
                        payable = totals.payable,
                        paid = state.effectivePaidAmount,
                        weight = totals.totalWeightGrams,
                        currency = currency,
                    )
                }
            }
        }
    }

    if (showProductPicker) {
        ProductPickerSheet(
            products = state.products,
            currency = currency,
            onDismiss = { showProductPicker = false },
            onPick = { viewModel.addProduct(it) },
        )
    }

    if (showCustomerPicker) {
        CustomerPickerSheet(
            customers = state.customers,
            onDismiss = { showCustomerPicker = false },
            onPick = {
                viewModel.setCustomer(it)
                showCustomerPicker = false
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("خالی کردن سبد") },
            text = { Text("همهٔ اقلام فاکتور حذف شوند؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clear()
                    confirmClear = false
                }) { Text("حذف همه") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmClear = false }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun CustomerCard(customer: Customer?, onPick: () -> Unit, onClear: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = customer?.fullName ?: "مشتری متفرقه",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = customer?.phone?.takeIf { it.isNotBlank() }
                            ?.let { PersianText.toPersianDigits(it) }
                            ?: "برای انتخاب مشتری ضربه بزنید",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (customer != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف مشتری")
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(
    index: Int,
    line: LineBreakdown,
    currency: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onDiscountChange: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${PersianText.formatNumber((index + 1).toLong())}. " +
                            line.item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (line.item.pricingMode == PricingMode.BY_WEIGHT) {
                            "${PersianText.formatGrams(line.item.weightGrams)} • " +
                                Karats.label(line.item.karat)
                        } else {
                            "قیمت مقطوع"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Remove, contentDescription = "کاهش")
                    }
                    Text(
                        text = PersianText.formatNumber(line.quantity.toLong()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "افزایش")
                    }
                }
                Text(
                    text = "${PersianText.formatNumber(line.total)} $currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            AssistChip(
                onClick = { expanded = !expanded },
                label = { Text(if (expanded) "بستن ریز محاسبه" else "ریز محاسبه و تخفیف") },
            )

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                if (line.item.pricingMode == PricingMode.BY_WEIGHT) {
                    KeyValueRow(
                        "نرخ هر گرم",
                        "${PersianText.formatNumber(line.ratePerGram)} $currency",
                    )
                    KeyValueRow(
                        "ارزش طلا (${PersianText.formatGrams(line.goldWeightGrams)})",
                        "${PersianText.formatNumber(line.unitGoldValue * line.quantity)} $currency",
                    )
                    KeyValueRow(
                        "اجرت ساخت",
                        "${PersianText.formatNumber(line.unitWage * line.quantity)} $currency",
                    )
                    KeyValueRow(
                        "سود فروشنده",
                        "${PersianText.formatNumber(line.unitProfit * line.quantity)} $currency",
                    )
                }
                if (line.unitStoneValue > 0) {
                    KeyValueRow(
                        "ارزش سنگ",
                        "${PersianText.formatNumber(line.unitStoneValue * line.quantity)} $currency",
                    )
                }
                KeyValueRow(
                    "مالیات بر ارزش افزوده",
                    "${PersianText.formatNumber(line.vat)} $currency",
                )
                AmountField(
                    label = "تخفیف این قلم",
                    value = line.item.discount,
                    onValueChange = onDiscountChange,
                    suffix = currency,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TotalsCard(
    goldValue: Long,
    wage: Long,
    profit: Long,
    stone: Long,
    discount: Long,
    vat: Long,
    vatPercent: Double,
    rounding: Long,
    payable: Long,
    paid: Long,
    weight: Double,
    currency: String,
) {
    SectionCard(title = "جمع‌بندی فاکتور") {
        KeyValueRow("وزن کل", PersianText.formatGrams(weight))
        KeyValueRow("ارزش طلا", "${PersianText.formatNumber(goldValue)} $currency")
        KeyValueRow("اجرت ساخت", "${PersianText.formatNumber(wage)} $currency")
        KeyValueRow("سود فروشنده", "${PersianText.formatNumber(profit)} $currency")
        if (stone > 0) {
            KeyValueRow("ارزش سنگ و مقطوع", "${PersianText.formatNumber(stone)} $currency")
        }
        if (discount > 0) {
            KeyValueRow(
                "تخفیف",
                "${PersianText.formatNumber(discount)} $currency",
                valueColor = MaterialTheme.colorScheme.error,
            )
        }
        KeyValueRow(
            "مالیات بر ارزش افزوده (${PersianText.formatPercent(vatPercent)})",
            "${PersianText.formatNumber(vat)} $currency",
        )
        if (rounding != 0L) {
            KeyValueRow("گرد کردن", "${PersianText.formatNumber(rounding)} $currency")
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        KeyValueRow(
            "مبلغ قابل پرداخت",
            "${PersianText.formatNumber(payable)} $currency",
            emphasize = true,
            valueColor = MaterialTheme.colorScheme.primary,
        )
        if (paid < payable) {
            KeyValueRow(
                "مانده",
                "${PersianText.formatNumber(payable - paid)} $currency",
                valueColor = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            text = "به حروف: ${PersianText.numberToWords(payable)} $currency",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SubmitBar(payable: Long, currency: String, onSubmit: () -> Unit) {
    androidx.compose.material3.Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "مبلغ قابل پرداخت",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${PersianText.formatNumber(payable)} $currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(onClick = onSubmit, shape = RoundedCornerShape(14.dp)) {
                Text("ثبت و صدور فاکتور")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductPickerSheet(
    products: List<Product>,
    currency: String,
    onDismiss: () -> Unit,
    onPick: (Product) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = products.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.code.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("انتخاب کالا", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.id }) { product ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(product) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(product.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (product.pricingMode == PricingMode.BY_WEIGHT) {
                                        PersianText.formatGrams(product.weightGrams)
                                    } else {
                                        "${PersianText.formatNumber(product.fixedPrice)} $currency"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "افزودن",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                item { Box(modifier = Modifier.size(24.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerPickerSheet(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onPick: (Customer?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("انتخاب مشتری", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                item {
                    OutlinedButton(
                        onClick = { onPick(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("مشتری متفرقه (بدون ثبت مشخصات)") }
                }
                items(customers, key = { it.id }) { customer ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(customer) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(customer.fullName, style = MaterialTheme.typography.bodyLarge)
                            if (customer.phone.isNotBlank()) {
                                Text(
                                    text = PersianText.toPersianDigits(customer.phone),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                item { Box(modifier = Modifier.size(24.dp)) }
            }
        }
    }
}
