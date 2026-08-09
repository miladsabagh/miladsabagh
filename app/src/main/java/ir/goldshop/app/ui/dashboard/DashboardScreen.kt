package ir.goldshop.app.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.goldshop.app.ui.components.SectionCard
import ir.goldshop.app.util.formatToman
import ir.goldshop.app.util.toPersianString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNewInvoice: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoices: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(state.shopName) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GoldPriceCard(pricePerGram = state.goldPricePerGram, onOpenSettings = onOpenSettings)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "فاکتور امروز",
                        value = state.todayInvoiceCount.toPersianString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "فروش امروز",
                        value = "${state.todayTotalSales.formatToman()} تومان",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "تعداد مشتریان",
                        value = state.totalCustomers.toPersianString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "تعداد کالاها",
                        value = state.totalProducts.toPersianString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Button(
                    onClick = onNewInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("صدور فاکتور جدید", style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                SectionCard(title = "دسترسی سریع") {
                    QuickAccessRow(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "لیست فاکتورها",
                        onClick = onOpenInvoices
                    )
                    QuickAccessRow(
                        icon = Icons.Filled.Diamond,
                        label = "مدیریت کالاها (طلا و جواهر)",
                        onClick = onOpenProducts
                    )
                    QuickAccessRow(
                        icon = Icons.Filled.People,
                        label = "مدیریت مشتریان",
                        onClick = onOpenCustomers
                    )
                    QuickAccessRow(
                        icon = Icons.Filled.Settings,
                        label = "تنظیمات فروشگاه و نرخ طلا",
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldPriceCard(pricePerGram: Double, onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "نرخ امروز طلای ۱۸ عیار (هر گرم)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (pricePerGram > 0) "${pricePerGram.formatToman()} تومان" else "ثبت نشده",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenSettings) {
                Text("بروزرسانی نرخ طلا")
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickAccessRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onClick) {
            Text("باز کردن")
        }
    }
}
