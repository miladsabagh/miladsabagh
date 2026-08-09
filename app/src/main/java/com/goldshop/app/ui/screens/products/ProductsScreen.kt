package com.goldshop.app.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ProductCategory
import com.goldshop.app.ui.components.AppTextField
import com.goldshop.app.ui.components.EmptyState
import com.goldshop.app.ui.components.ProductRow
import com.goldshop.app.ui.components.ScreenHeader
import com.goldshop.app.ui.theme.Gold
import com.goldshop.app.ui.theme.Ink
import com.goldshop.app.util.PriceCalculator
import com.goldshop.app.util.formatToman
import com.goldshop.app.util.purityLabel
import com.goldshop.app.util.toPersianDigits

@Composable
fun ProductsScreen(
    products: List<Product>,
    goldPricePerGram18: Long,
    onSave: (
        id: Long,
        name: String,
        category: ProductCategory,
        weight: String,
        purity: String,
        labor: String,
        profit: String,
        stock: String,
        description: String
    ) -> Unit,
    onDelete: (Product) -> Unit,
    onAddToInvoice: (Product) -> Unit
) {
    var editing by remember { mutableStateOf<Product?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showEditor = true
                },
                containerColor = Gold,
                contentColor = Ink
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن محصول")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenHeader(
                title = "محصولات",
                subtitle = "کاتالوگ طلا و جواهر · ${products.size.toPersianDigits()} قلم"
            )

            if (products.isEmpty()) {
                EmptyState(
                    message = "هنوز محصولی ثبت نشده است.",
                    actionLabel = "افزودن محصول",
                    onAction = {
                        editing = null
                        showEditor = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val price = PriceCalculator.productUnitPrice(product, goldPricePerGram18)
                        ProductRow(
                            title = product.name,
                            subtitle = "${product.category.labelFa} · ${product.weightGrams.toPersianDigits()} گرم · ${purityLabel(product.purity)} · موجودی ${product.stockQuantity.toPersianDigits()}",
                            price = price.formatToman(),
                            trailing = {
                                IconButton(onClick = {
                                    editing = product
                                    showEditor = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = { onDelete(product) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف")
                                }
                            },
                            onClick = { onAddToInvoice(product) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showEditor) {
        ProductEditorDialog(
            product = editing,
            onDismiss = { showEditor = false },
            onSave = { id, name, category, weight, purity, labor, profit, stock, description ->
                onSave(id, name, category, weight, purity, labor, profit, stock, description)
                showEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        category: ProductCategory,
        weight: String,
        purity: String,
        labor: String,
        profit: String,
        stock: String,
        description: String
    ) -> Unit
) {
    var name by remember(product) { mutableStateOf(product?.name.orEmpty()) }
    var category by remember(product) { mutableStateOf(product?.category ?: ProductCategory.RING) }
    var weight by remember(product) { mutableStateOf(product?.weightGrams?.toString().orEmpty()) }
    var purity by remember(product) { mutableStateOf((product?.purity ?: 750).toString()) }
    var labor by remember(product) { mutableStateOf((product?.laborPercent ?: 10.0).toString()) }
    var profit by remember(product) { mutableStateOf((product?.profitPercent ?: 7.0).toString()) }
    var stock by remember(product) { mutableStateOf((product?.stockQuantity ?: 1).toString()) }
    var description by remember(product) { mutableStateOf(product?.description.orEmpty()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product == null) "محصول جدید" else "ویرایش محصول")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTextField(value = name, onValueChange = { name = it }, label = "نام کالا")
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category.labelFa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ProductCategory.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.labelFa) },
                                onClick = {
                                    category = item
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                AppTextField(value = weight, onValueChange = { weight = it }, label = "وزن (گرم)", keyboardType = KeyboardType.Decimal)
                AppTextField(value = purity, onValueChange = { purity = it }, label = "عیار (مثلاً ۷۵۰)", keyboardType = KeyboardType.Number)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = labor,
                        onValueChange = { labor = it },
                        label = "اجرت %",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                    AppTextField(
                        value = profit,
                        onValueChange = { profit = it },
                        label = "سود %",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    )
                }
                AppTextField(value = stock, onValueChange = { stock = it }, label = "موجودی", keyboardType = KeyboardType.Number)
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && (weight.toDoubleOrNull() ?: 0.0) > 0) {
                        onSave(
                            product?.id ?: 0L,
                            name,
                            category,
                            weight,
                            purity,
                            labor,
                            profit,
                            stock,
                            description
                        )
                    }
                }
            ) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
