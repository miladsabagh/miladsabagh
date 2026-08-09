package ir.goldshop.app.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.data.entity.ProductCategory
import ir.goldshop.app.ui.components.NumberField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    viewModel: ProductViewModel,
    productId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.RING.persianLabel) }
    var karat by remember { mutableStateOf("18") }
    var weight by remember { mutableStateOf("") }
    var laborPercent by remember { mutableStateOf("7") }
    var profitPercent by remember { mutableStateOf("7") }
    var stock by remember { mutableStateOf("1") }
    var sku by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var loadedId by remember { mutableStateOf(0L) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.getProduct(productId)?.let { product ->
                name = product.name
                category = product.category
                karat = product.karat.toString()
                weight = product.weightGrams.let { if (it == 0.0) "" else it.toString() }
                laborPercent = product.laborPercent.toString()
                profitPercent = product.profitPercent.toString()
                stock = product.quantityInStock.toString()
                sku = product.sku
                notes = product.notes
                loadedId = product.id
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "افزودن کالای جدید" else "ویرایش کالا") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام کالا (مثلاً دستبند طلا)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("دسته‌بندی") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    ProductCategory.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.persianLabel) },
                            onClick = {
                                category = option.persianLabel
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "عیار",
                    value = karat,
                    onValueChange = { karat = it },
                    allowDecimal = false,
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "وزن (گرم)",
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "اجرت پیش‌فرض",
                    value = laborPercent,
                    onValueChange = { laborPercent = it },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "سود پیش‌فرض",
                    value = profitPercent,
                    onValueChange = { profitPercent = it },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "موجودی انبار",
                    value = stock,
                    onValueChange = { stock = it },
                    allowDecimal = false,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("کد کالا (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val product = Product(
                        id = loadedId,
                        name = name.ifBlank { "کالای بدون‌نام" },
                        category = category,
                        karat = karat.toIntOrNull() ?: 18,
                        weightGrams = weight.toDoubleOrNull() ?: 0.0,
                        laborPercent = laborPercent.toDoubleOrNull() ?: 0.0,
                        profitPercent = profitPercent.toDoubleOrNull() ?: 0.0,
                        quantityInStock = stock.toIntOrNull() ?: 0,
                        sku = sku,
                        notes = notes
                    )
                    viewModel.saveProduct(product, onSaved)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره کالا")
            }
        }
    }
}
