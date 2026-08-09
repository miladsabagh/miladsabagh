package com.miladsabagh.zarrin.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.ui.DashboardViewModel
import com.miladsabagh.zarrin.ui.components.StatCard
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.formatToman
import com.miladsabagh.zarrin.util.parseAmountToLong
import com.miladsabagh.zarrin.util.toPersianDigits

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNewInvoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val todaySales by viewModel.todaySales.collectAsStateWithLifecycle()
    val totalSales by viewModel.totalSales.collectAsStateWithLifecycle()
    val productCount by viewModel.productCount.collectAsStateWithLifecycle()

    DashboardContent(
        settings = settings,
        todaySales = todaySales,
        totalSales = totalSales,
        productCount = productCount,
        onNewInvoice = onNewInvoice,
        onOpenSettings = onOpenSettings,
        onGoldPriceSave = { viewModel.updateGoldPrice(it) }
    )
}

@Composable
fun DashboardContent(
    settings: ShopSettings,
    todaySales: Pair<Long, Int>,
    totalSales: Pair<Long, Int>,
    productCount: Int,
    onNewInvoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onGoldPriceSave: (Long) -> Unit
) {
    var showPriceDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ZarrinTopBar(
            title = settings.storeName,
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "قیمت هر گرم طلای ۱۸ عیار",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            settings.goldPricePerGram18k.formatToman(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { showPriceDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش قیمت")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "فروش امروز",
                    value = todaySales.first.formatToman(),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "فاکتور امروز",
                    value = "${todaySales.second}".toPersianDigits(),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "فروش کل",
                    value = totalSales.first.formatToman(),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "تعداد محصولات",
                    value = "$productCount".toPersianDigits(),
                    icon = Icons.Filled.Diamond,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onNewInvoice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("صدور فاکتور جدید", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showPriceDialog) {
        var text by remember { mutableStateOf(settings.goldPricePerGram18k.toString()) }
        AlertDialog(
            onDismissRequest = { showPriceDialog = false },
            title = { Text("قیمت هر گرم طلای ۱۸ عیار") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("قیمت به تومان") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    text.parseAmountToLong()?.let { onGoldPriceSave(it) }
                    showPriceDialog = false
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { showPriceDialog = false }) { Text("انصراف") }
            }
        )
    }
}
