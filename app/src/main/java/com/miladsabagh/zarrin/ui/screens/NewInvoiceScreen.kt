package com.miladsabagh.zarrin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.TextButton
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
import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.data.db.CustomerEntity
import com.miladsabagh.zarrin.data.db.ProductEntity
import com.miladsabagh.zarrin.ui.CartLine
import com.miladsabagh.zarrin.ui.NewInvoiceViewModel
import com.miladsabagh.zarrin.ui.components.LabeledAmountRow
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.formatGram
import com.miladsabagh.zarrin.util.formatToman
import com.miladsabagh.zarrin.util.parseAmountToLong
import com.miladsabagh.zarrin.util.parseWeightToDouble
import com.miladsabagh.zarrin.util.toPersianDigits

@Composable
fun NewInvoiceScreen(
    viewModel: NewInvoiceViewModel,
    onSaved: (Long) -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val savedInvoiceId by viewModel.savedInvoiceId.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(savedInvoiceId) {
        savedInvoiceId?.let { onSaved(it) }
    }

    NewInvoiceContent(
        settings = settings,
        customers = customers,
        products = products,
        selectedCustomer = selectedCustomer,
        lines = lines,
        error = error,
        onSelectCustomer = { viewModel.selectCustomer(it) },
        onAddProduct = { viewModel.addProduct(it) },
        onAddManual = { title, karat, weight, wage -> viewModel.addManual(title, karat, weight, wage) },
        onRemoveLine = { viewModel.removeLine(it) },
        onSaveInvoice = { viewModel.saveInvoice() },
        onErrorShown = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceContent(
    settings: ShopSettings,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    selectedCustomer: CustomerEntity?,
    lines: List<CartLine>,
    error: String?,
    onSelectCustomer: (CustomerEntity?) -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onAddManual: (String, Int, Double, Long) -> Unit,
    onRemoveLine: (Int) -> Unit,
    onSaveInvoice: () -> Unit,
    onErrorShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = { ZarrinTopBar(title = "صدور فاکتور جدید") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("مشتری", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            selectedCustomer?.name ?: "مشتری حضوری",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        selectedCustomer?.let {
                            Text(it.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(onClick = { showCustomerPicker = true }) {
                        Icon(Icons.Filled.Person, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("انتخاب")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("نرخ روز طلای ۱۸ عیار", style = MaterialTheme.typography.bodyMedium)
                    Text(settings.goldPricePerGram18k.formatToman(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showItemSheet = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("افزودن قلم")
                }
            }

            if (lines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "هنوز قلمی اضافه نشده است",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                lines.forEachIndexed { index, line ->
                    val breakdown = line.breakdown(settings)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "عیار ${line.karat} • ${line.weightGrams.formatGram()} • اجرت هر گرم ${line.wagePerGram.formatToman()}".toPersianDigits(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    breakdown.lineTotal.formatToman(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { onRemoveLine(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = "حذف قلم")
                            }
                        }
                    }
                }
            }

            if (lines.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val goldSum = lines.sumOf { it.breakdown(settings).goldValue }
                        val wageSum = lines.sumOf { it.breakdown(settings).wageAmount }
                        val profitSum = lines.sumOf { it.breakdown(settings).profitAmount }
                        val taxSum = lines.sumOf { it.breakdown(settings).taxAmount }

                        LabeledAmountRow("طلای خام", goldSum.formatToman())
                        LabeledAmountRow("اجرت ساخت", wageSum.formatToman())
                        LabeledAmountRow(
                            "سود (${settings.profitPercent.toString().toPersianDigits()}٪)",
                            profitSum.formatToman()
                        )
                        LabeledAmountRow(
                            "مالیات ارزش افزوده (${settings.taxPercent.toString().toPersianDigits()}٪)",
                            taxSum.formatToman()
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        LabeledAmountRow(
                            "مبلغ قابل پرداخت",
                            lines.sumOf { it.breakdown(settings).lineTotal }.formatToman(),
                            emphasize = true
                        )
                    }
                }

                Button(
                    onClick = onSaveInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("صدور فاکتور", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("انتخاب مشتری") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextButton(
                        onClick = {
                            onSelectCustomer(null)
                            showCustomerPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("مشتری حضوری (بدون ثبت)") }
                    HorizontalDivider()
                    customers.forEach { customer ->
                        TextButton(
                            onClick = {
                                onSelectCustomer(customer)
                                showCustomerPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(customer.name, style = MaterialTheme.typography.bodyLarge)
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerPicker = false }) { Text("بستن") }
            }
        )
    }

    if (showItemSheet) {
        ModalBottomSheet(onDismissRequest = { showItemSheet = false }) {
            ItemPickerContent(
                products = products,
                onPickProduct = { product ->
                    onAddProduct(product)
                    showItemSheet = false
                },
                onAddManual = { title, karat, weight, wage ->
                    onAddManual(title, karat, weight, wage)
                    showItemSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemPickerContent(
    products: List<ProductEntity>,
    onPickProduct: (ProductEntity) -> Unit,
    onAddManual: (String, Int, Double, Long) -> Unit
) {
    var manualMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("افزودن قلم به فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AssistChip(
                    onClick = { manualMode = !manualMode },
                    label = { Text(if (manualMode) "انتخاب از محصولات" else "قلم دستی") }
                )
            }
        }

        if (manualMode) {
            item {
                ManualItemForm(onAdd = onAddManual)
            }
        } else {
            itemsIndexed(products, key = { _, p -> p.id }) { _, product ->
                Card(
                    onClick = { onPickProduct(product) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${product.category.displayName} • عیار ${product.karat} • ${product.weightGrams.formatGram()}".toPersianDigits(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ManualItemForm(onAdd: (String, Int, Double, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf(18) }
    var weightText by remember { mutableStateOf("") }
    var wageText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان قلم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("عیار", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(18, 21, 22, 24).forEach { k ->
                FilterChip(
                    selected = karat == k,
                    onClick = { karat = k },
                    label = { Text("$k".toPersianDigits()) }
                )
            }
        }

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("وزن (گرم)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = wageText,
            onValueChange = { wageText = it },
            label = { Text("اجرت ساخت هر گرم (تومان)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                val weight = weightText.parseWeightToDouble()
                val wage = wageText.parseAmountToLong() ?: 0L
                when {
                    title.isBlank() -> error = "عنوان قلم را وارد کنید"
                    weight == null || weight <= 0 -> error = "وزن معتبر نیست"
                    wage < 0 -> error = "اجرت معتبر نیست"
                    else -> onAdd(title.trim(), karat, weight, wage)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("افزودن به فاکتور") }
    }
}
