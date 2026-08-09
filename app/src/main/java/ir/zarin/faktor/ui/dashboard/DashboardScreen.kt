package ir.zarin.faktor.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.ui.common.EmptyState
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.NumericField
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.StatCard
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.invoices.InvoiceRow
import ir.zarin.faktor.ui.theme.GoldAccent
import ir.zarin.faktor.ui.theme.PaidGreen
import ir.zarin.faktor.ui.theme.PartialAmber

@Composable
fun DashboardScreen(
    onNewSale: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onNewSale = onNewSale,
        onOpenProducts = onOpenProducts,
        onOpenCustomers = onOpenCustomers,
        onOpenInvoices = onOpenInvoices,
        onOpenSettings = onOpenSettings,
        onOpenInvoice = onOpenInvoice,
        onGoldRateChange = viewModel::updateGoldRate,
        modifier = modifier,
    )
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    onNewSale: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onGoldRateChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    var showRateDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GoldRateCard(
                shopName = state.settings.shopName,
                rateText = format.money(state.settings.goldRatePerGramRial),
                unitLabel = currencyLabel(),
                dateText = format.dateLong(state.todayMillis),
                onEditRate = { showRateDialog = true },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = stringResource(R.string.today_sales),
                    value = "${format.money(state.todaySalesRial)} ${currencyLabel()}",
                    icon = Icons.Default.PointOfSale,
                    modifier = Modifier.weight(1f),
                    accent = PaidGreen,
                )
                StatCard(
                    title = stringResource(R.string.today_invoices),
                    value = format.count(state.todayInvoiceCount),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = stringResource(R.string.month_sales),
                    value = "${format.money(state.monthSalesRial)} ${currencyLabel()}",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = stringResource(R.string.unpaid_total),
                    value = "${format.money(state.outstandingRial)} ${currencyLabel()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    accent = PartialAmber,
                )
            }
        }

        item {
            SectionCard(title = stringResource(R.string.quick_actions)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    QuickAction(Icons.Default.PointOfSale, stringResource(R.string.new_invoice), onNewSale)
                    QuickAction(Icons.Default.Diamond, stringResource(R.string.nav_products), onOpenProducts)
                    QuickAction(Icons.Default.Groups, stringResource(R.string.nav_customers), onOpenCustomers)
                    QuickAction(Icons.Default.Settings, stringResource(R.string.nav_settings), onOpenSettings)
                }
            }
        }

        item {
            SectionCard(
                title = stringResource(R.string.recent_invoices),
                trailing = {
                    TextButton(onClick = onOpenInvoices) { Text(stringResource(R.string.see_all)) }
                },
            ) {
                if (state.recentInvoices.isEmpty()) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        text = stringResource(R.string.no_invoices),
                    )
                }
            }
        }

        items(state.recentInvoices, key = { it.id }) { invoice ->
            InvoiceRow(invoice = invoice, onClick = { onOpenInvoice(invoice.id) })
        }
    }

    if (showRateDialog) {
        GoldRateDialog(
            initialValue = format.amountForInput(state.settings.goldRatePerGramRial),
            unitLabel = currencyLabel(),
            onDismiss = { showRateDialog = false },
            onConfirm = { text ->
                onGoldRateChange(format.inputToRial(text))
                showRateDialog = false
            },
        )
    }
}

@Composable
private fun GoldRateCard(
    shopName: String,
    rateText: String,
    unitLabel: String,
    dateText: String,
    onEditRate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(listOf(Color(0xFF6B4E00), Color(0xFFB08400), GoldAccent)),
            ),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = shopName.ifBlank { stringResource(R.string.app_name) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                    IconButton(onClick = onEditRate) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.gold_rate_edit),
                            tint = Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.gold_rate_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = rateText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = unitLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onClick,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoldRateDialog(
    initialValue: String,
    unitLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gold_rate_edit)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.gold_rate_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                NumericField(
                    value = text,
                    onValueChange = { text = it },
                    label = stringResource(R.string.gold_rate_short),
                    suffix = unitLabel,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
