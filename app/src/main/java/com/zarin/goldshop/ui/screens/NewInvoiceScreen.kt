package com.zarin.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.zarin.goldshop.ui.AppViewModel
import com.zarin.goldshop.ui.DraftItem
import com.zarin.goldshop.ui.MoneyRow
import com.zarin.goldshop.ui.NumberField
import com.zarin.goldshop.ui.SectionCard
import com.zarin.goldshop.util.GoldCalc
import com.zarin.goldshop.util.PersianUtils

@Composable
fun NewInvoiceScreen(vm: AppViewModel, onSaved: (Long) -> Unit) {
    val items by vm.draftItems.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()
    val customerName by vm.customerName.collectAsStateWithLifecycle()
    val customerPhone by vm.customerPhone.collectAsStateWithLifecycle()
    val discount by vm.discount.collectAsStateWithLifecycle()

    var showItemDialog by remember { mutableStateOf(false) }
    val totals = vm.computeTotals(items, settings, discount)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = "مشخصات خریدار") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = customerName,
                onValueChange = vm::setCustomerName,
                label = { Text("نام خریدار") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            NumberField(customerPhone, vm::setCustomerPhone, "شماره تماس", Modifier.fillMaxWidth())
        }

        SectionCard(title = "اقلام فاکتور") {
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text(
                    "هنوز قلمی اضافه نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.forEachIndexed { index, d ->
                    val r = GoldCalc.computeLine(
                        GoldCalc.LineInput(d.weight, d.karat, d.wagePercent, d.stonePrice, d.quantity),
                        settings.goldPricePerGram, settings.profitPercent, settings.taxPercent
                    )
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(d.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "وزن ${PersianUtils.formatWeight(d.weight)}گ • عیار ${PersianUtils.toPersianDigits(d.karat.toString())} • اجرت ${PersianUtils.formatNumber(d.wagePercent)}٪ • تعداد ${PersianUtils.toPersianDigits(d.quantity.toString())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                PersianUtils.formatToman(r.lineTotal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { vm.removeDraftItem(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف")
                        }
                    }
                    HorizontalDivider()
                }
            }
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = { showItemDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("افزودن قلم")
            }
        }

        SectionCard(title = "جمع فاکتور") {
            Spacer(Modifier.height(8.dp))
            MoneyRow("ارزش طلا", totals.goldValueTotal)
            MoneyRow("اجرت", totals.wageTotal)
            if (totals.stoneTotal > 0) MoneyRow("نگین/سنگ", totals.stoneTotal)
            MoneyRow("سود", totals.profitTotal)
            MoneyRow("مالیات ارزش افزوده", totals.taxTotal)
            Spacer(Modifier.height(8.dp))
            NumberField(
                value = if (discount == 0L) "" else discount.toString(),
                onValueChange = { vm.setDiscount(it.toLongOrNull() ?: 0L) },
                label = "تخفیف (تومان)",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            MoneyRow("مبلغ قابل پرداخت", totals.grandTotal, strong = true)
        }

        Button(
            onClick = { vm.saveInvoice { id -> onSaved(id) } },
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("ثبت و صدور فاکتور")
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showItemDialog) {
        AddItemDialog(
            vm = vm,
            onDismiss = { showItemDialog = false },
            onAdd = { vm.addDraftItem(it); showItemDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(vm: AppViewModel, onDismiss: () -> Unit, onAdd: (DraftItem) -> Unit) {
    val products by vm.products.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf("18") }
    var wage by remember { mutableStateOf("7") }
    var stone by remember { mutableStateOf("0") }
    var qty by remember { mutableStateOf("1") }
    var productId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (weight.toDoubleOrNull() ?: 0.0) > 0,
                onClick = {
                    onAdd(
                        DraftItem(
                            productId = productId,
                            name = name.trim(),
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            karat = karat.toIntOrNull() ?: 18,
                            wagePercent = wage.toDoubleOrNull() ?: 0.0,
                            stonePrice = stone.toLongOrNull() ?: 0,
                            quantity = qty.toIntOrNull() ?: 1,
                        )
                    )
                }
            ) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
        title = { Text("افزودن قلم") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (products.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = "انتخاب از انبار",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("انتخاب کالا (اختیاری)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = {
                                        name = p.name
                                        weight = p.weight.toString()
                                        karat = p.karat.toString()
                                        wage = p.wagePercent.toString()
                                        stone = p.stonePrice.toString()
                                        productId = p.id
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it; productId = null }, label = { Text("شرح کالا") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(weight, { weight = it }, "وزن (گرم)", Modifier.weight(1f), decimal = true)
                    NumberField(karat, { karat = it }, "عیار", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(wage, { wage = it }, "اجرت (٪)", Modifier.weight(1f), decimal = true)
                    NumberField(qty, { qty = it }, "تعداد", Modifier.weight(1f))
                }
                NumberField(stone, { stone = it }, "قیمت نگین/سنگ (تومان)", Modifier.fillMaxWidth())
            }
        },
    )
}
