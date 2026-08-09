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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import ir.zarrin.goldshop.data.local.Product
import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.WageMode
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.DropdownSelector
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.KeyValueRow
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.ZarrinTextField
import ir.zarrin.goldshop.ui.formatCount
import ir.zarrin.goldshop.ui.formatGrams
import ir.zarrin.goldshop.ui.formatMoney
import ir.zarrin.goldshop.ui.viewmodel.ProductEditorViewModel
import ir.zarrin.goldshop.ui.viewmodel.ProductListViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun ProductListScreen(
    bottomBar: @Composable () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    viewModel: ProductListViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = { Text("انبار کالا") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("کالای جدید") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("جستجوی نام یا کد کالا") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (state.products.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Diamond,
                    title = "انبار خالی است",
                    subtitle = "کالاهای پرفروش را ثبت کنید تا هنگام صدور فاکتور با یک لمس اضافه شوند."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.products, key = { it.id }) { product ->
                        val preview = GoldCalculator.calculateLine(
                            ir.zarrin.goldshop.domain.LineInput(
                                kind = product.kind,
                                karat = product.karat,
                                weightGrams = product.weightGrams,
                                goldRatePerGram = GoldCalculator.rateForKarat(
                                    state.settings.goldRate18,
                                    product.karat
                                ),
                                wageMode = product.wageMode,
                                wageValue = product.wageValue,
                                profitPercent = product.profitPercent,
                                stonePrice = product.stonePrice,
                                taxBasis = product.kind.defaultTaxBasis,
                                taxPercent = state.settings.taxPercent,
                                unitPriceOverride = product.unitPriceOverride
                            )
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditProduct(product.id) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Diamond,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        buildString {
                                            if (product.code.isNotBlank()) {
                                                append("کد ${PersianNumbers.toPersianDigits(product.code)} • ")
                                            }
                                            append(product.kind.label)
                                            if (product.weightGrams > 0.0) {
                                                append(" • ${formatGrams(product.weightGrams)}")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "قیمت روز: ${formatMoney(preview.unitPrice, state.settings.currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "موجودی",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatCount(product.stock),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { pendingDelete = product }) {
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
                }
            }
        }
    }

    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف کالا") },
            text = { Text("«${product.name}» از انبار حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(product)
                    pendingDelete = null
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("انصراف") } }
        )
    }
}

@Composable
fun ProductEditorScreen(
    onBack: () -> Unit,
    viewModel: ProductEditorViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "کالای جدید" else "ویرایش کالا") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        key(state.product.id) {
            ProductForm(
                initial = state.product,
                goldRate18 = state.settings.goldRate18,
                taxPercent = state.settings.taxPercent,
                currency = state.settings.currency,
                modifier = Modifier.padding(padding),
                onSave = viewModel::save
            )
        }
    }
}

