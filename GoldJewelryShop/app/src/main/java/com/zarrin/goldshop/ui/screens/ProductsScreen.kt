package com.zarrin.goldshop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.data.ProductCategory
import com.zarrin.goldshop.data.ProductEntity
import com.zarrin.goldshop.domain.categoryLabel
import com.zarrin.goldshop.domain.formatPercentFa
import com.zarrin.goldshop.domain.formatWeightFa
import com.zarrin.goldshop.domain.parseDoubleOrZero
import com.zarrin.goldshop.domain.parseLongOrZero
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.AppTextField
import com.zarrin.goldshop.ui.components.EmptyState
import com.zarrin.goldshop.ui.components.ListDivider
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.components.ScreenScaffold

@Composable
fun ProductsScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<ProductEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        ProductEditor(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { product ->
                viewModel.saveProduct(
                    id = product.id,
                    name = product.name,
                    category = product.category,
                    weight = product.weightGrams,
                    karat = product.purityKarat,
                    makingFee = product.makingFeePercent,
                    stock = product.stockCount,
                    code = product.code,
                    notes = product.notes
                )
                showEditor = false
            }
        )
        return
    }

    ScreenScaffold(
        title = "کالاهای طلا و جواهر",
        subtitle = "موجودی، وزن، عیار و اجرت ساخت",
        onBack = onBack,
        actions = {
            TextButton(onClick = {
                editing = null
                showEditor = true
            }) { Text("افزودن") }
        }
    ) {
        if (products.isEmpty()) {
            EmptyState("هنوز کالایی ثبت نشده است")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(products, key = { it.id }) { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editing = product
                                showEditor = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${categoryLabel(product.category)} · کد ${toPersianDigits(product.code)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${formatWeightFa(product.weightGrams)} · عیار ${toPersianDigits(product.purityKarat.toString())} · اجرت ${formatPercentFa(product.makingFeePercent)} · موجودی ${toPersianDigits(product.stockCount.toString())}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { viewModel.deleteProduct(product) }) {
                                Text("حذف", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    ListDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditor(
    initial: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var code by remember { mutableStateOf(initial?.code.orEmpty()) }
    var weight by remember { mutableStateOf(initial?.weightGrams?.toString().orEmpty()) }
    var karat by remember { mutableStateOf(initial?.purityKarat?.toString() ?: "18") }
    var fee by remember { mutableStateOf(initial?.makingFeePercent?.toString() ?: "10") }
    var stock by remember { mutableStateOf(initial?.stockCount?.toString() ?: "1") }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: ProductCategory.RING) }
    var expanded by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = if (initial == null) "کالای جدید" else "ویرایش کالا",
        onBack = onDismiss
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            AppTextField(name, { name = it }, "نام کالا")
            Spacer(Modifier.height(10.dp))
            AppTextField(code, { code = it }, "کد کالا")
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = categoryLabel(category),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("دسته‌بندی") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ProductCategory.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(categoryLabel(item)) },
                            onClick = {
                                category = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            AppTextField(weight, { weight = it }, "وزن (گرم)", keyboardType = KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            AppTextField(karat, { karat = it }, "عیار", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(10.dp))
            AppTextField(fee, { fee = it }, "اجرت ساخت (٪)", keyboardType = KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            AppTextField(stock, { stock = it }, "موجودی", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(10.dp))
            AppTextField(notes, { notes = it }, "توضیحات", singleLine = false)
            Spacer(Modifier.height(20.dp))
            PrimaryActionButton(
                text = "ذخیره کالا",
                onClick = {
                    if (name.isBlank() || code.isBlank()) return@PrimaryActionButton
                    onSave(
                        ProductEntity(
                            id = initial?.id ?: 0,
                            name = name,
                            category = category,
                            weightGrams = parseDoubleOrZero(weight),
                            purityKarat = parseLongOrZero(karat).toInt().coerceIn(8, 24),
                            makingFeePercent = parseDoubleOrZero(fee),
                            stockCount = parseLongOrZero(stock).toInt().coerceAtLeast(0),
                            code = code,
                            notes = notes
                        )
                    )
                }
            )
        }
    }
}
