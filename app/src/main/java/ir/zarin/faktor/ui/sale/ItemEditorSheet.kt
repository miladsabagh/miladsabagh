package ir.zarin.faktor.ui.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.zarin.faktor.R
import ir.zarin.faktor.core.PersianNumbers
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.model.ProductCategory
import ir.zarin.faktor.domain.PricingContext
import ir.zarin.faktor.ui.common.ChoiceChips
import ir.zarin.faktor.ui.common.FieldLabel
import ir.zarin.faktor.ui.common.KeyValueRow
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.NumericField
import ir.zarin.faktor.ui.common.SectionDivider
import ir.zarin.faktor.ui.common.ZarinTextField
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.label

private val KARAT_OPTIONS = listOf(14, 18, 21, 22, 24)

/** فرم افزودن یا ویرایش یک قلم فاکتور. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorSheet(
    initial: SaleLine,
    pricingContext: PricingContext,
    products: List<Product>,
    onConfirm: (SaleLine) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        ItemEditorForm(
            initial = initial,
            pricingContext = pricingContext,
            products = products,
            onConfirm = onConfirm,
        )
    }
}

@Composable
fun ItemEditorForm(
    initial: SaleLine,
    pricingContext: PricingContext,
    products: List<Product>,
    onConfirm: (SaleLine) -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 640.dp,
) {
    val format = LocalDisplayFormat.current
    var showProductPicker by remember { mutableStateOf(false) }

    var name by remember(initial.key) { mutableStateOf(initial.name) }
    var productId by remember(initial.key) { mutableStateOf(initial.productId) }
    var category by remember(initial.key) { mutableStateOf(initial.category) }
    var pricingMode by remember(initial.key) { mutableStateOf(initial.pricingMode) }
    var karat by remember(initial.key) { mutableIntStateOf(initial.karat) }
    var weight by remember(initial.key) {
        mutableStateOf(if (initial.weightGrams == 0.0) "" else PersianNumbers.decimal(initial.weightGrams))
    }
    var quantity by remember(initial.key) { mutableStateOf(initial.quantity.toString()) }
    var wagePercent by remember(initial.key) {
        mutableStateOf(if (initial.wagePercent == 0.0) "" else PersianNumbers.decimal(initial.wagePercent))
    }
    var stonePrice by remember(initial.key) { mutableStateOf(format.amountForInput(initial.stonePriceRial)) }
    var fixedPrice by remember(initial.key) {
        mutableStateOf(format.amountForInput(initial.unitFixedPriceRial))
    }
    var applyVat by remember(initial.key) { mutableStateOf(initial.applyVat) }
    var discount by remember(initial.key) { mutableStateOf(format.amountForInput(initial.discountRial)) }

    fun buildLine(): SaleLine = initial.copy(
        productId = productId,
        name = name.trim(),
        category = category,
        pricingMode = pricingMode,
        karat = karat,
        weightGrams = PersianNumbers.parseDouble(weight) ?: 0.0,
        quantity = (PersianNumbers.parseLong(quantity)?.toInt() ?: 1).coerceAtLeast(1),
        wagePercent = PersianNumbers.parseDouble(wagePercent) ?: 0.0,
        stonePriceRial = format.inputToRial(stonePrice),
        unitFixedPriceRial = format.inputToRial(fixedPrice),
        applyVat = applyVat,
        discountRial = format.inputToRial(discount),
    )

    val preview = buildLine().breakdown(pricingContext)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.add_item),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        if (products.isNotEmpty()) {
            OutlinedButton(
                onClick = { showProductPicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(
                    text = "  ${stringResource(R.string.from_inventory)}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        ZarinTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.product_name),
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

        if (pricingMode == PricingMode.BY_WEIGHT) {
            Column {
                FieldLabel(stringResource(R.string.karat))
                ChoiceChips(
                    options = KARAT_OPTIONS,
                    selected = karat,
                    onSelect = { karat = it },
                    label = { format.count(it) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumericField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = stringResource(R.string.weight_gram),
                    suffix = stringResource(R.string.gram),
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
                NumericField(
                    value = wagePercent,
                    onValueChange = { wagePercent = it },
                    label = stringResource(R.string.wage_percent),
                    suffix = "٪",
                    decimal = true,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            NumericField(
                value = fixedPrice,
                onValueChange = { fixedPrice = it },
                label = stringResource(R.string.fixed_price),
                suffix = currencyLabel(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericField(
                value = quantity,
                onValueChange = { quantity = it },
                label = stringResource(R.string.quantity),
                suffix = stringResource(R.string.count_unit),
                modifier = Modifier.weight(1f),
            )
            NumericField(
                value = stonePrice,
                onValueChange = { stonePrice = it },
                label = stringResource(R.string.stone_price),
                suffix = currencyLabel(),
                modifier = Modifier.weight(1f),
            )
        }

        NumericField(
            value = discount,
            onValueChange = { discount = it },
            label = stringResource(R.string.discount),
            suffix = currencyLabel(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = stringResource(R.string.apply_vat), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = applyVat, onCheckedChange = { applyVat = it })
        }

        SectionDivider()

        KeyValueRow(
            label = stringResource(R.string.gold_value),
            value = "${format.money(preview.goldValueRial)} ${currencyLabel()}",
        )
        if (preview.wageRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.wage),
                value = "${format.money(preview.wageRial)} ${currencyLabel()}",
            )
        }
        if (preview.profitRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.profit),
                value = "${format.money(preview.profitRial)} ${currencyLabel()}",
            )
        }
        if (preview.stoneRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.stone),
                value = "${format.money(preview.stoneRial)} ${currencyLabel()}",
            )
        }
        if (preview.vatRial > 0) {
            KeyValueRow(
                label = stringResource(R.string.vat),
                value = "${format.money(preview.vatRial)} ${currencyLabel()}",
            )
        }
        KeyValueRow(
            label = stringResource(R.string.line_total),
            value = "${format.money(preview.totalRial)} ${currencyLabel()}",
            emphasize = true,
        )

        Button(
            onClick = { onConfirm(buildLine()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
        ) {
            Text(stringResource(R.string.add))
        }
    }

    if (showProductPicker) {
        ProductPickerSheet(
            products = products,
            pricingContext = pricingContext,
            onSelect = { product ->
                productId = product.id.takeIf { it != 0L }
                name = product.name
                category = product.category
                pricingMode = product.pricingMode
                karat = product.karat
                weight = if (product.weightGrams == 0.0) "" else PersianNumbers.decimal(product.weightGrams)
                wagePercent =
                    if (product.wagePercent == 0.0) "" else PersianNumbers.decimal(product.wagePercent)
                stonePrice = format.amountForInput(product.stonePriceRial)
                fixedPrice = format.amountForInput(product.fixedPriceRial)
                applyVat = product.applyVat
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false },
        )
    }
}
