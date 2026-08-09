package ir.zarrin.goldshop.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.InvoiceEntity
import ir.zarrin.goldshop.domain.InvoiceStatus
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.StatCard
import ir.zarrin.goldshop.ui.components.StatusPill
import ir.zarrin.goldshop.ui.components.asAmount
import ir.zarrin.goldshop.ui.theme.SuccessGreen
import ir.zarrin.goldshop.ui.theme.WarningAmber
import ir.zarrin.goldshop.util.JalaliDate
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toJalaliDateLabel
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenReports: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: DashboardViewModel = viewModel {
        DashboardViewModel(
            container.invoiceRepository,
            container.productRepository,
            container.customerRepository,
            container.settingsRepository
        )
    }
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    var showRateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(settings.shopName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "امروز ${JalaliDate.now().formattedLong()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenReports) {
                        Icon(Icons.Default.BarChart, contentDescription = "گزارش‌ها")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewInvoice,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("فاکتور جدید") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GoldRateCard(
                    rate = settings.money(settings.goldRate18),
                    updatedAt = if (settings.rateUpdatedAt > 0) {
                        "به‌روزرسانی: ${settings.rateUpdatedAt.toJalaliDateLabel()}"
                    } else {
                        "نرخ پیش‌فرض برنامه"
                    },
                    onEdit = { showRateDialog = true }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "فروش امروز",
                        value = settings.money(state.todayTotal),
                        subtitle = "${state.todayCount.toPersianDigits()} فاکتور",
                        icon = Icons.Default.TrendingUp,
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "فروش این ماه",
                        value = settings.money(state.monthTotal),
                        subtitle = "${state.monthCount.toPersianDigits()} فاکتور",
                        icon = Icons.Default.Payments,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "مانده مطالبات",
                        value = settings.money(state.receivables),
                        subtitle = "مبلغ دریافت نشده",
                        icon = Icons.Default.Description,
                        accent = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "موجودی انبار",
                        value = "${state.stockWeight.formatWeight()} گرم",
                        subtitle = "${state.stockCount.toPersianDigits()} قطعه در ${state.productCount.toPersianDigits()} کالا",
                        icon = Icons.Default.Scale,
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionCard(title = "دسترسی سریع") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction(
                            icon = Icons.Default.ShoppingBag,
                            label = "فاکتورها",
                            onClick = onOpenInvoices,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            icon = Icons.Default.Diamond,
                            label = "کالاها",
                            onClick = onOpenProducts,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            icon = Icons.Default.People,
                            label = "مشتریان",
                            onClick = onOpenCustomers,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            icon = Icons.Default.BarChart,
                            label = "گزارش",
                            onClick = onOpenReports,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("آخرین فاکتورها", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onOpenInvoices) { Text("مشاهده همه") }
                }
            }

            if (state.recentInvoices.isEmpty()) {
                item {
                    SectionCard {
                        EmptyState(
                            icon = Icons.Default.Description,
                            title = "هنوز فاکتوری ثبت نشده",
                            message = "با دکمه «فاکتور جدید» اولین فروش خود را ثبت کنید."
                        )
                    }
                }
            } else {
                items(state.recentInvoices, key = { it.id }) { invoice ->
                    RecentInvoiceRow(
                        invoice = invoice,
                        amount = settings.money(invoice.grandTotal),
                        onClick = { onOpenInvoice(invoice.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showRateDialog) {
        var rateText by remember { mutableStateOf(settings.display(settings.goldRate18).toString()) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("نرخ روز طلای ۱۸ عیار") },
            text = {
                Column {
                    Text(
                        "نرخ هر گرم طلای ۱۸ عیار را وارد کنید. قیمت سایر عیارها به نسبت محاسبه می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    AmountField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = "نرخ هر گرم",
                        suffix = settings.currencyLabel
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val entered = rateText.asAmount()
                    if (entered > 0) {
                        viewModel.updateGoldRate(entered / settings.currency.multiplier)
                    }
                    showRateDialog = false
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun GoldRateCard(rate: String, updatedAt: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Diamond,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "نرخ هر گرم طلای ۱۸ عیار",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    rate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    updatedAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "ویرایش نرخ")
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RecentInvoiceRow(invoice: InvoiceEntity, amount: String, onClick: () -> Unit) {
    val status = InvoiceStatus.fromName(invoice.status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.customerName, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(3.dp))
                Text(
                    "شماره ${invoice.number.toPersianDigits()} • ${invoice.dateMillis.toJalaliDateLabel()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                StatusPill(
                    text = status.label,
                    color = when (status) {
                        InvoiceStatus.PAID -> SuccessGreen
                        InvoiceStatus.PARTIAL -> WarningAmber
                        InvoiceStatus.UNPAID -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}
