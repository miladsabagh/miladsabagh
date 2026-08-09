package com.zarfam.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.data.db.Product
import com.zarfam.goldshop.data.db.ProductCategory
import com.zarfam.goldshop.domain.formatWeight
import com.zarfam.goldshop.domain.parseDecimal
import com.zarfam.goldshop.domain.toEnglishDigits
import com.zarfam.goldshop.domain.toPersianDigits
import com.zarfam.goldshop.ui.components.AppTextField
import com.zarfam.goldshop.ui.components.ConfirmDeleteDialog
import com.zarfam.goldshop.ui.components.EmptyState
import com.zarfam.goldshop.ui.viewmodel.ProductsViewModel

@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory),
) {
    val products by viewModel.products.collectAsState()
    var editing by remember { mutableStateOf<Product?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Product?>(null) }

    if (showDialog) {
        ProductDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.upsert(it)
                showDialog = false
            },
        )
    }

    deleting?.let { product ->
        ConfirmDeleteDialog(
            title = "حذف محصول",
            text = "«${product.name}» حذف شود؟",
            onConfirm = {
                viewModel.delete(product)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن محصول")
            }
        },
    ) { padding ->
        if (products.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(Icons.Default.Diamond, "محصولی ثبت نشده است. با دکمه + اضافه کنید.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            editing = product
                            showDialog = true
                        },
                        onDelete = { deleting = product },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(ProductCategory.fromName(product.category).fa) },
                )
                Text(
                    "وزن: ${product.weightGrams.formatWeight()} گرم | عیار: ${product.karat.toString().toPersianDigits()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "اجرت: ٪${product.wagePercent.formatWeight()} | سود: ٪${product.profitPercent.formatWeight()} | موجودی: ${product.stockCount.toString().toPersianDigits()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember {
        mutableStateOf(initial?.let { ProductCategory.fromName(it.category) } ?: ProductCategory.RING)
    }
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var weight by remember { mutableStateOf(initial?.weightGrams?.toString() ?: "") }
    var karat by remember { mutableStateOf(initial?.karat ?: 18) }
    var wage by remember { mutableStateOf(initial?.wagePercent?.toString() ?: "10") }
    var profit by remember { mutableStateOf(initial?.profitPercent?.toString() ?: "7") }
    var stock by remember { mutableStateOf(initial?.stockCount?.toString() ?: "1") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var karatExpanded by remember { mutableStateOf(false) }

    val weightVal = weight.parseDecimal()
    val valid = name.isNotBlank() && weightVal != null && weightVal > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "افزودن محصول" else "ویرایش محصول") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(name, { name = it }, "نام محصول *")

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category.fa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.medium,
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        ProductCategory.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.fa) },
                                onClick = {
                                    category = entry
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                AppTextField(weight, { weight = it }, "وزن *", decimal = true, suffix = "گرم")

                ExposedDropdownMenuBox(
                    expanded = karatExpanded,
                    onExpandedChange = { karatExpanded = it },
                ) {
                    OutlinedTextField(
                        value = "${karat.toString().toPersianDigits()} عیار",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("عیار") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = karatExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.medium,
                    )
                    ExposedDropdownMenu(
                        expanded = karatExpanded,
                        onDismissRequest = { karatExpanded = false },
                    ) {
                        listOf(18, 21, 22, 24).forEach { k ->
                            DropdownMenuItem(
                                text = { Text("${k.toString().toPersianDigits()} عیار") },
                                onClick = {
                                    karat = k
                                    karatExpanded = false
                                },
                            )
                        }
                    }
                }

                AppTextField(wage, { wage = it }, "اجرت ساخت", decimal = true, suffix = "٪")
                AppTextField(profit, { profit = it }, "سود فروش", decimal = true, suffix = "٪")
                AppTextField(stock, { stock = it }, "موجودی", numeric = true, suffix = "عدد")
                AppTextField(code, { code = it }, "کد محصول (اختیاری)")
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        Product(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            category = category.name,
                            code = code.trim(),
                            weightGrams = weightVal ?: 0.0,
                            karat = karat,
                            wagePercent = wage.parseDecimal() ?: 0.0,
                            profitPercent = profit.parseDecimal() ?: 0.0,
                            stockCount = stock.toEnglishDigits().trim().toIntOrNull() ?: 1,
                            createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                        ),
                    )
                },
            ) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
