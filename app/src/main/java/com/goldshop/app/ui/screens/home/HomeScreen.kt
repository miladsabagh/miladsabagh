package com.goldshop.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goldshop.app.data.model.DashboardStats
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.ShopSettings
import com.goldshop.app.ui.components.GoldPanel
import com.goldshop.app.ui.components.PrimaryButton
import com.goldshop.app.ui.components.ProductRow
import com.goldshop.app.ui.components.SectionLabel
import com.goldshop.app.ui.components.StatTile
import com.goldshop.app.ui.theme.GoldSoft
import com.goldshop.app.util.formatDateFa
import com.goldshop.app.util.formatToman
import com.goldshop.app.util.toPersianDigits
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    settings: ShopSettings,
    stats: DashboardStats,
    recentInvoices: List<Invoice>,
    onNewInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onOpenProducts: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                GoldPanel(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = settings.shopName,
                        style = MaterialTheme.typography.displayLarge,
                        color = GoldSoft
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "مدیریت فروش، موجودی و صدور فاکتور طلا",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GoldSoft.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "نرخ روز طلای ۱۸ عیار",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldSoft.copy(alpha = 0.7f)
                    )
                    Text(
                        text = settings.goldPricePerGram18.formatToman(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = GoldSoft,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    PrimaryButton(text = "صدور فاکتور جدید", onClick = onNewInvoice)
                }
            }
        }

        item {
            SectionLabel("خلاصه امروز")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "فروش امروز",
                    value = stats.todaySales.formatToman(),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "فروش ماه",
                    value = stats.monthSales.formatToman(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "محصولات",
                    value = stats.productCount.toPersianDigits(),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "مشتریان",
                    value = stats.customerCount.toPersianDigits(),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "فاکتورها",
                    value = stats.invoiceCount.toPersianDigits(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onOpenProducts) { Text("مدیریت کالا") }
                SectionLabel("آخرین فاکتورها")
            }
        }

        if (recentInvoices.isEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "هنوز فاکتوری ثبت نشده است.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recentInvoices.take(8), key = { it.id }) { invoice ->
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    ProductRow(
                        title = invoice.invoiceNumber,
                        subtitle = "${invoice.customerName} · ${invoice.createdAt.formatDateFa()}",
                        price = invoice.total.formatToman(),
                        onClick = { onOpenInvoice(invoice.id) }
                    )
                }
            }
        }
    }
}
