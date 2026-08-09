package com.zarin.goldshop.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarin.goldshop.data.Product
import com.zarin.goldshop.ui.AppViewModel
import com.zarin.goldshop.ui.NumberField
import com.zarin.goldshop.util.PersianUtils

@Composable
fun InventoryScreen(vm: AppViewModel) {
    val products by vm.products.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Product?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("افزودن کالا") },
            )
        }
    ) { padding ->
        if (products.isEmpty()) {
            EmptyState(Modifier.fillMaxSize().padding(padding), "هنوز کالایی ثبت نشده است", "برای شروع، یک کالای طلا یا جواهر اضافه کنید")
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(products, key = { it.id }) { p ->
                    ProductCard(p, onEdit = { editing = p; showDialog = true }, onDelete = { vm.deleteProduct(p) })
                }
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { vm.saveProduct(it); showDialog = false },
        )
    }
}

@Composable
private fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${product.category} • وزن ${PersianUtils.formatWeight(product.weight)} گرم • عیار ${PersianUtils.toPersianDigits(product.karat.toString())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "اجرت ${PersianUtils.formatNumber(product.wagePercent)}٪ • موجودی ${PersianUtils.toPersianDigits(product.stock.toString())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "حذف") }
        }
    }
}

@Composable
private fun ProductDialog(initial: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "طلا") }
    var weight by remember { mutableStateOf(initial?.weight?.toString() ?: "") }
    var karat by remember { mutableStateOf(initial?.karat?.toString() ?: "18") }
    var wage by remember { mutableStateOf(initial?.wagePercent?.toString() ?: "7") }
    var stone by remember { mutableStateOf(initial?.stonePrice?.toString() ?: "0") }
    var stock by remember { mutableStateOf(initial?.stock?.toString() ?: "1") }
    var code by remember { mutableStateOf(initial?.code ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (weight.toDoubleOrNull() ?: 0.0) > 0,
                onClick = {
                    onSave(
                        (initial ?: Product(name = "", category = "", weight = 0.0, karat = 18, wagePercent = 0.0, stonePrice = 0, stock = 0)).copy(
                            name = name.trim(),
                            category = category,
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            karat = karat.toIntOrNull() ?: 18,
                            wagePercent = wage.toDoubleOrNull() ?: 0.0,
                            stonePrice = stone.toLongOrNull() ?: 0,
                            stock = stock.toIntOrNull() ?: 0,
                            code = code.trim(),
                        )
                    )
                }
            ) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
        title = { Text(if (initial == null) "افزودن کالا" else "ویرایش کالا") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام کالا") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                CategoryPicker(category) { category = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(weight, { weight = it }, "وزن (گرم)", Modifier.weight(1f), decimal = true)
                    NumberField(karat, { karat = it }, "عیار", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(wage, { wage = it }, "اجرت (٪)", Modifier.weight(1f), decimal = true)
                    NumberField(stock, { stock = it }, "موجودی", Modifier.weight(1f))
                }
                NumberField(stone, { stone = it }, "قیمت نگین/سنگ (تومان)", Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("کد کالا (اختیاری)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
    )
}
