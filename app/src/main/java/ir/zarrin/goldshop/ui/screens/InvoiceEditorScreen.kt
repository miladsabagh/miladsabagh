@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.local.InvoiceItem
import ir.zarrin.goldshop.data.local.Product
import ir.zarrin.goldshop.data.local.toLineInput
import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.model.Currency
import ir.zarrin.goldshop.domain.model.InvoiceType
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.PaymentMethod
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DropdownSelector
import ir.zarrin.goldshop.ui.components.JalaliDatePickerDialog
import ir.zarrin.goldshop.ui.components.KeyValueRow
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.ZarrinTextField
import ir.zarrin.goldshop.ui.formatCount
import ir.zarrin.goldshop.ui.formatGrams
import ir.zarrin.goldshop.ui.formatMoney
import ir.zarrin.goldshop.ui.viewmodel.InvoiceEditorViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun InvoiceEditorScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: InvoiceEditorViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var sheetItem by remember { mutableStateOf<InvoiceItem?>(null) }

    val currency = state.settings.currency

    LaunchedEffect(state.savedInvoiceId) {
        state.savedInvoiceId?.let(onSaved)
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    var rateText by remember(state.loading) {
        mutableStateOf(state.invoice.baseGoldRate.takeIf { it > 0L }?.toString().orEmpty())
    }
    var discountText by remember(state.loading) {
        mutableStateOf(state.invoice.discount.takeIf { it > 0L }?.toString().orEmpty())
    }
    var paidText by remember(state.loading) {
        mutableStateOf(state.invoice.paid.takeIf { it > 0L }?.toString().orEmpty())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (state.isNew) "صدور فاکتور جدید" else "ویرایش فاکتور")
                        Text(
                            "شماره ${PersianNumbers.toPersianDigits(state.invoice.number)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "مبلغ قابل پرداخت",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatMoney(state.totals.payable, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(onClick = viewModel::save) {
                        Text(if (state.isNew) "ثبت و صدور فاکتور" else "ذخیره تغییرات")
                    }
                }
            }
        }
    ) { padding ->
        if (state.loading) {
            Box(modifier = Modifier.padding(padding).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionCard(title = "اطلاعات فاکتور", icon = Icons.Filled.CalendarMonth) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InvoiceType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.invoice.type == type,
                                    onClick = { viewModel.setType(type) },
                                    label = { Text("فاکتور ${type.label}") }
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                                Spacer(Modifier.width(10.dp))
                                Text("تاریخ فاکتور", modifier = Modifier.weight(1f))
                                Text(
                                    state.jalaliDate.formatLong(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        AmountField(
                            label = "نرخ روز هر گرم طلای ۱۸ عیار",
                            text = rateText,
                            onTextChange = {
                                rateText = it
                                viewModel.setBaseGoldRate(PersianNumbers.parseLong(it) ?: 0L)
                            },
                            currency = currency
                        )
                    }
                }
            }

            item {
                SectionCard(title = "مشتری", icon = Icons.Filled.Person) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (state.customers.isNotEmpty()) {
                            DropdownSelector(
                                label = "انتخاب از دفتر مشتریان",
                                options = state.customers,
                                selected = state.customers.firstOrNull { it.id == state.invoice.customerId },
                                placeholder = "مشتری متفرقه",
                                optionLabel = { customer ->
                                    if (customer.phone.isBlank()) {
                                        customer.name
                                    } else {
                                        "${customer.name} — ${PersianNumbers.toPersianDigits(customer.phone)}"
                                    }
                                },
                                onSelect = viewModel::selectCustomer
                            )
                        }
                        ZarrinTextField(
                            value = state.invoice.customerName,
                            onValueChange = viewModel::setCustomerName,
                            label = "نام خریدار"
                        )
                        ZarrinTextField(
                            value = state.invoice.customerPhone,
                            onValueChange = viewModel::setCustomerPhone,
                            label = "شماره تماس",
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.addCustomer(state.invoice.customerName, state.invoice.customerPhone)
                            },
                            enabled = state.invoice.customerName.isNotBlank() && state.invoice.customerId == null
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("ذخیره در دفتر مشتریان")
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "اقلام فاکتور (${formatCount(state.items.size)})",
                    icon = Icons.Filled.Diamond
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.items.isEmpty()) {
                            Text(
                                "هنوز کالایی اضافه نشده است. از انبار انتخاب کنید یا قلم دستی بسازید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        state.items.forEachIndexed { index, item ->
                            InvoiceItemRow(
                                index = index,
                                item = item,
                                total = state.lineTotals.getOrNull(index)?.total ?: 0L,
                                currency = currency,
                                onEdit = {
                                    editingIndex = index
                                    sheetItem = item
                                },
                                onDelete = { viewModel.removeItem(index) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showProductPicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("از انبار")
                            }
                            Button(
                                onClick = {
                                    editingIndex = -1
                                    sheetItem = viewModel.newItemTemplate()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("قلم جدید")
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "پرداخت و تسویه", icon = Icons.Filled.Payments) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AmountField(
                            label = "تخفیف",
                            text = discountText,
                            onTextChange = {
                                discountText = it
                                viewModel.setDiscount(PersianNumbers.parseLong(it) ?: 0L)
                            },
                            currency = currency
                        )
                        AmountField(
                            label = "مبلغ پرداخت‌شده / بیعانه",
                            text = paidText,
                            onTextChange = {
                                paidText = it
                                viewModel.setPaid(PersianNumbers.parseLong(it) ?: 0L)
                            },
                            currency = currency
                        )
                        DropdownSelector(
                            label = "روش پرداخت",
                            options = PaymentMethod.entries,
                            selected = state.invoice.paymentMethod,
                            optionLabel = { it.label },
                            onSelect = viewModel::setPaymentMethod
                        )
                        ZarrinTextField(
                            value = state.invoice.note,
                            onValueChange = viewModel::setNote,
                            label = "توضیحات",
                            singleLine = false,
                            minLines = 2
                        )
                    }
                }
            }

            item {
                SectionCard(title = "جمع‌بندی", icon = Icons.Filled.Payments) {
                    Column {
                        KeyValueRow("ارزش طلا", formatMoney(state.totals.goldValue, currency))
                        KeyValueRow("اجرت ساخت", formatMoney(state.totals.wage, currency))
                        KeyValueRow("سود فروشنده", formatMoney(state.totals.profit, currency))
                        if (state.totals.stone > 0L) {
                            KeyValueRow("نگین و سنگ", formatMoney(state.totals.stone, currency))
                        }
                        KeyValueRow("مالیات بر ارزش افزوده", formatMoney(state.totals.tax, currency))
                        if (state.totals.discount > 0L) {
                            KeyValueRow("تخفیف", "− ${formatMoney(state.totals.discount, currency)}")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        KeyValueRow("وزن کل", formatGrams(state.totals.totalWeightGrams))
                        KeyValueRow(
                            label = "قابل پرداخت",
                            value = formatMoney(state.totals.payable, currency),
                            emphasize = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        KeyValueRow("پرداخت‌شده", formatMoney(state.totals.paid, currency))
                        KeyValueRow(
                            label = "مانده",
                            value = formatMoney(state.totals.remaining, currency),
                            emphasize = true
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initial = state.jalaliDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.setDate(date)
                showDatePicker = false
            }
        )
    }

    sheetItem?.let { item ->
        InvoiceItemSheet(
            initial = item,
            baseGoldRate = state.invoice.baseGoldRate,
            currency = currency,
            isEditing = editingIndex >= 0,
            onDismiss = { sheetItem = null },
            onConfirm = { updated ->
                if (editingIndex >= 0) viewModel.updateItem(editingIndex, updated) else viewModel.addItem(updated)
                sheetItem = null
                editingIndex = -1
            }
        )
    }

    if (showProductPicker) {
        ProductPickerSheet(
            products = state.products,
            currency = currency,
            baseGoldRate = state.invoice.baseGoldRate,
            taxPercent = state.invoice.taxPercent,
            onDismiss = { showProductPicker = false },
            onPick = { product ->
                viewModel.addItem(viewModel.itemFromProduct(product))
                showProductPicker = false
            }
        )
    }
}

@Composable
private fun InvoiceItemRow(
    index: Int,
    item: InvoiceItem,
    total: Long,
    currency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    formatCount(index + 1),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title.ifBlank { item.kind.label },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    buildString {
                        if (item.weightGrams > 0.0) {
                            append(formatGrams(item.weightGrams))
                            append(" • ")
                            append(Karat.label(item.karat).substringBefore(" ("))
                            append(" • ")
                        }
                        append("تعداد ${formatCount(item.quantity)}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatMoney(total, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductPickerSheet(
    products: List<Product>,
    currency: Currency,
    baseGoldRate: Long,
    taxPercent: Double,
    onDismiss: () -> Unit,
    onPick: (Product) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = products.filter {
        query.isBlank() || it.name.contains(query, true) || it.code.contains(query, true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Text("انتخاب کالا از انبار", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("جستجوی نام یا کد کالا") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            if (filtered.isEmpty()) {
                Text(
                    "کالایی در انبار ثبت نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.size) { position ->
                        val product = filtered[position]
                        val preview = GoldCalculator.calculateLine(
                            InvoiceItem(
                                kind = product.kind,
                                karat = product.karat,
                                weightGrams = product.weightGrams,
                                goldRatePerGram = GoldCalculator.rateForKarat(baseGoldRate, product.karat),
                                wageMode = product.wageMode,
                                wageValue = product.wageValue,
                                profitPercent = product.profitPercent,
                                stonePrice = product.stonePrice,
                                taxBasis = product.kind.defaultTaxBasis,
                                taxPercent = taxPercent,
                                unitPriceOverride = product.unitPriceOverride
                            ).toLineInput()
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(product) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        buildString {
                                            if (product.code.isNotBlank()) {
                                                append("کد ${PersianNumbers.toPersianDigits(product.code)} • ")
                                            }
                                            if (product.weightGrams > 0.0) {
                                                append(formatGrams(product.weightGrams))
                                                append(" • ")
                                            }
                                            append("موجودی ${formatCount(product.stock)}")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    formatMoney(preview.unitPrice, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
