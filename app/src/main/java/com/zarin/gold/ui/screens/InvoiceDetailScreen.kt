package com.zarin.gold.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.data.Invoice
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.GhostButton
import com.zarin.gold.ui.components.GoldButton
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.InvoiceExporter
import com.zarin.gold.util.toPersian
import com.zarin.gold.util.toPersianCurrency
import com.zarin.gold.util.toPersianDateTime
import com.zarin.gold.util.toPersianDigits
import com.zarin.gold.util.toPersianWeight

@Composable
fun InvoiceDetailScreen(
    invoice: Invoice?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    AtmosphereBackground {
        if (invoice == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("فاکتور یافت نشد", color = ZarinColors.Ivory)
                Spacer(modifier = Modifier.height(16.dp))
                GhostButton(text = "بازگشت", onClick = onBack, modifier = Modifier.fillMaxWidth())
            }
            return@AtmosphereBackground
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "زرین",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = ZarinColors.AmberSoft
                )
                Text("فاکتور فروش طلا و جواهر", color = ZarinColors.IvoryMuted, fontSize = 13.sp)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ZarinColors.NightElevated.copy(alpha = 0.9f))
                        .border(1.dp, ZarinColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Meta("شماره فاکتور", invoice.invoiceNumber)
                    Meta("تاریخ", invoice.createdAt.toPersianDateTime())
                    Meta("مشتری", invoice.customerName)
                    if (invoice.customerPhone.isNotBlank()) {
                        Meta("تلفن", invoice.customerPhone.toPersianDigits())
                    }
                    Meta("نرخ طلای ۱۸", invoice.goldPricePerGram18.toPersianCurrency())
                }
            }

            items(invoice.lines) { line ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ZarinColors.NightSoft.copy(alpha = 0.8f))
                        .padding(14.dp)
                ) {
                    Text(line.productName, color = ZarinColors.Ivory, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${line.carat.toPersian()} عیار · ${line.weightGram.toPersianWeight()} · تعداد ${line.quantity.toPersian()}",
                        color = ZarinColors.IvoryMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(line.lineTotal.toPersianCurrency(), color = ZarinColors.AmberSoft, fontSize = 13.sp)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ZarinColors.NightElevated)
                        .border(1.dp, ZarinColors.Line, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Meta("جمع جزء", invoice.subtotal.toPersianCurrency())
                    Meta("تخفیف", invoice.discount.toPersianCurrency())
                    Meta("مالیات ۹٪", invoice.tax.toPersianCurrency())
                    Meta("مبلغ قابل پرداخت", invoice.total.toPersianCurrency())
                    if (invoice.note.isNotBlank()) {
                        Meta("یادداشت", invoice.note)
                    }
                }
            }

            item {
                GoldButton(
                    text = "اشتراک‌گذاری PDF",
                    onClick = { shareInvoice(context, invoice) },
                    icon = Icons.Outlined.Share,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                GhostButton(
                    text = "بازگشت",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun Meta(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ZarinColors.IvoryMuted, fontSize = 13.sp)
        Text(value, color = ZarinColors.Ivory, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun shareInvoice(context: Context, invoice: Invoice) {
    runCatching { InvoiceExporter.sharePdf(context, invoice) }
}
