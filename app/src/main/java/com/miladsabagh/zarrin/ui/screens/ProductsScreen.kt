package com.miladsabagh.zarrin.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miladsabagh.zarrin.data.db.ProductCategory
import com.miladsabagh.zarrin.data.db.ProductEntity
import com.miladsabagh.zarrin.ui.ProductsViewModel
import com.miladsabagh.zarrin.ui.components.EmptyState
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.formatGram
import com.miladsabagh.zarrin.util.formatToman
import com.miladsabagh.zarrin.util.parseAmountToLong
import com.miladsabagh.zarrin.util.parseWeightToDouble
import com.miladsabagh.zarrin.util.toPersianDigits

@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    ProductsContent(
        products = products,
        onSave = { viewModel.save(it) },
        onDelete = { viewModel.delete(it) }
    )
}

@Composable
fun ProductsContent(
    products: List<ProductEntity>,
    onSave: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    var editing by remember { mutableStateOf<ProductEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = { ZarrinTopBar(title = "محصولات") },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن محصول")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (products.isEmpty()) {
                EmptyState("هنوز محصولی ثبت نشده است", Icons.Filled.Diamond)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onEdit = {
                                editing = product
                                showDialog = true
                            },
                            onDelete = { pendingDelete = product }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProductEditDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { product ->
                onSave(product)
                showDialog = false
            }
        )
    }

    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف محصول") },
            text = { Text("آیا از حذف «${product.name}» مطمئن هستید؟") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(product)
                    pendingDelete = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${product.category.displayName} • عیار ${product.karat}".toPersianDigits(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "وزن: ${product.weightGrams.formatGram()}  |  اجرت هر گرم: ${product.wagePerGram.formatToman()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditDialog(
    initial: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: ProductCategory.RING) }
    var karat by remember { mutableStateOf(initial?.karat ?: 18) }
    var weightText by remember { mutableStateOf(initial?.weightGrams?.toString() ?: "") }
    var wageText by remember { mutableStateOf(initial?.wagePerGram?.toString() ?: "") }
    var categoryMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "افزودن محصول" else "ویرایش محصول") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام محصول") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryMenu,
                    onExpandedChange = { categoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenu,
                        onDismissRequest = { categoryMenu = false }
                    ) {
                        ProductCategory.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.displayName) },
                                onClick = {
                                    category = entry
                                    categoryMenu = false
                                }
                            )
                        }
                    }
                }

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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val weight = weightText.parseWeightToDouble()
                val wage = wageText.parseAmountToLong() ?: 0L
                when {
                    name.isBlank() -> error = "نام محصول را وارد کنید"
                    weight == null || weight <= 0 -> error = "وزن معتبر نیست"
                    wage < 0 -> error = "اجرت معتبر نیست"
                    else -> onSave(
                        (initial ?: ProductEntity(name = "", category = category, weightGrams = 0.0, wagePerGram = 0)).copy(
                            name = name.trim(),
                            category = category,
                            karat = karat,
                            weightGrams = weight,
                            wagePerGram = wage
                        )
                    )
                }
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
