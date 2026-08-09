package ir.zarrin.goldshop.ui.newinvoice

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.CustomerEntity
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.domain.DraftItem
import ir.zarrin.goldshop.domain.Karats
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.domain.PriceBreakdown
import ir.zarrin.goldshop.domain.PriceInput
import ir.zarrin.goldshop.domain.PricingEngine
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.ProductCategory
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.LabeledRow
import ir.zarrin.goldshop.ui.components.PlainField
import ir.zarrin.goldshop.ui.components.SearchField
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.asAmount
import ir.zarrin.goldshop.ui.components.asDecimal
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: NewInvoiceViewModel = viewModel {
        NewInvoiceViewModel(
            container.invoiceRepository,
            container.customerRepository,
            container.productRepository,
            container.settingsRepository
        )
    }
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    val totals = state.totals
    val snackbarHostState = remember { SnackbarHostState() }

    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showCustomItem by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DraftItem?>(null) }
    var showRateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedInvoiceId) {
        state.savedInvoiceId?.let(onSaved)
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
        topBar = {
            TopAppBar(
                title = { Text("فاکتور فروش جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
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
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            settings.money(totals.grandTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = viewModel::save,
                        enabled = state.canSave,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("ثبت و صدور فاکتور")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(
                    title = "مشتری",
                    trailing = {
                        TextButton(onClick = { showCustomerPicker = true }) { Text("انتخاب") }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                state.form.customerName.ifBlank { "مشتری حضوری" },
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (state.form.customerPhone.isNotBlank()) {
                                Text(
                                    state.form.customerPhone.toPersianDigits(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "نرخ روز طلای ۱۸ عیار",
                    trailing = {
                        IconButton(onClick = { showRateDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش نرخ")
                        }
                    }
                ) {
                    Text(
                        settings.money(state.goldRate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "مالیات بر ارزش افزوده ${settings.taxPercent.toInt().toPersianDigits()}٪ روی اجرت و سود محاسبه می‌شود",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showProductPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("افزودن از انبار")
                    }
                    OutlinedButton(
                        onClick = { showCustomItem = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("قلم دستی")
                    }
                }
            }

            if (state.form.items.isEmpty()) {
                item {
                    SectionCard {
                        EmptyState(
                            icon = Icons.Default.Diamond,
                            title = "هنوز کالایی اضافه نشده",
                            message = "کالاها را از انبار انتخاب کنید یا یک قلم دستی وارد کنید."
                        )
                    }
                }
            } else {
                items(state.breakdowns, key = { it.first.key }) { (item, price) ->
                    DraftItemRow(
                        item = item,
                        price = price,
                        settings = settings,
                        onIncrease = { viewModel.changeQuantity(item.key, item.quantity + 1) },
                        onDecrease = { viewModel.changeQuantity(item.key, item.quantity - 1) },
                        onRemove = { viewModel.removeItem(item.key) },
                        onEdit = { editingItem = item }
                    )
                }
            }

            item {
                SectionCard(title = "جمع فاکتور") {
                    Column {
                        LabeledRow("بهای طلا", settings.money(totals.goldValue))
                        LabeledRow("اجرت ساخت", settings.money(totals.wage))
                        LabeledRow("سود فروشنده", settings.money(totals.profit))
                        if (totals.stone > 0) LabeledRow("بهای نگین و سنگ", settings.money(totals.stone))
                        LabeledRow("مالیات بر ارزش افزوده", settings.money(totals.tax))
                        if (totals.itemsDiscount > 0) {
                            LabeledRow("تخفیف ردیف‌ها", settings.money(totals.itemsDiscount))
                        }
                        LabeledRow(
                            "جمع وزن",
                            "${totals.totalWeight.formatWeight()} گرم"
                        )
                        Spacer(Modifier.height(8.dp))
                        AmountField(
                            value = if (state.form.invoiceDiscount == 0L) "" else
                                settings.display(state.form.invoiceDiscount).toString(),
                            onValueChange = {
                                viewModel.setInvoiceDiscount(
                                    it.asAmount() / settings.currency.multiplier
                                )
                            },
                            label = "تخفیف کل فاکتور",
                            suffix = settings.currencyLabel
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        LabeledRow(
                            "مبلغ قابل پرداخت",
                            settings.money(totals.grandTotal),
                            emphasize = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                SectionCard(title = "پرداخت") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PaymentMethod.entries.toList()) { method ->
                                FilterChip(
                                    selected = state.form.paymentMethod == method,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    label = { Text(method.label) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        AmountField(
                            value = settings.display(state.paidAmount).toString(),
                            onValueChange = {
                                viewModel.setPaidAmount(it.asAmount() / settings.currency.multiplier)
                            },
                            label = "مبلغ پرداخت شده",
                            suffix = settings.currencyLabel,
                            supportingText = "مانده: ${settings.money(state.remaining)}"
                        )
                        PlainField(
                            value = state.form.note,
                            onValueChange = viewModel::setNote,
                            label = "توضیحات فاکتور",
                            singleLine = false,
                            minLines = 2
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showProductPicker) {
        ModalBottomSheet(
            onDismissRequest = { showProductPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ProductPickerSheet(
                products = state.products,
                settings = settings,
                onPick = {
                    viewModel.addProduct(it)
                    showProductPicker = false
                }
            )
        }
    }

    if (showCustomerPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCustomerPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CustomerPickerSheet(
                customers = state.customers,
                onPick = {
                    viewModel.selectCustomer(it)
                    showCustomerPicker = false
                },
                onWalkIn = {
                    viewModel.selectCustomer(null)
                    showCustomerPicker = false
                },
                onCreate = { name, phone ->
                    viewModel.createCustomer(name, phone)
                    showCustomerPicker = false
                }
            )
        }
    }

    if (showCustomItem) {
        CustomItemDialog(
            settings = settings,
            onDismiss = { showCustomItem = false },
            onAdd = {
                viewModel.addCustomItem(it)
                showCustomItem = false
            }
        )
    }

    editingItem?.let { item ->
        ItemOptionsDialog(
            item = item,
            settings = settings,
            onDismiss = { editingItem = null },
            onApply = { updated ->
                viewModel.updateItem(item.key) { updated }
                editingItem = null
            }
        )
    }

    if (showRateDialog) {
        var rateText by remember { mutableStateOf(settings.display(state.goldRate).toString()) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("نرخ طلای این فاکتور") },
            text = {
                AmountField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = "نرخ هر گرم طلای ۱۸ عیار",
                    suffix = settings.currencyLabel
                )
            },
            confirmButton = {
                Button(onClick = {
                    val entered = rateText.asAmount()
                    viewModel.setGoldRate(
                        if (entered > 0) entered / settings.currency.multiplier else null
                    )
                    showRateDialog = false
                }) { Text("اعمال") }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun DraftItemRow(
    item: DraftItem,
    price: PriceBreakdown,
    settings: AppSettings,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            append(item.category.label)
                            if (item.pricingMode == PricingMode.BY_WEIGHT) {
                                append(" • ")
                                append(item.karat.toPersianDigits())
                                append(" عیار • ")
                                append(item.weightGrams.formatWeight())
                                append(" گرم")
                            } else {
                                append(" • قیمت مقطوع")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "قیمت واحد: ${settings.money(price.unitPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (price.discount > 0) {
                        Text(
                            "تخفیف: ${settings.money(price.discount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        settings.money(price.lineTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "ویرایش ردیف",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "کاهش", modifier = Modifier.size(16.dp))
                }
                Text(
                    item.quantity.toPersianDigits(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزایش", modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.weight(1f))
                if (item.availableStock > 0) {
                    Text(
                        "موجودی: ${item.availableStock.toPersianDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductPickerSheet(
    products: List<ProductEntity>,
    settings: AppSettings,
    onPick: (ProductEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(products, query) {
        if (query.isBlank()) products
        else products.filter { it.name.contains(query, true) || it.code.contains(query, true) }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("انتخاب کالا از انبار", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        SearchField(value = query, onValueChange = { query = it }, placeholder = "جستجوی کالا")
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { product ->
                val mode = PricingMode.fromName(product.pricingMode)
                val price = PricingEngine.calculate(
                    PriceInput(
                        pricingMode = mode,
                        weightGrams = product.weightGrams,
                        karat = product.karat,
                        baseRatePerGram = settings.goldRate18,
                        wagePercent = product.wagePercent,
                        profitPercent = product.profitPercent,
                        stonePrice = product.stonePrice,
                        fixedPrice = product.fixedPrice,
                        taxPercent = settings.taxPercent,
                        taxable = product.taxable
                    )
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(product) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                buildString {
                                    append(ProductCategory.fromName(product.category).label)
                                    if (mode == PricingMode.BY_WEIGHT) {
                                        append(" • ")
                                        append(product.weightGrams.formatWeight())
                                        append(" گرم")
                                    }
                                    append(" • موجودی ")
                                    append(product.stockQty.toPersianDigits())
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            settings.money(price.unitPrice),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CustomerPickerSheet(
    customers: List<CustomerEntity>,
    onPick: (CustomerEntity) -> Unit,
    onWalkIn: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    val filtered = remember(customers, query) {
        if (query.isBlank()) customers
        else customers.filter { it.name.contains(query, true) || it.phone.contains(query) }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("انتخاب مشتری", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showNew = !showNew }) {
                Text(if (showNew) "بستن" else "مشتری جدید")
            }
        }
        Spacer(Modifier.height(8.dp))

        if (showNew) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlainField(value = newName, onValueChange = { newName = it }, label = "نام مشتری")
                PlainField(
                    value = newPhone,
                    onValueChange = { newPhone = it },
                    label = "شماره تماس",
                    keyboardType = KeyboardType.Phone
                )
                Button(
                    onClick = { onCreate(newName.trim(), newPhone.trim()) },
                    enabled = newName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("ثبت و انتخاب") }
            }
            Spacer(Modifier.height(12.dp))
        }

        SearchField(value = query, onValueChange = { query = it }, placeholder = "جستجوی مشتری")
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onWalkIn,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) { Text("مشتری حضوری (بدون ثبت مشخصات)") }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { customer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(customer) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(customer.name, style = MaterialTheme.typography.titleSmall)
                        if (customer.phone.isNotBlank()) {
                            Text(
                                customer.phone.toPersianDigits(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CustomItemDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onAdd: (DraftItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(PricingMode.BY_WEIGHT) }
    var karat by remember { mutableStateOf(Karats.BASE) }
    var weight by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf(settings.defaultWagePercent.toString()) }
    var profit by remember { mutableStateOf(settings.defaultProfitPercent.toString()) }
    var stone by remember { mutableStateOf("") }
    var fixed by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن قلم دستی") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    PlainField(value = name, onValueChange = { name = it }, label = "شرح کالا")
                }
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        PricingMode.entries.forEachIndexed { index, value ->
                            SegmentedButton(
                                selected = mode == value,
                                onClick = { mode = value },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = PricingMode.entries.size
                                )
                            ) { Text(value.label) }
                        }
                    }
                }
                if (mode == PricingMode.BY_WEIGHT) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Karats.COMMON.forEach { value ->
                                FilterChip(
                                    selected = karat == value,
                                    onClick = { karat = value },
                                    label = { Text(value.toPersianDigits()) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                    item {
                        DecimalField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = "وزن",
                            suffix = "گرم"
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DecimalField(
                                value = wage,
                                onValueChange = { wage = it },
                                label = "اجرت",
                                suffix = "٪",
                                modifier = Modifier.weight(1f)
                            )
                            DecimalField(
                                value = profit,
                                onValueChange = { profit = it },
                                label = "سود",
                                suffix = "٪",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        AmountField(
                            value = stone,
                            onValueChange = { stone = it },
                            label = "بهای نگین",
                            suffix = settings.currencyLabel
                        )
                    }
                } else {
                    item {
                        AmountField(
                            value = fixed,
                            onValueChange = { fixed = it },
                            label = "قیمت مقطوع",
                            suffix = settings.currencyLabel
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onAdd(
                        DraftItem(
                            key = 0L,
                            name = name.trim(),
                            category = ProductCategory.OTHER,
                            karat = karat,
                            weightGrams = weight.asDecimal(),
                            pricingMode = mode,
                            wagePercent = wage.asDecimal(),
                            profitPercent = profit.asDecimal(),
                            stonePrice = stone.asAmount() / settings.currency.multiplier,
                            fixedPrice = fixed.asAmount() / settings.currency.multiplier,
                            taxable = true
                        )
                    )
                }
            ) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun ItemOptionsDialog(
    item: DraftItem,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onApply: (DraftItem) -> Unit
) {
    var weight by remember { mutableStateOf(if (item.weightGrams > 0) item.weightGrams.toString() else "") }
    var wage by remember { mutableStateOf(item.wagePercent.toString()) }
    var profit by remember { mutableStateOf(item.profitPercent.toString()) }
    var discount by remember {
        mutableStateOf(
            if (item.discount > 0) settings.display(item.discount).toString() else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش ردیف: ${item.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (item.pricingMode == PricingMode.BY_WEIGHT) {
                    DecimalField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = "وزن",
                        suffix = "گرم"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DecimalField(
                            value = wage,
                            onValueChange = { wage = it },
                            label = "اجرت",
                            suffix = "٪",
                            modifier = Modifier.weight(1f)
                        )
                        DecimalField(
                            value = profit,
                            onValueChange = { profit = it },
                            label = "سود",
                            suffix = "٪",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AmountField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = "تخفیف این ردیف",
                    suffix = settings.currencyLabel
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(
                    item.copy(
                        weightGrams = if (item.pricingMode == PricingMode.BY_WEIGHT) {
                            weight.asDecimal()
                        } else {
                            item.weightGrams
                        },
                        wagePercent = if (item.pricingMode == PricingMode.BY_WEIGHT) {
                            wage.asDecimal()
                        } else {
                            item.wagePercent
                        },
                        profitPercent = if (item.pricingMode == PricingMode.BY_WEIGHT) {
                            profit.asDecimal()
                        } else {
                            item.profitPercent
                        },
                        discount = discount.asAmount() / settings.currency.multiplier
                    )
                )
            }) { Text("اعمال") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
