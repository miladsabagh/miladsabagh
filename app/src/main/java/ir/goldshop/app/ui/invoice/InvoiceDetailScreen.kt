package ir.goldshop.app.ui.invoice

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.goldshop.app.data.dao.InvoiceWithItems
import ir.goldshop.app.data.repository.GoldShopRepository
import ir.goldshop.app.pdf.InvoicePdfGenerator
import ir.goldshop.app.ui.components.SummaryRow
import ir.goldshop.app.util.PersianDate
import ir.goldshop.app.util.formatToman
import ir.goldshop.app.util.formatWeight
import ir.goldshop.app.util.toPersianDigits
import ir.goldshop.app.util.toPersianString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceViewModel,
    repository: GoldShopRepository,
    invoiceId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var generating by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        data = viewModel.getInvoiceWithItems(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات فاکتور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (data != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف فاکتور")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = data
        if (deleted) {
            LaunchedEffect(Unit) { onBack() }
        }
        if (current == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            val invoice = current.invoice
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "فاکتور شماره ${invoice.invoiceNumber.toPersianDigits()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                PersianDate.fromTimestamp(invoice.issuedAt).toDisplayString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            HorizontalDivider()
                            Text("مشتری: ${invoice.customerName}", style = MaterialTheme.typography.bodyLarge)
                            if (invoice.customerPhone.isNotBlank()) {
                                Text("تماس: ${invoice.customerPhone.toPersianDigits()}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(current.items) { item ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.itemName, fontWeight = FontWeight.Bold)
                            Text(
                                "عیار ${item.karat.toPersianString()} · وزن ${item.weightGrams.formatWeight()} گرم · تعداد ${item.quantity.toPersianString()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            SummaryRow("جمع این قلم", "${item.lineTotal.formatToman()} تومان", emphasize = true)
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryRow("مجموع وزن", "${invoice.totalWeightGrams.formatWeight()} گرم")
                            SummaryRow("جمع اقلام", "${invoice.subtotalAmount.formatToman()} تومان")
                            SummaryRow("تخفیف", "${invoice.discountAmount.formatToman()} تومان")
                            HorizontalDivider()
                            SummaryRow("مبلغ نهایی", "${invoice.totalAmount.formatToman()} تومان", emphasize = true)
                            SummaryRow("پرداخت‌شده", "${invoice.paidAmount.formatToman()} تومان")
                            if (!invoice.isFullyPaid) {
                                SummaryRow("باقیمانده", "${invoice.remainingAmount.formatToman()} تومان")
                            }
                            SummaryRow("روش پرداخت", invoice.paymentMethod)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (!generating) {
                                generating = true
                                scope.launch {
                                    val settings = repository.getSettingsOrDefault()
                                    val file = InvoicePdfGenerator.generate(context, invoice, current.items, settings)
                                    val uri = InvoicePdfGenerator.getShareUri(context, file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    generating = false
                                    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری یا چاپ فاکتور"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text(if (generating) "  در حال آماده‌سازی..." else "  اشتراک‌گذاری / چاپ فاکتور (PDF)")
                    }
                }
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("حذف فاکتور") },
                    text = { Text("آیا از حذف این فاکتور مطمئن هستید؟ این عملیات قابل بازگشت نیست.") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.deleteInvoice(invoice)
                            showDeleteConfirm = false
                            deleted = true
                        }) { Text("حذف") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
                    }
                )
            }
        }
    }
}
