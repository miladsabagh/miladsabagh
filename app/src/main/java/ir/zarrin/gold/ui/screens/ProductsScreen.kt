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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarrin.gold.data.KARATS
import ir.zarrin.gold.data.PRODUCT_CATEGORIES
import ir.zarrin.gold.data.Product
import ir.zarrin.gold.domain.GoldPricing
import ir.zarrin.gold.ui.AppViewModel
import ir.zarrin.gold.ui.components.AppTextField
import ir.zarrin.gold.ui.components.ConfirmDialog
import ir.zarrin.gold.ui.components.EmptyState
import ir.zarrin.gold.util.PersianFormat

@Composable
fun ProductsScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var editing by remember { mutableStateOf<Product?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("محصول جدید") },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "محصولات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            if (products.isEmpty()) {
                EmptyState("محصولی ثبت نشده است. با دکمه «محصول جدید» شروع کنید.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(products, key = { it.id }) { product ->
                        val price = if (settings.goldPricePerGram18k > 0) {
                            GoldPricing.calculate(
                                pricePerGram18k = settings.goldPricePerGram18k,
                                weightGrams = product.weightGrams,
                                karat = product.karat,
                                wagePercent = product.wagePercent,
                                profitPercent = settings.profitPercent,
                                taxPercent = settings.taxPercent,
                            ).total
                        } else null

                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "${product.category} • عیار ${PersianFormat.toPersianDigits(product.karat.toString())} • " +
                                            PersianFormat.formatWeight(product.weightGrams) +
                                            " • اجرت ${PersianFormat.toPersianDigits(product.wagePercent.toString().removeSuffix(".0"))}٪",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        (price?.let { "قیمت روز: " + PersianFormat.formatCurrency(it) }
                                            ?: "قیمت روز طلا تعیین نشده") +
                                            "  •  موجودی: ${PersianFormat.formatNumber(product.stock.toLong())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (product.stock > 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                    )
                                }
                                IconButton(onClick = { editing = product; showEditor = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = { deleting = product }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showEditor) {
        ProductEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = {
                viewModel.saveProduct(it)
                showEditor = false
            },
        )
    }
    deleting?.let { product ->
        ConfirmDialog(
            title = "حذف محصول",
            message = "«${product.name}» حذف شود؟",
            onConfirm = { viewModel.deleteProduct(product); deleting = null },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
fun ProductEditorDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: PRODUCT_CATEGORIES.first()) }
    var karat by remember { mutableStateOf(initial?.karat ?: 18) }
    var weight by remember { mutableStateOf(initial?.weightGrams?.toString().orEmpty()) }
    var wage by remember { mutableStateOf(initial?.wagePercent?.toString()?.removeSuffix(".0").orEmpty()) }
    var stock by remember { mutableStateOf(initial?.stock?.toString() ?: "1") }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }

    val weightVal = PersianFormat.parseDouble(weight)
    val wageVal = PersianFormat.parseDouble(wage)
    val stockVal = PersianFormat.parseLong(stock)?.toInt()
    val valid = name.isNotBlank() && weightVal != null && weightVal > 0 &&
        wageVal != null && wageVal >= 0 && stockVal != null && stockVal >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "محصول جدید" else "ویرایش محصول") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(value = name, onValueChange = { name = it }, label = "نام محصول")
                Text("دسته‌بندی", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PRODUCT_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                        )
                    }
                }
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
                        AppTextField(
                            value = weight, onValueChange = { weight = it },
                            label = "وزن", numeric = true, suffix = "گرم",
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        AppTextField(
                            value = wage, onValueChange = { wage = it },
                            label = "اجرت ساخت", numeric = true, suffix = "٪",
                        )
                    }
                }
                AppTextField(value = stock, onValueChange = { stock = it }, label = "موجودی", numeric = true)
                AppTextField(value = note, onValueChange = { note = it }, label = "توضیحات (اختیاری)", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        (initial ?: Product(
                            name = "", category = "", karat = 18,
                            weightGrams = 0.0, wagePercent = 0.0, stock = 0,
                        )).copy(
                            name = name.trim(),
                            category = category,
                            karat = karat,
                            weightGrams = weightVal ?: 0.0,
                            wagePercent = wageVal ?: 0.0,
                            stock = stockVal ?: 0,
                            note = note.trim(),
                        )
                    )
                },
            ) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
