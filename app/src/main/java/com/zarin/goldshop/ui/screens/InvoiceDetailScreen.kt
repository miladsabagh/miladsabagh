package com.zarin.goldshop.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarin.goldshop.ui.AppViewModel
import com.zarin.goldshop.ui.MoneyRow
import com.zarin.goldshop.ui.SectionCard
import com.zarin.goldshop.util.InvoicePdf
import com.zarin.goldshop.util.PersianUtils

@Composable
fun InvoiceDetailScreen(vm: AppViewModel, invoiceId: Long, onDeleted: () -> Unit) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val data by vm.observeInvoice(invoiceId).collectAsStateWithLifecycle(initialValue = null)

    val inv = data ?: run {
        Text("در حال بارگذاری...", Modifier.padding(24.dp))
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = "اطلاعات فاکتور") {
            Spacer(Modifier.height(8.dp))
            InfoRow("شماره فاکتور", PersianUtils.toPersianDigits(inv.invoice.invoiceNumber))
            InfoRow("تاریخ", PersianUtils.formatDateLong(inv.invoice.dateMillis))
            InfoRow("خریدار", inv.invoice.customerName)
            if (inv.invoice.customerPhone.isNotBlank()) {
                InfoRow("تماس", PersianUtils.toPersianDigits(inv.invoice.customerPhone))
            }
            InfoRow("نرخ روز طلا", PersianUtils.formatToman(inv.invoice.goldPricePerGram))
        }

        SectionCard(title = "اقلام") {
            Spacer(Modifier.height(8.dp))
            inv.items.forEach { item ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "وزن ${PersianUtils.formatWeight(item.weight)}گ • عیار ${PersianUtils.toPersianDigits(item.karat.toString())} • تعداد ${PersianUtils.toPersianDigits(item.quantity.toString())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyRow("جمع ردیف", item.lineTotal)
                }
                HorizontalDivider()
            }
        }

        SectionCard(title = "جمع کل") {
            Spacer(Modifier.height(8.dp))
            MoneyRow("ارزش طلا", inv.invoice.goldValueTotal)
            MoneyRow("اجرت", inv.invoice.wageTotal)
            if (inv.invoice.stoneTotal > 0) MoneyRow("نگین/سنگ", inv.invoice.stoneTotal)
            MoneyRow("سود", inv.invoice.profitTotal)
            MoneyRow("مالیات ارزش افزوده", inv.invoice.taxTotal)
            if (inv.invoice.discount > 0) MoneyRow("تخفیف", -inv.invoice.discount)
            Spacer(Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(Modifier.height(6.dp))
            MoneyRow("مبلغ قابل پرداخت", inv.invoice.grandTotal, strong = true)
        }

        Button(
            onClick = {
                val file = InvoicePdf.generate(context, inv, settings)
                val uri = InvoicePdf.uriFor(context, file)
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "فاکتور ${inv.invoice.invoiceNumber}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(share, "اشتراک فاکتور"))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.height(0.dp))
            Text("  صدور و اشتراک فاکتور PDF")
        }

        OutlinedButton(
            onClick = {
                val file = InvoicePdf.generate(context, inv, settings)
                val uri = InvoicePdf.uriFor(context, file)
                val view = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(view, "مشاهده فاکتور"))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
            Text("  مشاهده PDF")
        }

        OutlinedButton(
            onClick = { vm.deleteInvoice(inv.invoice.id); onDeleted() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("حذف فاکتور") }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
