package ir.zarin.faktor.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.core.PersianNumbers
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.model.ProductCategory
import ir.zarin.faktor.ui.common.ChoiceChips
import ir.zarin.faktor.ui.common.ConfirmDialog
import ir.zarin.faktor.ui.common.FieldLabel
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.NumericField
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.ZarinTextField
import ir.zarin.faktor.ui.common.ZarinTopBar
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.label

private val KARAT_OPTIONS = listOf(14, 18, 21, 22, 24)

@Composable
fun ProductEditScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductEditViewModel = viewModel(factory = ProductEditViewModel.Factory),
) {
    val product by viewModel.product.collectAsStateWithLifecycle()
    val current = product ?: return

    Scaffold(
        modifier = modifier,
        topBar = {
            ZarinTopBar(
                title = stringResource(if (viewModel.isNew) R.string.add_product else R.string.edit_product),
                onBack = onDone,
                actions = {
                    if (!viewModel.isNew) {
                        var confirming by remember { mutableStateOf(false) }
                        IconButton(onClick = { confirming = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                        if (confirming) {
                            ConfirmDialog(
                                text = stringResource(R.string.delete_product_confirm),
                                onConfirm = {
                                    confirming = false
                                    viewModel.delete(onDone)
                                },
                                onDismiss = { confirming = false },
                                confirmLabel = stringResource(R.string.delete),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        ProductEditForm(
            initial = current,
            onSave = { updated -> viewModel.save(updated, onDone) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
fun ProductEditForm(
    initial: Product,
    onSave: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current

    var name by remember(initial.id) { mutableStateOf(initial.name) }
    var code by remember(initial.id) { mutableStateOf(initial.code) }
    var category by remember(initial.id) { mutableStateOf(initial.category) }
    var pricingMode by remember(initial.id) { mutableStateOf(initial.pricingMode) }
    var karat by remember(initial.id) { mutableStateOf(initial.karat) }
    var weight by remember(initial.id) {
        mutableStateOf(if (initial.weightGrams == 0.0) "" else PersianNumbers.decimal(initial.weightGrams))
    }
    var wagePercent by remember(initial.id) {
        mutableStateOf(if (initial.wagePercent == 0.0) "" else PersianNumbers.decimal(initial.wagePercent))
    }
    var stonePrice by remember(initial.id) { mutableStateOf(format.amountForInput(initial.stonePriceRial)) }
    var fixedPrice by remember(initial.id) { mutableStateOf(format.amountForInput(initial.fixedPriceRial)) }
    var stock by remember(initial.id) { mutableStateOf(initial.stockQty.toString()) }
    var applyVat by remember(initial.id) { mutableStateOf(initial.applyVat) }
    var note by remember(initial.id) { mutableStateOf(initial.note) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = stringResource(R.string.add_product)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZarinTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.product_name),
                )
                ZarinTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "${stringResource(R.string.product_code)} (${stringResource(R.string.optional)})",
                )
                Column {
                    FieldLabel(stringResource(R.string.category))
                    ChoiceChips(
                        options = ProductCategory.entries,
                        selected = category,
                        onSelect = { category = it },
                        label = { it.label() },
                    )
                }
                Column {
                    FieldLabel(stringResource(R.string.pricing_mode))
                    ChoiceChips(
                        options = PricingMode.entries,
                        selected = pricingMode,
                        onSelect = { pricingMode = it },
                        label = { it.label() },
                    )
                }
            }
        }

        if (pricingMode == PricingMode.BY_WEIGHT) {
            SectionCard(title = stringResource(R.string.pricing_by_weight)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        FieldLabel(stringResource(R.string.karat))
                        ChoiceChips(
                            options = KARAT_OPTIONS,
                            selected = karat,
                            onSelect = { karat = it },
                            label = { format.count(it) },
                        )
                    }
                    NumericField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = stringResource(R.string.weight_gram),
                        suffix = stringResource(R.string.gram),
                        decimal = true,
                    )
                    NumericField(
                        value = wagePercent,
                        onValueChange = { wagePercent = it },
                        label = stringResource(R.string.wage_percent),
                        suffix = "٪",
                        decimal = true,
                    )
                }
            }
        } else {
            SectionCard(title = stringResource(R.string.pricing_fixed)) {
                NumericField(
                    value = fixedPrice,
                    onValueChange = { fixedPrice = it },
                    label = stringResource(R.string.fixed_price),
                    suffix = currencyLabel(),
                )
            }
        }

        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumericField(
                    value = stonePrice,
                    onValueChange = { stonePrice = it },
                    label = stringResource(R.string.stone_price),
                    suffix = currencyLabel(),
                )
                NumericField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = stringResource(R.string.stock_qty),
                    suffix = stringResource(R.string.count_unit),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.apply_vat),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(checked = applyVat, onCheckedChange = { applyVat = it })
                }
                ZarinTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(R.string.note),
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        Button(
            onClick = {
                onSave(
                    initial.copy(
                        name = name.trim(),
                        code = code.trim(),
                        category = category,
                        pricingMode = pricingMode,
                        karat = karat,
                        weightGrams = PersianNumbers.parseDouble(weight) ?: 0.0,
                        wagePercent = PersianNumbers.parseDouble(wagePercent) ?: 0.0,
                        stonePriceRial = format.inputToRial(stonePrice),
                        fixedPriceRial = format.inputToRial(fixedPrice),
                        stockQty = PersianNumbers.parseLong(stock)?.toInt() ?: 0,
                        applyVat = applyVat,
                        note = note.trim(),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
        ) {
            Text(stringResource(R.string.save))
        }
        Spacer(Modifier.height(24.dp))
    }
}
