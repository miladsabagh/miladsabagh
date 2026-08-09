@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.StatTile
import ir.zarrin.goldshop.ui.formatCount
import ir.zarrin.goldshop.ui.formatMoney
import ir.zarrin.goldshop.ui.theme.ZarrinColors
import ir.zarrin.goldshop.ui.viewmodel.DashboardViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun DashboardScreen(
    bottomBar: @Composable () -> Unit,
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingRate by remember { mutableStateOf(false) }
    val today = remember { PersianCalendar.today() }
    val weekDay = remember { PersianCalendar.weekDayName(System.currentTimeMillis()) }

    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.settings.shopName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$weekDay ${today.formatLong()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenReports) {
                        Icon(Icons.Filled.BarChart, contentDescription = "گزارش‌ها")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewInvoice,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("فاکتور جدید") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GoldRateCard(
                    rate = state.settings.goldRate18,
                    currencyLabel = state.settings.currency.shortLabel,
                    onEdit = { editingRate = true }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        title = "فروش امروز",
                        value = formatMoney(state.todaySales, state.settings.currency),
                        caption = "${formatCount(state.todayCount)} فاکتور",
                        icon = Icons.Filled.Today,
                        container = ZarrinColors.SuccessSoft,
                        contentColor = ZarrinColors.Success,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenInvoices
                    )
                    StatTile(
                        title = "فروش این ماه",
                        value = formatMoney(state.monthSales, state.settings.currency),
                        caption = "${formatCount(state.monthCount)} فاکتور",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        container = ZarrinColors.InfoSoft,
                        contentColor = ZarrinColors.Info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenReports
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        title = "مانده طلب",
                        value = formatMoney(state.receivables, state.settings.currency),
                        caption = "فاکتورهای تسویه‌نشده",
                        icon = Icons.Filled.HourglassBottom,
                        container = ZarrinColors.DangerSoft,
                        contentColor = ZarrinColors.Danger,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenInvoices
                    )
                    StatTile(
                        title = "انبار و مشتریان",
                        value = "${formatCount(state.productCount)} کالا",
                        caption = "${formatCount(state.customerCount)} مشتری",
                        icon = Icons.Filled.Diamond,
                        container = ZarrinColors.GoldSoft,
                        contentColor = ZarrinColors.Gold,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProducts
                    )
                }
            }
            item {
                SectionCard(title = "دسترسی سریع", icon = Icons.Filled.Scale) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction(
                            label = "فاکتور جدید",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            modifier = Modifier.weight(1f),
                            onClick = onNewInvoice
                        )
                        QuickAction(
                            label = "کالای جدید",
                            icon = Icons.Filled.Diamond,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenProducts
                        )
                        QuickAction(
                            label = "مشتری جدید",
                            icon = Icons.Filled.PersonAdd,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenCustomers
                        )
                        QuickAction(
                            label = "گزارش‌ها",
                            icon = Icons.Filled.BarChart,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenReports
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
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "هنوز فاکتوری صادر نشده",
                            subtitle = "با دکمه «فاکتور جدید» اولین فاکتور فروش خود را صادر کنید."
                        )
                    }
                }
            } else {
                items(state.recentInvoices, key = { it.id }) { invoice ->
                    InvoiceRow(
                        invoice = invoice,
                        currency = state.settings.currency,
                        onClick = { onOpenInvoice(invoice.id) }
                    )
                }
            }
        }
    }

    if (editingRate) {
        GoldRateDialog(
            initial = state.settings.goldRate18,
            currencyLabel = state.settings.currency.shortLabel,
            onDismiss = { editingRate = false },
            onConfirm = { rate ->
                viewModel.updateGoldRate(rate)
                editingRate = false
            }
        )
    }
}

@Composable
private fun GoldRateCard(rate: Long, currencyLabel: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                    .size(46.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Diamond,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "نرخ روز طلای ۱۸ عیار",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    "${PersianNumbers.formatAmount(rate)} $currencyLabel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "هر گرم — مبنای محاسبه فاکتورهای جدید",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            TextButton(onClick = onEdit) { Text("بروزرسانی") }
        }
    }
}

@Composable
private fun GoldRateDialog(
    initial: Long,
    currencyLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var text by remember { mutableStateOf(if (initial == 0L) "" else initial.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نرخ روز طلای ۱۸ عیار") },
        text = {
            Column {
                Text(
                    "نرخ هر گرم طلای ۱۸ عیار را وارد کنید؛ این عدد مبنای قیمت‌گذاری فاکتورهای جدید است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                AmountField(
                    label = "نرخ هر گرم",
                    text = text,
                    onTextChange = { text = it },
                    currency = if (currencyLabel == "ریال") {
                        ir.zarrin.goldshop.domain.model.Currency.RIAL
                    } else {
                        ir.zarrin.goldshop.domain.model.Currency.TOMAN
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(PersianNumbers.parseLong(text) ?: 0L) }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InvoiceRow(
    invoice: Invoice,
    currency: ir.zarrin.goldshop.domain.model.Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = invoice.payable - invoice.paid
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.customerName.ifBlank { "مشتری متفرقه" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${invoice.type.label} • شماره ${PersianNumbers.toPersianDigits(invoice.number)} • " +
                        PersianCalendar.fromEpochMillis(invoice.dateMillis).formatNumeric(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatMoney(invoice.payable, currency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (remaining > 0L) "مانده: ${formatMoney(remaining, currency)}" else "تسویه شده",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining > 0L) ZarrinColors.Danger else ZarrinColors.Success
                )
            }
        }
    }
}
