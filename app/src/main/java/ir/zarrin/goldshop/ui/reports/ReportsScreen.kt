package ir.zarrin.goldshop.ui.reports

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.LabeledRow
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.StatCard
import ir.zarrin.goldshop.ui.theme.SuccessGreen
import ir.zarrin.goldshop.ui.theme.WarningAmber
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ReportsViewModel = viewModel {
        ReportsViewModel(container.invoiceRepository, container.settingsRepository)
    }
    val state by viewModel.state.collectAsState()
    val settings = state.settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("گزارش فروش") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "کل فروش",
                        value = settings.money(state.totalSales),
                        subtitle = "${state.invoiceCount.toPersianDigits()} فاکتور",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "وزن فروخته شده",
                        value = "${state.totalWeight.formatWeight()} گرم",
                        subtitle = "مجموع طلای وزنی",
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "جمع اجرت و سود",
                        value = settings.money(state.totalWage + state.totalProfit),
                        subtitle = "درآمد ناخالص",
                        accent = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "مالیات وصول شده",
                        value = settings.money(state.totalTax),
                        subtitle = "ارزش افزوده",
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionCard(title = "فروش هفت روز اخیر") {
                    BarChart(
                        buckets = state.lastDays,
                        money = { settings.money(it) }
                    )
                }
            }

            item {
                SectionCard(title = "فروش ماهانه سال جاری") {
                    Column {
                        state.monthlyBuckets.filter { it.count > 0 }.forEach { bucket ->
                            LabeledRow(
                                "${bucket.label} (${bucket.count.toPersianDigits()} فاکتور)",
                                settings.money(bucket.total)
                            )
                        }
                        if (state.monthlyBuckets.none { it.count > 0 }) {
                            Text(
                                "هنوز فروشی در سال جاری ثبت نشده است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "پرفروش‌ترین کالاها") {
                    Column {
                        if (state.topProducts.isEmpty()) {
                            Text(
                                "اطلاعاتی برای نمایش وجود ندارد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        state.topProducts.forEach { product ->
                            LabeledRow(
                                "${product.name} (${product.quantity.toPersianDigits()} عدد)",
                                settings.money(product.total)
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "تفکیک شیوه پرداخت") {
                    Column {
                        if (state.paymentBreakdown.isEmpty()) {
                            Text(
                                "اطلاعاتی برای نمایش وجود ندارد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        state.paymentBreakdown.forEach { (label, amount) ->
                            LabeledRow(label, settings.money(amount))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun BarChart(buckets: List<DayBucket>, money: (Long) -> String) {
    val max = buckets.maxOfOrNull { it.total } ?: 0L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buckets.forEach { bucket ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(74.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(9.dp)
                        )
                ) {
                    val fraction = if (max > 0) bucket.total.toFloat() / max.toFloat() else 0f
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                                .height(18.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(9.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (bucket.total > 0) money(bucket.total) else "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
