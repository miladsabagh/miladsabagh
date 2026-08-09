package com.goldshop.app.ui.screens.invoice

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.goldshop.app.data.model.Customer
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.InvoiceWithItems
import com.goldshop.app.data.model.Product
import com.goldshop.app.ui.InvoiceDraftUi
import com.goldshop.app.ui.components.AppTextField
import com.goldshop.app.ui.components.EmptyState
import com.goldshop.app.ui.components.PriceBreakdown
import com.goldshop.app.ui.components.PrimaryButton
import com.goldshop.app.ui.components.ProductRow
import com.goldshop.app.ui.components.ScreenHeader
import com.goldshop.app.ui.components.SectionLabel
import com.goldshop.app.util.PriceCalculator
import com.goldshop.app.util.formatDateFa
import com.goldshop.app.util.formatToman
import com.goldshop.app.util.purityLabel
import com.goldshop.app.util.toPersianDigits
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun InvoiceHubScreen(
    products: List<Product>,
    customers: List<Customer>,
    invoices: List<Invoice>,
    draft: InvoiceDraftUi,
    goldPrice: Long,
    subtotal: Long,
    tax: Long,
    total: Long,
    selectedInvoice: InvoiceWithItems?,
    onAddProduct: (Product) -> Unit,
    onRemoveProduct: (Long) -> Unit,
    onChangeQty: (Long, Int) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onUpdateDraft: (customerName: String?, customerPhone: String?, discount: String?, notes: String?, paid: String?) -> Unit,
    onIssue: (onDone: (Long) -> Unit) -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onClearSelected: () -> Unit,
    onExportPdf: (Long, onReady: (File) -> Unit, onError: (String) -> Unit) -> Unit,
    onClearDraft: () -> Unit
) {
    var mode by remember { mutableStateOf(InvoiceMode.CREATE) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(selectedInvoice) {
        if (selectedInvoice != null) mode = InvoiceMode.DETAIL
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenHeader(
                title = "فاکتور",
                subtitle = "صدور، مشاهده و اشتراک‌گذاری",
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = mode == InvoiceMode.CREATE,
                            onClick = {
                                mode = InvoiceMode.CREATE
                                onClearSelected()
                            },
                            label = { Text("جدید") }
                        )
                        FilterChip(
                            selected = mode == InvoiceMode.HISTORY,
                            onClick = {
                                mode = InvoiceMode.HISTORY
                                onClearSelected()
                            },
                            label = { Text("سوابق") }
                        )
                    }
                }
            )

            when (mode) {
                InvoiceMode.CREATE -> CreateInvoiceContent(
                    products = products,
                    customers = customers,
                    draft = draft,
                    goldPrice = goldPrice,
                    subtotal = subtotal,
                    tax = tax,
                    total = total,
                    onAddProduct = onAddProduct,
                    onRemoveProduct = onRemoveProduct,
                    onChangeQty = onChangeQty,
                    onSelectCustomer = onSelectCustomer,
                    onUpdateDraft = onUpdateDraft,
                    onIssue = {
                        onIssue { id ->
                            scope.launch {
                                snackbar.showSnackbar("فاکتور با موفقیت صادر شد")
                            }
                            onOpenInvoice(id)
                            mode = InvoiceMode.DETAIL
                        }
                    },
                    onClearDraft = onClearDraft
                )

                InvoiceMode.HISTORY -> InvoiceHistoryContent(
                    invoices = invoices,
                    onOpen = {
                        onOpenInvoice(it)
                        mode = InvoiceMode.DETAIL
                    }
                )

                InvoiceMode.DETAIL -> {
                    val detail = selectedInvoice
                    if (detail == null) {
                        EmptyState(message = "فاکتوری انتخاب نشده است.")
                    } else {
                        InvoiceDetailContent(
                            detail = detail,
                            onBack = {
                                mode = InvoiceMode.HISTORY
                                onClearSelected()
                            },
                            onShare = {
                                onExportPdf(
                                    detail.invoice.id,
                                    { file ->
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, "اشتراک فاکتور")
                                        )
                                    },
                                    { err ->
                                        scope.launch { snackbar.showSnackbar(err) }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class InvoiceMode { CREATE, HISTORY, DETAIL }

@Composable
private fun CreateInvoiceContent(
    products: List<Product>,
    customers: List<Customer>,
    draft: InvoiceDraftUi,
    goldPrice: Long,
    subtotal: Long,
    tax: Long,
    total: Long,
    onAddProduct: (Product) -> Unit,
    onRemoveProduct: (Long) -> Unit,
    onChangeQty: (Long, Int) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onUpdateDraft: (customerName: String?, customerPhone: String?, discount: String?, notes: String?, paid: String?) -> Unit,
    onIssue: () -> Unit,
    onClearDraft: () -> Unit
) {
    var showProductPicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("مشتری", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = draft.selectedCustomer == null && draft.customerName.isBlank(),
                    onClick = { onSelectCustomer(null) },
                    label = { Text("حضوری") }
                )
                customers.take(4).forEach { customer ->
                    FilterChip(
                        selected = draft.selectedCustomer?.id == customer.id,
                        onClick = { onSelectCustomer(customer) },
                        label = { Text(customer.fullName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = draft.customerName,
                onValueChange = { onUpdateDraft(it, null, null, null, null) },
                label = "نام مشتری"
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = draft.customerPhone,
                onValueChange = { onUpdateDraft(null, it, null, null, null) },
                label = "موبایل",
                keyboardType = KeyboardType.Phone
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showProductPicker = true }) { Text("افزودن کالا") }
                Text("اقلام فاکتور", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (draft.cart.isEmpty()) {
            item {
                Text(
                    "کالایی اضافه نشده است. از فهرست محصولات انتخاب کنید.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(draft.cart, key = { it.product.id }) { item ->
                val line = PriceCalculator.cartItemTotal(item, goldPrice)
                ProductRow(
                    title = item.product.name,
                    subtitle = "${item.product.weightGrams.toPersianDigits()} گرم · ${purityLabel(item.product.purity)}",
                    price = line.formatToman(),
                    trailing = {
                        IconButton(onClick = { onChangeQty(item.product.id, item.quantity - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "کاهش")
                        }
                        Text(
                            item.quantity.toPersianDigits(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { onChangeQty(item.product.id, item.quantity + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "افزایش")
                        }
                        TextButton(onClick = { onRemoveProduct(item.product.id) }) {
                            Text("حذف")
                        }
                    }
                )
            }
        }

        item {
            AppTextField(
                value = draft.discountText,
                onValueChange = { onUpdateDraft(null, null, it.filter(Char::isDigit), null, null) },
                label = "تخفیف (ریال)",
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = draft.paidText,
                onValueChange = { onUpdateDraft(null, null, null, null, it.filter(Char::isDigit)) },
                label = "مبلغ پرداختی (خالی = کل مبلغ)",
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = draft.notes,
                onValueChange = { onUpdateDraft(null, null, null, it, null) },
                label = "توضیحات فاکتور",
                singleLine = false
            )
        }

        item {
            PriceBreakdown(
                lines = listOf(
                    "نرخ طلای ۱۸" to goldPrice.formatToman(),
                    "جمع جزء" to subtotal.formatToman(),
                    "تخفیف" to (draft.discountText.toLongOrNull() ?: 0L).formatToman(),
                    "مالیات" to tax.formatToman()
                ),
                totalLabel = "مبلغ نهایی",
                totalValue = total.formatToman()
            )
            if (draft.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(draft.error, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(text = "صدور فاکتور", onClick = onIssue, enabled = draft.cart.isNotEmpty())
            TextButton(onClick = onClearDraft, modifier = Modifier.fillMaxWidth()) {
                Text("پاک کردن پیش‌نویس")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showProductPicker) {
        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text("انتخاب کالا") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products.filter { it.stockQuantity > 0 }, key = { it.id }) { product ->
                        val price = PriceCalculator.productUnitPrice(product, goldPrice)
                        ProductRow(
                            title = product.name,
                            subtitle = "${product.category.labelFa} · موجودی ${product.stockQuantity.toPersianDigits()}",
                            price = price.formatToman(),
                            onClick = {
                                onAddProduct(product)
                                showProductPicker = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("بستن") }
            }
        )
    }
}

@Composable
private fun InvoiceHistoryContent(
    invoices: List<Invoice>,
    onOpen: (Long) -> Unit
) {
    if (invoices.isEmpty()) {
        EmptyState(message = "سابقه‌ای برای نمایش وجود ندارد.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(invoices, key = { it.id }) { invoice ->
            ProductRow(
                title = invoice.invoiceNumber,
                subtitle = "${invoice.customerName} · ${invoice.createdAt.formatDateFa()}",
                price = invoice.total.formatToman(),
                onClick = { onOpen(invoice.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDetailContent(
    detail: InvoiceWithItems,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    val invoice = detail.invoice
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(invoice.invoiceNumber) },
            navigationIcon = {
                TextButton(onClick = onBack) { Text("بازگشت") }
            },
            actions = {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "اشتراک PDF")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("مشتری: ${invoice.customerName}", style = MaterialTheme.typography.titleMedium)
                Text("تاریخ: ${invoice.createdAt.formatDateFa()}")
                if (invoice.customerPhone.isNotBlank()) {
                    Text("موبایل: ${invoice.customerPhone.toPersianDigits()}")
                }
                Text("نرخ طلا: ${invoice.goldPricePerGram18.formatToman()}")
            }
            item { SectionLabel("اقلام") }
            items(detail.items, key = { it.id }) { item ->
                ProductRow(
                    title = item.productName,
                    subtitle = "${item.weightGrams.toPersianDigits()} گرم · ${purityLabel(item.purity)} · تعداد ${item.quantity.toPersianDigits()}",
                    price = item.lineTotal.formatToman()
                )
            }
            item {
                PriceBreakdown(
                    lines = listOf(
                        "جمع جزء" to invoice.subtotal.formatToman(),
                        "تخفیف" to invoice.discount.formatToman(),
                        "مالیات" to invoice.taxAmount.formatToman(),
                        "پرداخت‌شده" to invoice.paidAmount.formatToman()
                    ),
                    totalLabel = "مبلغ نهایی",
                    totalValue = invoice.total.formatToman()
                )
                if (invoice.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("توضیحات: ${invoice.notes}")
                }
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryButton(text = "اشتراک‌گذاری PDF", onClick = onShare)
            }
        }
    }
}
