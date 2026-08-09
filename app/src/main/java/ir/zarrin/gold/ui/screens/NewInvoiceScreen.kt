package ir.zarrin.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
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
import ir.zarrin.gold.data.Customer
import ir.zarrin.gold.data.KARATS
import ir.zarrin.gold.data.Product
import ir.zarrin.gold.domain.PriceBreakdown
import ir.zarrin.gold.ui.AppViewModel
import ir.zarrin.gold.ui.DraftLine
import ir.zarrin.gold.ui.components.AppTextField
import ir.zarrin.gold.ui.components.KeyValueRow
import ir.zarrin.gold.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val lines = remember { mutableListOf<DraftLine>().toMutableStateList() }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var guestName by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomItem by remember { mutableStateOf(false) }
    var pickingProduct by remember { mutableStateOf<Product?>(null) }

    val discount = PersianFormat.parseLong(discountText) ?: 0L
    val sum = lines.fold(PriceBreakdown.ZERO) { acc, l -> acc + l.breakdown }
    val payable = (sum.total - discount).coerceAtLeast(0)
    val canSave = lines.isNotEmpty() && settings.goldPricePerGram18k > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فاکتور جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        viewModel.saveInvoice(
                            customer = selectedCustomer,
                            guestName = guestName,
                            lines = lines.toList(),
                            discount = discount,
                            onSaved = onSaved,
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("صدور فاکتور — " + PersianFormat.formatCurrency(payable))
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (settings.goldPricePerGram18k <= 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        "قیمت روز طلا تعیین نشده است. ابتدا از داشبورد قیمت هر گرم طلای ۱۸ عیار را وارد کنید.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            // انتخاب مشتری
            Card(onClick = { showCustomerPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("خریدار", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            selectedCustomer?.name
                                ?: guestName.ifBlank { "مشتری متفرقه (برای انتخاب بزنید)" },
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }

            // اقلام فاکتور
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { showCustomItem = true }) { Text("قلم دستی") }
                    Button(onClick = { showProductPicker = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text(" از محصولات")
                    }
                }
            }

            if (lines.isEmpty()) {
                Text(
                    "هنوز قلمی اضافه نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            lines.forEachIndexed { index, line ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "عیار ${PersianFormat.toPersianDigits(line.karat.toString())} • " +
                                    PersianFormat.formatWeight(line.weightGrams) +
                                    " • تعداد ${PersianFormat.formatNumber(line.quantity.toLong())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                PersianFormat.formatCurrency(line.breakdown.total),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        IconButton(onClick = { lines.removeAt(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف قلم",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            AppTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = "تخفیف",
                numeric = true,
                suffix = "تومان",
            )

            // جمع فاکتور
            if (lines.isNotEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        KeyValueRow("جمع بهای طلا", PersianFormat.formatCurrency(sum.goldValue))
                        KeyValueRow("جمع اجرت ساخت", PersianFormat.formatCurrency(sum.wage))
                        KeyValueRow("سود فروشنده", PersianFormat.formatCurrency(sum.profit))
                        KeyValueRow("مالیات بر ارزش افزوده", PersianFormat.formatCurrency(sum.tax))
                        if (discount > 0) {
                            KeyValueRow("تخفیف", "− " + PersianFormat.formatCurrency(discount))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        KeyValueRow("مبلغ قابل پرداخت", PersianFormat.formatCurrency(payable), emphasize = true)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // انتخاب مشتری
    if (showCustomerPicker) {
        ModalBottomSheet(onDismissRequest = { showCustomerPicker = false }) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    "انتخاب خریدار",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Column(Modifier.padding(horizontal = 16.dp)) {
                    AppTextField(
                        value = guestName,
                        onValueChange = { guestName = it; selectedCustomer = null },
                        label = "نام مشتری متفرقه",
                    )
                }
                LazyColumn {
                    items(customers, key = { it.id }) { customer ->
                        ListItem(
                            headlineContent = { Text(customer.name) },
                            supportingContent = {
                                if (customer.phone.isNotBlank()) {
                                    Text(PersianFormat.toPersianDigits(customer.phone))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            leadingContent = {
                                FilterChip(
                                    selected = selectedCustomer?.id == customer.id,
                                    onClick = {
                                        selectedCustomer = customer
                                        showCustomerPicker = false
                                    },
                                    label = { Text("انتخاب") },
                                )
                            },
                        )
                    }
                }
                TextButton(
                    onClick = { showCustomerPicker = false },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { Text("تأیید") }
            }
        }
    }

    // انتخاب محصول
    if (showProductPicker) {
        ModalBottomSheet(onDismissRequest = { showProductPicker = false }) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    "افزودن از محصولات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (products.isEmpty()) {
                    Text(
                        "محصولی ثبت نشده است. از تب «محصولات» اضافه کنید یا از «قلم دستی» استفاده کنید.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyColumn {
                    items(products, key = { it.id }) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = {
                                Text(
                                    "عیار ${PersianFormat.toPersianDigits(product.karat.toString())} • " +
                                        PersianFormat.formatWeight(product.weightGrams) +
                                        " • موجودی ${PersianFormat.formatNumber(product.stock.toLong())}"
                                )
                            },
                            trailingContent = {
                                FilledTonalButton(onClick = {
                                    pickingProduct = product
                                    showProductPicker = false
                                }) { Text("افزودن") }
                            },
                        )
                    }
                }
            }
        }
    }

    // تعیین تعداد برای محصول انتخابی
    pickingProduct?.let { product ->
        QuantityDialog(
            product = product,
            onDismiss = { pickingProduct = null },
            onConfirm = { qty ->
                lines.add(
                    viewModel.priceLine(
                        name = product.name,
                        karat = product.karat,
                        weightGrams = product.weightGrams,
                        wagePercent = product.wagePercent,
                        quantity = qty,
                        product = product,
                    )
                )
                pickingProduct = null
            },
        )
    }

    // قلم دستی
    if (showCustomItem) {
        CustomItemDialog(
            onDismiss = { showCustomItem = false },
            onConfirm = { name, karat, weight, wage, qty ->
                lines.add(
                    viewModel.priceLine(
                        name = name, karat = karat, weightGrams = weight,
                        wagePercent = wage, quantity = qty,
                    )
                )
                showCustomItem = false
            },
        )
    }
}

@Composable
private fun QuantityDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var qtyText by remember { mutableStateOf("1") }
    val qty = PersianFormat.parseLong(qtyText)?.toInt()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            AppTextField(value = qtyText, onValueChange = { qtyText = it }, label = "تعداد", numeric = true)
        },
        confirmButton = {
            TextButton(
                enabled = qty != null && qty >= 1,
                onClick = { onConfirm(qty ?: 1) },
            ) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

@Composable
private fun CustomItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, karat: Int, weight: Double, wage: Double, qty: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf(18) }
    var weight by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }

    val weightVal = PersianFormat.parseDouble(weight)
    val wageVal = PersianFormat.parseDouble(wage)
    val qty = PersianFormat.parseLong(qtyText)?.toInt()
    val valid = name.isNotBlank() && weightVal != null && weightVal > 0 &&
        wageVal != null && wageVal >= 0 && qty != null && qty >= 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("قلم دستی") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it }, label = "شرح کالا")
                Text("عیار", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KARATS.forEach { k ->
                        FilterChip(
                            selected = karat == k,
                            onClick = { karat = k },
                            label = { Text(PersianFormat.toPersianDigits(k.toString())) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        AppTextField(value = weight, onValueChange = { weight = it },
                            label = "وزن", numeric = true, suffix = "گرم")
                    }
                    Box(Modifier.weight(1f)) {
                        AppTextField(value = wage, onValueChange = { wage = it },
                            label = "اجرت", numeric = true, suffix = "٪")
                    }
                }
                AppTextField(value = qtyText, onValueChange = { qtyText = it }, label = "تعداد", numeric = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onConfirm(name.trim(), karat, weightVal ?: 0.0, wageVal ?: 0.0, qty ?: 1)
                },
            ) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
