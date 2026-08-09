package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Karats
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ProductCategory
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.WageMode
import com.zarnegar.gold.domain.pricing.GoldPricing
import com.zarnegar.gold.ui.components.AmountField
import com.zarnegar.gold.ui.components.DecimalField
import com.zarnegar.gold.ui.components.KeyValueRow
import com.zarnegar.gold.ui.components.SectionCard
import com.zarnegar.gold.ui.components.TextInputField
import com.zarnegar.gold.ui.vm.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long,
    viewModel: ProductsViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var product by remember { mutableStateOf<Product?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(productId, state.settings) {
        if (!loaded) {
            product = if (productId == 0L) {
                Product(
                    code = "K-${(System.currentTimeMillis() % 100000)}",
                    name = "",
                    wagePercent = state.settings.defaultWagePercent,
                    profitPercent = state.settings.defaultProfitPercent,
                    stock = 1,
                )
            } else {
                viewModel.find(productId)
            }
            loaded = true
        }
    }

    val current = product ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == 0L) "کالای جدید" else "ویرایش کالا") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SectionCard(title = "مشخصات کالا") {
                    TextInputField(
                        label = "نام کالا",
                        value = current.name,
                        onValueChange = { product = current.copy(name = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "کد کالا",
                        value = current.code,
                        onValueChange = { product = current.copy(code = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    EnumDropdown(
                        label = "دسته‌بندی",
                        options = ProductCategory.entries,
                        selected = current.category,
                        optionLabel = { it.label },
                        onSelect = { product = current.copy(category = it) },
                    )
                    Text("نحوهٔ قیمت‌گذاری", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        PricingMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = current.pricingMode == mode,
                                onClick = { product = current.copy(pricingMode = mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index,
                                    PricingMode.entries.size,
                                ),
                            ) { Text(mode.label) }
                        }
                    }
                }
            }

            if (current.pricingMode == PricingMode.BY_WEIGHT) {
                item {
                    SectionCard(title = "وزن و عیار") {
                        KaratDropdown(
                            selected = current.karat,
                            onSelect = { product = current.copy(karat = it) },
                        )
                        DecimalField(
                            label = "وزن کل",
                            value = current.weightGrams,
                            onValueChange = { product = current.copy(weightGrams = it) },
                            suffix = "گرم",
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DecimalField(
                            label = "وزن سنگ / نگین",
                            value = current.stoneWeightGrams,
                            onValueChange = { product = current.copy(stoneWeightGrams = it) },
                            suffix = "گرم",
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AmountField(
                            label = "ارزش سنگ / نگین",
                            value = current.stoneValue,
                            onValueChange = { product = current.copy(stoneValue = it) },
                            suffix = state.settings.currencyLabel,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    SectionCard(title = "اجرت و سود") {
                        Text("نحوهٔ اجرت", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            WageMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = current.wageMode == mode,
                                    onClick = { product = current.copy(wageMode = mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index,
                                        WageMode.entries.size,
                                    ),
                                ) { Text(mode.label) }
                            }
                        }
                        if (current.wageMode == WageMode.PERCENT) {
                            DecimalField(
                                label = "درصد اجرت",
                                value = current.wagePercent,
                                onValueChange = { product = current.copy(wagePercent = it) },
                                suffix = "٪",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            AmountField(
                                label = "اجرت هر گرم",
                                value = current.wagePerGram,
                                onValueChange = { product = current.copy(wagePerGram = it) },
                                suffix = state.settings.currencyLabel,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        DecimalField(
                            label = "درصد سود فروشنده",
                            value = current.profitPercent,
                            onValueChange = { product = current.copy(profitPercent = it) },
                            suffix = "٪",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                item {
                    SectionCard(title = "قیمت مقطوع") {
                        AmountField(
                            label = "قیمت فروش",
                            value = current.fixedPrice,
                            onValueChange = { product = current.copy(fixedPrice = it) },
                            suffix = state.settings.currencyLabel,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                SectionCard(title = "موجودی و مالیات") {
                    AmountField(
                        label = "موجودی",
                        value = current.stock.toLong(),
                        onValueChange = { product = current.copy(stock = it.toInt()) },
                        suffix = "عدد",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("معاف از مالیات بر ارزش افزوده")
                            Text(
                                text = "برای سکه و مسکوکات فعال کنید",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = current.vatExempt,
                            onCheckedChange = { product = current.copy(vatExempt = it) },
                        )
                    }
                    TextInputField(
                        label = "توضیحات",
                        value = current.note,
                        onValueChange = { product = current.copy(note = it) },
                        singleLine = false,
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                val breakdown = remember(current, state.settings) {
                    GoldPricing.priceLine(SaleItem.fromProduct(current), state.settings.pricing())
                }
                val currency = state.settings.currencyLabel
                SectionCard(title = "پیش‌نمایش قیمت با نرخ روز") {
                    KeyValueRow(
                        "نرخ هر گرم (عیار ${PersianText.formatNumber(current.karat.toLong())})",
                        "${PersianText.formatNumber(breakdown.ratePerGram)} $currency",
                    )
                    KeyValueRow(
                        "ارزش طلا",
                        "${PersianText.formatNumber(breakdown.unitGoldValue)} $currency",
                    )
                    KeyValueRow(
                        "اجرت ساخت",
                        "${PersianText.formatNumber(breakdown.unitWage)} $currency",
                    )
                    KeyValueRow(
                        "سود فروشنده",
                        "${PersianText.formatNumber(breakdown.unitProfit)} $currency",
                    )
                    if (breakdown.unitStoneValue > 0) {
                        KeyValueRow(
                            "ارزش سنگ",
                            "${PersianText.formatNumber(breakdown.unitStoneValue)} $currency",
                        )
                    }
                    KeyValueRow(
                        "مالیات بر ارزش افزوده",
                        "${PersianText.formatNumber(breakdown.vat)} $currency",
                    )
                    KeyValueRow(
                        "قیمت نهایی",
                        "${PersianText.formatNumber(breakdown.total)} $currency",
                        emphasize = true,
                        valueColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.save(current)
                        onDone()
                    },
                    enabled = current.name.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("ذخیرهٔ کالا") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KaratDropdown(selected: Int, onSelect: (Int) -> Unit) {
    EnumDropdown(
        label = "عیار",
        options = Karats.COMMON.map { it.first },
        selected = selected,
        optionLabel = { Karats.label(it) },
        onSelect = onSelect,
    )
}
