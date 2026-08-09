package com.zarfam.goldshop.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.data.db.InvoiceWithItems
import com.zarfam.goldshop.data.db.ShopSettings
import com.zarfam.goldshop.domain.JalaliDate
import com.zarfam.goldshop.domain.formatWeight
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.domain.toMoneyToman
import com.zarfam.goldshop.domain.toPersianDigits
import com.zarfam.goldshop.pdf.InvoicePdfGenerator
import com.zarfam.goldshop.ui.components.ConfirmDeleteDialog
import com.zarfam.goldshop.ui.components.PriceRow
import com.zarfam.goldshop.ui.viewmodel.InvoicesViewModel
import com.zarfam.goldshop.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onBack: () -> Unit,
    viewModel: InvoicesViewModel = viewModel(factory = InvoicesViewModel.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val data by viewModel.observeInvoice(invoiceId).collectAsState(initial = null)
    val settings by settingsViewModel.settings.collectAsState()
    var generatingPdf by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun sharePdf(invoiceData: InvoiceWithItems, shopSettings: ShopSettings, view: Boolean) {
        scope.launch {
            generatingPdf = true
            val file: File = withContext(Dispatchers.IO) {
                InvoicePdfGenerator(context).generate(invoiceData, shopSettings)
            }
            generatingPdf = false
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = if (view) {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(Intent.createChooser(intent, "فاکتور PDF"))
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "حذف فاکتور",
            text = "این فاکتور برای همیشه حذف شود؟",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete(invoiceId)
                onBack()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        data?.let { "فاکتور ${it.invoice.invoiceNumber.toString().toPersianDigits()}" } ?: "فاکتور",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف فاکتور", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        val invoiceData = data
        if (invoiceData == null) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val invoice = invoiceData.invoice
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("خریدار", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(invoice.customerName, fontWeight = FontWeight.Medium)
                    }
                    if (invoice.customerPhone.isNotBlank()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تلفن", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(invoice.customerPhone.toPersianDigits())
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تاریخ صدور", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(JalaliDate.formatWithTime(invoice.dateMillis))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("قیمت گرم ۱۸ عیار", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(invoice.goldPricePerGram18.toMoneyToman())
                    }
                }
            }

            Text("اقلام", style = MaterialTheme.typography.titleMedium)

            invoiceData.items.forEachIndexed { index, item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${(index + 1).toString().toPersianDigits()}. ${item.name}",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                item.lineTotal.toMoney(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${item.weightGrams.formatWeight()} گرم | عیار ${item.karat.toString().toPersianDigits()} | تعداد ${item.quantity.toString().toPersianDigits()} | اجرت ٪${item.wagePercent.formatWeight()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(14.dp)) {
                    PriceRow("ارزش طلای خام", invoiceData.items.sumOf { it.rawGoldValue }.toMoneyToman())
                    PriceRow("اجرت ساخت", invoiceData.items.sumOf { it.wageAmount }.toMoneyToman())
                    PriceRow("سود فروشنده", invoiceData.items.sumOf { it.profitAmount }.toMoneyToman())
                    PriceRow("مالیات (٪${invoice.taxPercent.formatWeight()})", invoiceData.items.sumOf { it.taxAmount }.toMoneyToman())
                    if (invoice.discount > 0) PriceRow("تخفیف", "${invoice.discount.toMoney()}- تومان")
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    PriceRow("مبلغ قابل پرداخت", invoice.grandTotal.toMoneyToman(), highlight = true)
                }
            }

            if (invoice.note.isNotBlank()) {
                Text(
                    "توضیحات: ${invoice.note}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { sharePdf(invoiceData, settings, view = false) },
                    enabled = !generatingPdf,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("اشتراک PDF")
                }
                OutlinedButton(
                    onClick = { sharePdf(invoiceData, settings, view = true) },
                    enabled = !generatingPdf,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("مشاهده PDF")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
