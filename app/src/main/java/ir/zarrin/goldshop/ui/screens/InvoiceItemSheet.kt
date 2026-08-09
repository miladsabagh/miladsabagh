@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.local.InvoiceItem
import ir.zarrin.goldshop.data.local.toLineInput
import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.model.Currency
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.DropdownSelector
import ir.zarrin.goldshop.ui.components.KeyValueRow
import ir.zarrin.goldshop.ui.components.ZarrinTextField
import ir.zarrin.goldshop.ui.formatMoney

private fun decimalToText(value: Double): String =
    if (value == 0.0) "" else PersianNumbers.toEnglishDigits(PersianNumbers.formatWeight(value, 3)).replace('٫', '.')

/** Bottom sheet used both for adding a new invoice line and for editing an existing one. */
@Composable
fun InvoiceItemSheet(
    initial: InvoiceItem,
    baseGoldRate: Long,
    currency: Currency,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (InvoiceItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initial.title) }
    var kind by remember { mutableStateOf(initial.kind) }
    var karat by remember { mutableIntStateOf(initial.karat) }
    var weightText by remember { mutableStateOf(decimalToText(initial.weightGrams)) }
    var quantityText by remember { mutableStateOf(initial.quantity.coerceAtLeast(1).toString()) }
    var rateText by remember { mutableStateOf(if (initial.goldRatePerGram == 0L) "" else initial.goldRatePerGram.toString()) }
    var wageMode by remember { mutableStateOf(initial.wageMode) }
    var wageText by remember { mutableStateOf(decimalToText(initial.wageValue)) }
    var profitText by remember { mutableStateOf(decimalToText(initial.profitPercent)) }
    var stoneText by remember { mutableStateOf(if (initial.stonePrice == 0L) "" else initial.stonePrice.toString()) }
    var taxBasis by remember { mutableStateOf(initial.taxBasis) }
    var taxText by remember { mutableStateOf(decimalToText(initial.taxPercent)) }
    var unitPriceText by remember {
        mutableStateOf(initial.unitPriceOverride?.takeIf { it > 0L }?.toString() ?: "")
    }

    val pricedPerPiece = kind.isPricedPerPiece
    val draft = initial.copy(
        title = title.trim(),
        kind = kind,
        karat = karat,
        weightGrams = PersianNumbers.parseDouble(weightText) ?: 0.0,
        quantity = (PersianNumbers.parseLong(quantityText) ?: 1L).toInt().coerceAtLeast(1),
        goldRatePerGram = PersianNumbers.parseLong(rateText) ?: 0L,
        wageMode = wageMode,
        wageValue = PersianNumbers.parseDouble(wageText) ?: 0.0,
        profitPercent = PersianNumbers.parseDouble(profitText) ?: 0.0,
        stonePrice = PersianNumbers.parseLong(stoneText) ?: 0L,
        taxBasis = taxBasis,
        taxPercent = PersianNumbers.parseDouble(taxText) ?: 0.0,
        unitPriceOverride = if (pricedPerPiece) PersianNumbers.parseLong(unitPriceText) else null
    )
    val totals = GoldCalculator.calculateLine(draft.toLineInput())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (isEditing) "ویرایش قلم فاکتور" else "افزودن قلم به فاکتور",
                style = MaterialTheme.typography.titleMedium
            )

            ZarrinTextField(
                value = title,
                onValueChange = { title = it },
                label = "شرح کالا"
            )

            DropdownSelector(
                label = "نوع کالا",
                options = ItemKind.entries,
                selected = kind,
                optionLabel = { it.label },
                onSelect = { selected ->
                    kind = selected
                    taxBasis = selected.defaultTaxBasis
                }
            )

            if (!pricedPerPiece) {
                DropdownSelector(
                    label = "عیار",
                    options = Karat.COMMON.map { it.first },
                    selected = karat,
                    optionLabel = { Karat.label(it) },
                    onSelect = { selected ->
                        karat = selected
                        rateText = GoldCalculator.rateForKarat(baseGoldRate, selected).toString()
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DecimalField(
                        label = "وزن",
                        text = weightText,
                        onTextChange = { weightText = it },
                        suffix = "گرم",
                        modifier = Modifier.weight(1f)
                    )
                    DecimalField(
                        label = "تعداد",
                        text = quantityText,
                        onTextChange = { quantityText = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                AmountField(
                    label = "نرخ هر گرم این عیار",
                    text = rateText,
                    onTextChange = { rateText = it },
                    currency = currency
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DropdownSelector(
                        label = "نحوه اجرت",
                        options = WageMode.entries,
                        selected = wageMode,
                        optionLabel = { it.label },
                        onSelect = { wageMode = it },
                        modifier = Modifier.weight(1.2f)
                    )
                    DecimalField(
                        label = "اجرت",
                        text = wageText,
                        onTextChange = { wageText = it },
                        suffix = wageMode.unit,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DecimalField(
                        label = "سود فروشنده",
                        text = profitText,
                        onTextChange = { profitText = it },
                        suffix = "٪",
                        modifier = Modifier.weight(1f)
                    )
                    AmountField(
                        label = "قیمت نگین/سنگ",
                        text = stoneText,
                        onTextChange = { stoneText = it },
                        currency = currency,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AmountField(
                        label = "قیمت واحد",
                        text = unitPriceText,
                        onTextChange = { unitPriceText = it },
                        currency = currency,
                        modifier = Modifier.weight(1.6f)
                    )
                    DecimalField(
                        label = "تعداد",
                        text = quantityText,
                        onTextChange = { quantityText = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownSelector(
                    label = "پایه مالیات",
                    options = TaxBasis.entries,
                    selected = taxBasis,
                    optionLabel = { it.label },
                    onSelect = { taxBasis = it },
                    modifier = Modifier.weight(1.4f)
                )
                DecimalField(
                    label = "مالیات",
                    text = taxText,
                    onTextChange = { taxText = it },
                    suffix = "٪",
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("پیش‌نمایش محاسبه", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    KeyValueRow("ارزش طلا", formatMoney(totals.goldValue, currency))
                    KeyValueRow("اجرت ساخت", formatMoney(totals.wage, currency))
                    KeyValueRow("سود فروشنده", formatMoney(totals.profit, currency))
                    if (totals.stone > 0L) KeyValueRow("نگین و سنگ", formatMoney(totals.stone, currency))
                    KeyValueRow("مالیات بر ارزش افزوده", formatMoney(totals.tax, currency))
                    KeyValueRow(
                        label = "مبلغ این قلم",
                        value = formatMoney(totals.total, currency),
                        emphasize = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("انصراف") }
                Button(
                    onClick = { onConfirm(draft) },
                    enabled = totals.total > 0L,
                    modifier = Modifier.weight(1.4f)
                ) {
                    Text(if (isEditing) "ثبت تغییرات" else "افزودن به فاکتور", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
