package com.zarin.gold.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.data.Invoice
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.SectionTitle
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.toPersian
import com.zarin.gold.util.toPersianCurrency
import com.zarin.gold.util.toPersianDateTime

@Composable
fun HistoryScreen(
    invoices: List<Invoice>,
    onOpenInvoice: (Long) -> Unit
) {
    AtmosphereBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle(
                    title = "سوابق فاکتور",
                    subtitle = if (invoices.isEmpty()) "فاکتوری ثبت نشده"
                    else "${invoices.size.toPersian()} فاکتور صادر شده"
                )
            }

            items(invoices, key = { it.id }) { invoice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ZarinColors.NightElevated.copy(alpha = 0.88f))
                        .border(1.dp, ZarinColors.Line, RoundedCornerShape(16.dp))
                        .clickable { onOpenInvoice(invoice.id) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(invoice.invoiceNumber, color = ZarinColors.AmberSoft, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(invoice.customerName, color = ZarinColors.Ivory, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            invoice.createdAt.toPersianDateTime(),
                            color = ZarinColors.IvoryMuted,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        invoice.total.toPersianCurrency(),
                        color = ZarinColors.Ivory,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