@Composable
private fun ProductForm(
    initial: Product,
    goldRate18: Long,
    taxPercent: Double,
    currency: ir.zarrin.goldshop.domain.model.Currency,
    modifier: Modifier = Modifier,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var code by remember { mutableStateOf(initial.code) }
    var kind by remember { mutableStateOf(initial.kind) }
    var karat by remember { mutableIntStateOf(initial.karat) }
    var weightText by remember { mutableStateOf(if (initial.weightGrams == 0.0) "" else initial.weightGrams.toString()) }
    var wageMode by remember { mutableStateOf(initial.wageMode) }
    var wageText by remember { mutableStateOf(if (initial.wageValue == 0.0) "" else initial.wageValue.toString()) }
    var profitText by remember { mutableStateOf(if (initial.profitPercent == 0.0) "" else initial.profitPercent.toString()) }
    var stoneText by remember { mutableStateOf(if (initial.stonePrice == 0L) "" else initial.stonePrice.toString()) }
    var unitPriceText by remember { mutableStateOf(initial.unitPriceOverride?.toString().orEmpty()) }
    var stockCount by remember { mutableIntStateOf(initial.stock) }
    var note by remember { mutableStateOf(initial.note) }

    val pricedPerPiece = kind.isPricedPerPiece
    val product = initial.copy(
        name = name.trim(),
        code = code.trim(),
        kind = kind,
        karat = karat,
        weightGrams = PersianNumbers.parseDouble(weightText) ?: 0.0,
        wageMode = wageMode,
        wageValue = PersianNumbers.parseDouble(wageText) ?: 0.0,
        profitPercent = PersianNumbers.parseDouble(profitText) ?: 0.0,
        stonePrice = PersianNumbers.parseLong(stoneText) ?: 0L,
        unitPriceOverride = if (pricedPerPiece) PersianNumbers.parseLong(unitPriceText) else null,
        stock = stockCount,
        note = note
    )
    val preview = GoldCalculator.calculateLine(
        ir.zarrin.goldshop.domain.LineInput(
            kind = product.kind,
            karat = product.karat,
            weightGrams = product.weightGrams,
            goldRatePerGram = GoldCalculator.rateForKarat(goldRate18, product.karat),
            wageMode = product.wageMode,
            wageValue = product.wageValue,
            profitPercent = product.profitPercent,
            stonePrice = product.stonePrice,
            taxBasis = product.kind.defaultTaxBasis,
            taxPercent = taxPercent,
            unitPriceOverride = product.unitPriceOverride
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "مشخصات کالا", icon = Icons.Filled.Diamond) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZarrinTextField(value = name, onValueChange = { name = it }, label = "نام کالا")
                ZarrinTextField(value = code, onValueChange = { code = it }, label = "کد کالا")
                DropdownSelector(
                    label = "نوع کالا",
                    options = ItemKind.entries,
                    selected = kind,
                    optionLabel = { it.label },
                    onSelect = { kind = it }
                )
                if (!pricedPerPiece) {
                    DropdownSelector(
                        label = "عیار",
                        options = Karat.COMMON.map { it.first },
                        selected = karat,
                        optionLabel = { Karat.label(it) },
                        onSelect = { karat = it }
                    )
                    DecimalField(
                        label = "وزن",
                        text = weightText,
                        onTextChange = { weightText = it },
                        suffix = "گرم"
                    )
                } else {
                    AmountField(
                        label = "قیمت واحد",
                        text = unitPriceText,
                        onTextChange = { unitPriceText = it },
                        currency = currency
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("موجودی", modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (stockCount > 0) stockCount -= 1 }) { Text("−") }
                    Text(formatCount(stockCount), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { stockCount += 1 }) { Text("+") }
                }
            }
        }

        if (!pricedPerPiece) {
            SectionCard(title = "اجرت و سود", icon = Icons.Filled.Diamond) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DropdownSelector(
                        label = "نحوه اجرت",
                        options = WageMode.entries,
                        selected = wageMode,
                        optionLabel = { it.label },
                        onSelect = { wageMode = it }
                    )
                    DecimalField(
                        label = "مقدار اجرت",
                        text = wageText,
                        onTextChange = { wageText = it },
                        suffix = wageMode.unit
                    )
                    DecimalField(
                        label = "درصد سود فروشنده",
                        text = profitText,
                        onTextChange = { profitText = it },
                        suffix = "٪"
                    )
                    AmountField(
                        label = "قیمت نگین و سنگ",
                        text = stoneText,
                        onTextChange = { stoneText = it },
                        currency = currency
                    )
                }
            }
        }

        SectionCard(title = "قیمت روز این کالا", icon = Icons.Filled.Diamond) {
            Column {
                KeyValueRow("ارزش طلا", formatMoney(preview.goldValue, currency))
                KeyValueRow("اجرت ساخت", formatMoney(preview.wage, currency))
                KeyValueRow("سود فروشنده", formatMoney(preview.profit, currency))
                KeyValueRow("مالیات", formatMoney(preview.tax, currency))
                KeyValueRow(
                    label = "قیمت فروش",
                    value = formatMoney(preview.unitPrice, currency),
                    emphasize = true,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        ZarrinTextField(
            value = note,
            onValueChange = { note = it },
            label = "توضیحات",
            singleLine = false,
            minLines = 2
        )

        Button(
            onClick = { onSave(product) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره کالا", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
    }
}
