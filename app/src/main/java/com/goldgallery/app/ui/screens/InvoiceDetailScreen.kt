package com.goldgallery.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldgallery.app.data.db.InvoiceWithItems
import com.goldgallery.app.logic.JalaliDate
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.logic.PriceBreakdown
import com.goldgallery.app.pdf.InvoicePdfGenerator
import com.goldgallery.app.ui.theme.BronzeDeep
import com.goldgallery.app.ui.theme.Gold
import com.goldgallery.app.ui.theme.GoldLight
import com.goldgallery.app.ui.theme.GoldPale
import com.goldgallery.app.viewmodel.ShopViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun InvoiceDetailScreen(viewModel: ShopViewModel, invoiceId: Long, onBack: () -> Unit) {
    val data by viewModel.invoiceFlow(invoiceId).collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val invoice = data ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val breakdown = PriceBreakdown(
        goldRaw = invoice.items.sumOf { it.goldRaw },
        wage = invoice.items.sumOf { it.wage },
        profit = invoice.items.sumOf { it.profit },
        tax = invoice.items.sumOf { it.tax },
        total = invoice.items.sumOf { it.total },
    )

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(BronzeDeep, Gold)))
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "بازگشت",
                        tint = GoldPale,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "فاکتور فروش № ${PersianFormat.toPersianDigits(invoice.invoice.number.toString())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPale,
                    )
                    Text(
                        JalaliDate.formatLong(invoice.invoice.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPale.copy(alpha = 0.85f),
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(invoice.invoice.customerName, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(PersianFormat.toPersianDigits(invoice.invoice.customerPhone.ifBlank { "—" }))
                        }
                        HorizontalDivider()
                        Text(
                            "نرخ لحظه‌ای طلای ۱۸ عیار: ${PersianFormat.money(invoice.invoice.goldPrice18PerGram)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            itemsIndexed(invoice.items, key = { _, item -> item.id }) { index, item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = GoldPale, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                PersianFormat.toPersianDigits((index + 1).toString()),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = BronzeDeep,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${PersianFormat.weight(item.weightGrams)} • ${PersianFormat.karat(item.karat)} • تعداد ${PersianFormat.toPersianDigits(item.quantity.toString())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            PersianFormat.money(item.total),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                BreakdownCard(breakdown)
            }
        }

        Surface(shadowElevation = 12.dp) {
            Button(
                onClick = {
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            InvoicePdfGenerator.generate(context, invoice)
                        }
                        sharePdf(context, file)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("دریافت و اشتراک‌گذاری PDF فاکتور", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "com.goldgallery.app.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "فاکتور فروش طلا و جواهر")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
}
