package com.miladsabagh.goldshop.ui.home

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.util.JalaliDate
import com.miladsabagh.goldshop.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewInvoice: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenReports: () -> Unit,
    onAddProduct: () -> Unit,
    onAddCustomer: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = com.miladsabagh.goldshop.ui.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPriceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.storeSettings.storeName) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = JalaliDate.formatFull(System.currentTimeMillis()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("نرخ روز طلا (۱۸ عيار)", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (state.storeSettings.currentGoldPrice > 0)
                                PersianFormat.formatToman(state.storeSettings.currentGoldPrice) + " / گرم"
                            else "ثبت نشده",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { showPriceDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش نرخ طلا")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("فاکتور امروز", state.todayInvoiceCount.toString().let { PersianFormat.toPersianDigits(it) }, Modifier.weight(1f))
                StatCard("فروش امروز", PersianFormat.formatToman(state.todayTotalAmount), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("وزن فروش امروز", PersianFormat.formatWeight(state.todayTotalWeight), Modifier.weight(1f))
                StatCard("تعداد کالا / مشتری", "${PersianFormat.toPersianDigits(state.productCount.toString())} / ${PersianFormat.toPersianDigits(state.customerCount.toString())}", Modifier.weight(1f))
            }

            SectionTitle("عملیات سریع")

            Button(onClick = onNewInvoice, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("صدور فاکتور جدید")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onAddProduct, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Diamond, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("افزودن کالا")
                }
                OutlinedButton(onClick = onAddCustomer, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("افزودن مشتری")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenInvoices, modifier = Modifier.weight(1f)) {
                    Text("فاکتورها")
                }
                OutlinedButton(onClick = onOpenProducts, modifier = Modifier.weight(1f)) {
                    Text("کالاها")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenCustomers, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.People, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("مشتریان")
                }
                OutlinedButton(onClick = onOpenReports, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.QueryStats, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("گزارش‌ها")
                }
            }
        }
    }

    if (showPriceDialog) {
        GoldPriceDialog(
            currentPrice = state.storeSettings.currentGoldPrice,
            onDismiss = { showPriceDialog = false },
            onConfirm = { price ->
                viewModel.updateGoldPrice(price)
                showPriceDialog = false
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GoldPriceDialog(
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(if (currentPrice > 0) currentPrice.toLong().toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت نرخ روز طلا (۱۸ عيار)") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = PersianFormat.toEnglishDigits(it).filter { c -> c.isDigit() } },
                label = { Text("قیمت هر گرم (تومان)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.toDoubleOrNull() ?: 0.0) }) {
                Text("ثبت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
