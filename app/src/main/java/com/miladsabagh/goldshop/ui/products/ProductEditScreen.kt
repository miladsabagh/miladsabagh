package com.miladsabagh.goldshop.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.data.local.entity.Product
import com.miladsabagh.goldshop.data.local.entity.ProductCategory
import com.miladsabagh.goldshop.ui.LocalViewModelFactory
import com.miladsabagh.goldshop.ui.components.NumberField
import com.miladsabagh.goldshop.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProductViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.RING) }
    var weight by remember { mutableStateOf(0.0) }
    var karat by remember { mutableStateOf(18) }
    var laborFee by remember { mutableStateOf(7.0) }
    var profit by remember { mutableStateOf(7.0) }
    var stonePrice by remember { mutableStateOf(0.0) }
    var quantity by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var existing by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        if (productId != null && productId > 0) {
            val product = viewModel.getProduct(productId)
            if (product != null) {
                existing = product
                name = product.name
                code = product.code
                category = product.category
                weight = product.weightGrams
                karat = product.karat
                laborFee = product.laborFeePercent
                profit = product.profitPercent
                stonePrice = product.stonePrice
                quantity = product.quantity
                notes = product.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "کالای جدید" else "ویرایش کالا") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام کالا") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("کد / بارکد (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )

            CategoryDropdown(selected = category, onSelected = { category = it })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "وزن (گرم)",
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.weight(1f)
                )
                KaratDropdown(selected = karat, onSelected = { karat = it }, modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "اجرت ساخت",
                    value = laborFee,
                    onValueChange = { laborFee = it },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "سود فروشنده",
                    value = profit,
                    onValueChange = { profit = it },
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "قیمت سنگ (تومان)",
                    value = stonePrice,
                    onValueChange = { stonePrice = it },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (quantity == 0) "" else quantity.toString(),
                    onValueChange = {
                        val cleaned = PersianFormat.toEnglishDigits(it).filter { c -> c.isDigit() }
                        quantity = cleaned.toIntOrNull() ?: 0
                    },
                    label = { Text("موجودی (تعداد)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val product = (existing ?: Product(name = name, weightGrams = weight)).copy(
                        name = name,
                        code = code,
                        category = category,
                        weightGrams = weight,
                        karat = karat,
                        laborFeePercent = laborFee,
                        profitPercent = profit,
                        stonePrice = stonePrice,
                        quantity = quantity,
                        notes = notes
                    )
                    viewModel.saveProduct(product) { onDone() }
                },
                enabled = name.isNotBlank() && weight > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره کالا")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(selected: ProductCategory, onSelected: (ProductCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("نوع کالا") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProductCategory.values().forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.displayName) },
                    onClick = {
                        onSelected(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KaratDropdown(selected: Int, onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val karats = listOf(24, 22, 21, 18, 14, 10)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = PersianFormat.toPersianDigits(selected.toString()),
            onValueChange = {},
            readOnly = true,
            label = { Text("عيار") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            karats.forEach { k ->
                DropdownMenuItem(
                    text = { Text(PersianFormat.toPersianDigits(k.toString())) },
                    onClick = {
                        onSelected(k)
                        expanded = false
                    }
                )
            }
        }
    }
}
