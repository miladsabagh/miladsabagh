package ir.zarrin.goldshop.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.domain.Karats
import ir.zarrin.goldshop.domain.PriceInput
import ir.zarrin.goldshop.domain.PricingEngine
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.ProductCategory
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.LabeledRow
import ir.zarrin.goldshop.ui.components.PlainField
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.asAmount
import ir.zarrin.goldshop.ui.components.asDecimal
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: ProductEditViewModel = viewModel {
        ProductEditViewModel(container.productRepository, container.settingsRepository, productId)
    }
    val state by viewModel.state.collectAsState()
    val settings = state.settings

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    val currencyMultiplier = settings.currency.multiplier
    val price = PricingEngine.calculate(
        PriceInput(
            pricingMode = state.pricingMode,
            weightGrams = state.weight.asDecimal(),
            karat = state.karat,
            baseRatePerGram = settings.goldRate18,
            wagePercent = state.wagePercent.asDecimal(),
            profitPercent = state.profitPercent.asDecimal(),
            stonePrice = state.stonePrice.asAmount() / currencyMultiplier,
            fixedPrice = state.fixedPrice.asAmount() / currencyMultiplier,
            taxPercent = settings.taxPercent,
            taxable = state.taxable,
            quantity = 1
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (productId > 0) "ویرایش کالا" else "کالای جدید") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(title = "مشخصات کالا") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlainField(
                            value = state.name,
                            onValueChange = { value -> viewModel.update { it.copy(name = value) } },
                            label = "نام کالا"
                        )
                        PlainField(
                            value = state.code,
                            onValueChange = { value -> viewModel.update { it.copy(code = value) } },
                            label = "کد کالا"
                        )
                        Text("دسته‌بندی", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ProductCategory.entries.toList()) { category ->
                                FilterChip(
                                    selected = state.category == category,
                                    onClick = { viewModel.update { it.copy(category = category) } },
                                    label = { Text(category.label) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "شیوه قیمت‌گذاری") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            PricingMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = state.pricingMode == mode,
                                    onClick = { viewModel.update { it.copy(pricingMode = mode) } },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = PricingMode.entries.size
                                    )
                                ) { Text(mode.label) }
                            }
                        }

                        if (state.pricingMode == PricingMode.BY_WEIGHT) {
                            Text("عیار", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Karats.COMMON.forEach { karat ->
                                    FilterChip(
                                        selected = state.karat == karat,
                                        onClick = { viewModel.update { it.copy(karat = karat) } },
                                        label = { Text(karat.toPersianDigits()) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                            DecimalField(
                                value = state.weight,
                                onValueChange = { value -> viewModel.update { it.copy(weight = value) } },
                                label = "وزن",
                                suffix = "گرم"
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DecimalField(
                                    value = state.wagePercent,
                                    onValueChange = { value -> viewModel.update { it.copy(wagePercent = value) } },
                                    label = "اجرت ساخت",
                                    suffix = "٪",
                                    modifier = Modifier.weight(1f)
                                )
                                DecimalField(
                                    value = state.profitPercent,
                                    onValueChange = { value -> viewModel.update { it.copy(profitPercent = value) } },
                                    label = "سود فروشنده",
                                    suffix = "٪",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            AmountField(
                                value = state.stonePrice,
                                onValueChange = { value -> viewModel.update { it.copy(stonePrice = value) } },
                                label = "بهای نگین و سنگ",
                                suffix = settings.currencyLabel
                            )
                        } else {
                            AmountField(
                                value = state.fixedPrice,
                                onValueChange = { value -> viewModel.update { it.copy(fixedPrice = value) } },
                                label = "قیمت مقطوع",
                                suffix = settings.currencyLabel
                            )
                            DecimalField(
                                value = state.weight,
                                onValueChange = { value -> viewModel.update { it.copy(weight = value) } },
                                label = "وزن (اختیاری)",
                                suffix = "گرم"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("مشمول مالیات بر ارزش افزوده", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "برای سکه و مسکوکات معمولاً غیرفعال است",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.taxable,
                                onCheckedChange = { value -> viewModel.update { it.copy(taxable = value) } }
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "انبار و توضیحات") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlainField(
                            value = state.stockQty,
                            onValueChange = { value ->
                                viewModel.update { it.copy(stockQty = value.filter { ch -> ch.isDigit() }) }
                            },
                            label = "تعداد موجودی",
                            keyboardType = KeyboardType.Number
                        )
                        PlainField(
                            value = state.note,
                            onValueChange = { value -> viewModel.update { it.copy(note = value) } },
                            label = "توضیحات",
                            singleLine = false,
                            minLines = 2
                        )
                    }
                }
            }

            item {
                SectionCard(title = "قیمت محاسبه‌شده با نرخ روز") {
                    Column {
                        if (state.pricingMode == PricingMode.BY_WEIGHT) {
                            LabeledRow("نرخ هر گرم ${state.karat.toPersianDigits()} عیار", settings.money(PricingEngine.ratePerGram(settings.goldRate18, state.karat)))
                            LabeledRow("بهای طلا", settings.money(price.goldValue))
                            LabeledRow("اجرت ساخت", settings.money(price.wage))
                            LabeledRow("سود فروشنده", settings.money(price.profit))
                            if (price.stone > 0) LabeledRow("بهای نگین", settings.money(price.stone))
                        }
                        LabeledRow("مالیات بر ارزش افزوده", settings.money(price.tax))
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(6.dp))
                        LabeledRow(
                            "قیمت فروش",
                            settings.money(price.unitPrice),
                            emphasize = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.save(
                            ProductEntity(
                                id = state.id,
                                code = state.code.ifBlank { "K-${System.currentTimeMillis() % 100000}" },
                                name = state.name.ifBlank { "کالای بدون نام" },
                                category = state.category.name,
                                karat = state.karat,
                                weightGrams = state.weight.asDecimal(),
                                wagePercent = state.wagePercent.asDecimal(),
                                profitPercent = state.profitPercent.asDecimal(),
                                stonePrice = state.stonePrice.asAmount() / currencyMultiplier,
                                pricingMode = state.pricingMode.name,
                                fixedPrice = state.fixedPrice.asAmount() / currencyMultiplier,
                                taxable = state.taxable,
                                stockQty = state.stockQty.toIntOrNull() ?: 1,
                                note = state.note
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = state.name.isNotBlank()
                ) {
                    Text("ذخیره کالا", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
