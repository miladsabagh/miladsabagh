package com.zarin.gold.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.data.Customer
import com.zarin.gold.data.InvoiceLine
import com.zarin.gold.ui.InvoiceDraft
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.GoldButton
import com.zarin.gold.ui.components.SectionTitle
import com.zarin.gold.ui.components.ZarinField
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.toPersian
import com.zarin.gold.util.toPersianCurrency
import com.zarin.gold.util.toPersianWeight

@Composable
fun InvoiceCreateScreen(
    draft: InvoiceDraft,
    customers: List<Customer>,
    onCustomerChange: (String, String) -> Unit,
    onSelectCustomer: (Customer) -> Unit,
    onDiscountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onChangeQuantity: (Long, Int) -> Unit,
    onRemoveLine: (Long) -> Unit,
    onOpenCatalog: () -> Unit,
    onSubmit: () -> Unit
) {
    AtmosphereBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionTitle(
                    title = "صدور فاکتور",
                    subtitle = "مشتری، اقلام و تخفیف را مشخص کنید"
                )
            }

            item {
                ZarinField(
                    value = draft.customerName,
                    onValueChange = { onCustomerChange(it, draft.customerPhone) },
                    label = "نام مشتری"
                )
            }

            item {
                ZarinField(
                    value = draft.customerPhone,
                    onValueChange = { onCustomerChange(draft.customerName, it) },
                    label = "شماره تماس"
                )
            }

            if (customers.isNotEmpty()) {
                item {
                    Text("انتخاب سریع مشتری", color = ZarinColors.IvoryMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        customers.take(3).forEach { customer ->
                            Text(
                                text = customer.name,
                                color = ZarinColors.AmberSoft,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, ZarinColors.Amber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable { onSelectCustomer(customer) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("اقلام فاکتور", color = ZarinColors.Ivory, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "+ افزودن از کاتالوگ",
                        color = ZarinColors.Amber,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenCatalog)
                            .padding(8.dp)
                    )
                }
            }

            if (draft.lines.isEmpty()) {
                item {
                    Text(
                        text = "هنوز کالایی اضافه نشده است.",
                        color = ZarinColors.IvoryMuted,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ZarinColors.NightElevated.copy(alpha = 0.7f))
                            .padding(16.dp)
                    )
                }
            } else {
                items(draft.lines, key = { it.productId }) { line ->
                    InvoiceLineRow(
                        line = line,
                        onInc = { onChangeQuantity(line.productId, 1) },
                        onDec = { onChangeQuantity(line.productId, -1) },
                        onRemove = { onRemoveLine(line.productId) }
                    )
                }
            }

            item {
                ZarinField(
                    value = draft.discountText,
                    onValueChange = onDiscountChange,
                    label = "تخفیف (ریال)"
                )
            }

            item {
                ZarinField(
                    value = draft.note,
                    onValueChange = onNoteChange,
                    label = "یادداشت (اختیاری)",
                    singleLine = false
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ZarinColors.NightSoft.copy(alpha = 0.9f))
                        .border(1.dp, ZarinColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryRow("جمع جزء", draft.subtotal.toPersianCurrency())
                    SummaryRow("تخفیف", draft.discount.toPersianCurrency())
                    SummaryRow("مالیات ۹٪", draft.tax.toPersianCurrency())
                    SummaryRow("مبلغ نهایی", draft.total.toPersianCurrency(), emphasize = true)
                }
            }

            item {
                GoldButton(
                    text = "ثبت و صدور فاکتور",
                    onClick = onSubmit,
                    enabled = draft.customerName.isNotBlank() && draft.lines.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun InvoiceLineRow(
    line: InvoiceLine,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZarinColors.NightElevated.copy(alpha = 0.85f))
            .border(1.dp, ZarinColors.Line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(line.productName, color = ZarinColors.Ivory, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${line.carat.toPersian()} عیار · ${line.weightGram.toPersianWeight()}",
                    color = ZarinColors.IvoryMuted,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "حذف", tint = ZarinColors.Danger)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec) {
                Icon(Icons.Outlined.Remove, contentDescription = null, tint = ZarinColors.AmberSoft)
            }
            Text(line.quantity.toPersian(), color = ZarinColors.Ivory, fontWeight = FontWeight.Bold)
            IconButton(onClick = onInc) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = ZarinColors.AmberSoft)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                line.lineTotal.toPersianCurrency(),
                color = ZarinColors.AmberSoft,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = if (emphasize) ZarinColors.Ivory else ZarinColors.IvoryMuted,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            value,
            color = if (emphasize) ZarinColors.AmberSoft else ZarinColors.Ivory,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (emphasize) 16.sp else 14.sp
        )
    }
}
