package com.zarrin.goldshop.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.domain.formatTomanFa
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.GoldAccentBar
import com.zarrin.goldshop.ui.components.MetricTile
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.theme.GoldPrimary

@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNewInvoice: () -> Unit,
    onProducts: () -> Unit,
    onCustomers: () -> Unit,
    onInvoices: () -> Unit,
    onSettings: () -> Unit
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val fade = remember { Animatable(0f) }
    val slide = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        viewModel.refreshStats()
        fade.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        slide.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .alpha(fade.value)
                .padding(top = slide.value.dp)
        ) {
            Text(
                text = settings.shopName,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            GoldAccentBar()
            Spacer(Modifier.height(14.dp))
            Text(
                text = "فروش طلا و جواهر با صدور فاکتور حرفه‌ای",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            PrimaryActionButton(text = "صدور فاکتور جدید", onClick = onNewInvoice)
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "فروش امروز",
                    value = formatTomanFa(stats.todaySales),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "فروش ماه",
                    value = formatTomanFa(stats.monthSales),
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "فاکتورها",
                    value = toPersianDigits(stats.invoiceCount.toString()),
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "موجودی کم",
                    value = toPersianDigits(stats.lowStockCount.toString()),
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "قیمت روز طلای ۱۸ عیار",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatTomanFa(settings.goldPrice18PerGram),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onSettings) {
                Text("ویرایش تنظیمات و قیمت روز")
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "دسترسی سریع",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            QuickLink(Icons.AutoMirrored.Outlined.ReceiptLong, "فاکتورها", "مشاهده و اشتراک‌گذاری", onInvoices)
            QuickLink(Icons.Outlined.Category, "کالاها", "مدیریت طلا و جواهر", onProducts)
            QuickLink(Icons.Outlined.Person, "مشتریان", "دفترچه مشتریان", onCustomers)
            QuickLink(Icons.Outlined.ShoppingBag, "فاکتور جدید", "محاسبه و صدور", onNewInvoice)
            QuickLink(Icons.Outlined.Settings, "تنظیمات فروشگاه", "قیمت، سود، مالیات", onSettings)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickLink(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = GoldPrimary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
