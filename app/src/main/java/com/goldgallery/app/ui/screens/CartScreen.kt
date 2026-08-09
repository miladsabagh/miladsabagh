package com.goldgallery.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldgallery.app.data.model.Category
import com.goldgallery.app.logic.GoldCalculator
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.logic.PriceBreakdown
import com.goldgallery.app.viewmodel.ShopViewModel

@Composable
fun CartScreen(viewModel: ShopViewModel, onInvoiceIssued: (Long) -> Unit) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val goldPrice by viewModel.goldPrice18.collectAsStateWithLifecycle()

    var customerName by rememberSaveable { mutableStateOf("") }
    var customerPhone by rememberSaveable { mutableStateOf("") }

    val breakdown = GoldCalculator.total(
        cart.map {
            GoldCalculator.itemPrice(
                weightGrams = it.product.weightGrams,
                karat = it.product.karat,
                wagePerGram = it.product.wagePerGram,
                base18PerGram = goldPrice,
                quantity = it.quantity,
            )
        }
    )

    if (cart.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "فاکتور خالی است",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "از بخش محصولات، اقلام موردنظر را به فاکتور اضافه کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "فاکتور جدید",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "نرخ روز: ${PersianFormat.money(goldPrice)} برای هر گرم طلای ۱۸ عیار",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "مشخصات مشتری",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام و نام خانوادگی") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it.filter { ch -> ch.isDigit() || ch in "۰۱۲۳۴۵۶۷۸۹" } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شماره تماس") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                    )
                }
            }
        }

        items(cart, key = { it.product.id }) { item ->
            val itemPrice = GoldCalculator.itemPrice(
                weightGrams = item.product.weightGrams,
                karat = item.product.karat,
                wagePerGram = item.product.wagePerGram,
                base18PerGram = goldPrice,
                quantity = item.quantity,
            )
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${PersianFormat.weight(item.product.weightGrams)} • ${PersianFormat.karat(item.product.karat)} • ${Category.of(item.product.category).persianName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            PersianFormat.money(itemPrice.total),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.increment(item.product.id) }) {
                                Icon(Icons.Outlined.Add, contentDescription = "افزایش")
                            }
                            Text(
                                PersianFormat.toPersianDigits(item.quantity.toString()),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            IconButton(onClick = { viewModel.decrement(item.product.id) }) {
                                Icon(Icons.Outlined.Remove, contentDescription = "کاهش")
                            }
                        }
                        IconButton(onClick = { viewModel.removeFromCart(item.product.id) }) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        item {
            BreakdownCard(breakdown)
        }

        item {
            Button(
                onClick = { viewModel.issueInvoice(customerName, customerPhone, onInvoiceIssued) },
                enabled = customerName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "صدور فاکتور • ${PersianFormat.money(breakdown.total)}",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun BreakdownCard(breakdown: PriceBreakdown) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "جزئیات محاسبه",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            BreakdownRow("طلای خام", breakdown.goldRaw)
            BreakdownRow("اجرت ساخت", breakdown.wage)
            BreakdownRow("سود فروشنده (٪۷)", breakdown.profit)
            BreakdownRow("مالیات بر ارزش افزوده (٪۹)", breakdown.tax)
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "مبلغ قابل پرداخت",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    PersianFormat.money(breakdown.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(PersianFormat.money(amount), style = MaterialTheme.typography.bodyMedium)
    }
}
