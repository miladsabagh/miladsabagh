package com.miladsabagh.goldshop.ui.invoice

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.pdf.InvoiceContent
import com.miladsabagh.goldshop.pdf.InvoicePrintAdapter
import com.miladsabagh.goldshop.pdf.PdfInvoiceGenerator
import com.miladsabagh.goldshop.ui.LocalViewModelFactory
import com.miladsabagh.goldshop.ui.components.SummaryRow
import com.miladsabagh.goldshop.util.JalaliDate
import com.miladsabagh.goldshop.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onBack: () -> Unit,
    viewModel: InvoiceDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) { viewModel.load(invoiceId) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف فاکتور", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        val invoice = state.invoice
        if (state.isLoading || invoice == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(state.storeSettings.storeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (state.storeSettings.storeAddress.isNotBlank()) {
                        Text(state.storeSettings.storeAddress, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (state.storeSettings.storePhone.isNotBlank()) {
                        Text("تلفن: ${state.storeSettings.storePhone}", style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SummaryRow("شماره فاکتور", "#${PersianFormat.toPersianDigits(invoice.invoiceNumber.toString())}", emphasize = true)
                    SummaryRow("تاریخ صدور", JalaliDate.formatFull(invoice.issuedAt))
                    SummaryRow("خریدار", invoice.customerName)
                    if (invoice.customerPhone.isNotBlank()) {
                        SummaryRow("تلفن مشتری", invoice.customerPhone)
                    }
                    SummaryRow("نرخ طلای ۱۸ عيار", PersianFormat.formatToman(invoice.goldPriceAtSale))
                    SummaryRow("وضعیت", invoice.status.displayName)
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    state.items.forEach { item -> ItemDetailRow(item) }
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    SummaryRow("مجموع وزن", PersianFormat.formatWeight(invoice.totalWeightGrams))
                    SummaryRow("جمع اجناس (بدون مالیات)", PersianFormat.formatToman(invoice.subtotalAmount))
                    SummaryRow("مالیات ارزش افزوده", PersianFormat.formatToman(invoice.taxAmount))
                    SummaryRow("تخفیف", "- " + PersianFormat.formatToman(invoice.discountAmount))
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    SummaryRow("مبلغ نهایی قابل پرداخت", PersianFormat.formatToman(invoice.totalAmount), emphasize = true)
                    SummaryRow("مبلغ پرداختی", PersianFormat.formatToman(invoice.paidAmount))
                    SummaryRow("مانده حساب", PersianFormat.formatToman(invoice.remainingAmount))
                    if (invoice.notes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("توضیحات: ${invoice.notes}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val content = InvoiceContent(state.storeSettings, invoice, state.items)
                        val file = PdfInvoiceGenerator.generateFile(context, content)
                        val uri = PdfInvoiceGenerator.getShareUri(context, file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری فاکتور"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("اشتراک PDF")
                }
                OutlinedButton(
                    onClick = {
                        val content = InvoiceContent(state.storeSettings, invoice, state.items)
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        printManager?.print(
                            "فاکتور ${invoice.invoiceNumber}",
                            InvoicePrintAdapter(context, content),
                            PrintAttributes.Builder().build()
                        ) ?: Toast.makeText(context, "امکان چاپ در دسترس نیست", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("چاپ فاکتور")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف فاکتور") },
            text = { Text("آیا از حذف این فاکتور مطمئن هستید؟ این عملیات قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteInvoice()
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun ItemDetailRow(item: InvoiceItem) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(item.itemName, fontWeight = FontWeight.Bold)
        Text(
            "${item.category.displayName} • عیار ${PersianFormat.toPersianDigits(item.karat.toString())} • ${PersianFormat.formatWeight(item.weightGrams)} × ${PersianFormat.toPersianDigits(item.quantity.toString())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(PersianFormat.formatToman(item.lineTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(Modifier.padding(top = 6.dp))
    }
}