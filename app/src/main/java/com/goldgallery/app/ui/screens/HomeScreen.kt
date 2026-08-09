package com.goldgallery.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldgallery.app.logic.JalaliDate
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.ui.Screen
import com.goldgallery.app.ui.theme.BronzeDeep
import com.goldgallery.app.ui.theme.Gold
import com.goldgallery.app.ui.theme.GoldDark
import com.goldgallery.app.ui.theme.GoldLight
import com.goldgallery.app.ui.theme.GoldPale
import com.goldgallery.app.viewmodel.ShopViewModel

@Composable
fun HomeScreen(viewModel: ShopViewModel, onNavigate: (String) -> Unit) {
    val goldPrice by viewModel.goldPrice18.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    var showPriceDialog by remember { mutableStateOf(false) }

    val totalRevenue = invoices.sumOf { invoice -> invoice.items.sumOf { it.total } }
    val today = JalaliDate.formatLong(System.currentTimeMillis())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "گالری طلا و جواهر پارسه",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = today,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(GoldDark, Gold, GoldLight)),
                    RoundedCornerShape(20.dp),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = BronzeDeep,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "قیمت هر گرم طلای ۱۸ عیار",
                        color = BronzeDeep,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showPriceDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "ویرایش قیمت", tint = BronzeDeep)
                    }
                }
                Text(
                    PersianFormat.money(goldPrice),
                    color = BronzeDeep,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "برای محاسبه قیمت‌ها از این نرخ استفاده می‌شود",
                    color = BronzeDeep.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Diamond,
                value = PersianFormat.toPersianDigits(products.size.toString()),
                label = "قلم کالا",
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                value = PersianFormat.toPersianDigits(invoices.size.toString()),
                label = "فاکتور صادرشده",
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.PriceCheck,
                value = PersianFormat.number(totalRevenue),
                label = "فروش کل (تومان)",
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "محاسبه خودکار قیمت فروش",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "قیمت نهایی هر قلم = طلای خام + اجرت ساخت + ۷٪ سود + ۹٪ مالیات ارزش افزوده",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Button(
            onClick = { onNavigate(Screen.Cart.route) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("صدور فاکتور جدید", style = MaterialTheme.typography.titleMedium)
        }
        FilledTonalButton(
            onClick = { onNavigate(Screen.Products.route) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Outlined.Diamond, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("مشاهده محصولات", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showPriceDialog) {
        var input by remember { mutableStateOf(PersianFormat.toPersianDigits(goldPrice.toString())) }
        AlertDialog(
            onDismissRequest = { showPriceDialog = false },
            title = { Text("ویرایش قیمت طلا") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("قیمت هر گرم طلای ۱۸ عیار (تومان)", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it.filter { ch -> ch.isDigit() || ch in "۰۱۲۳۴۵۶۷۸۹" } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = PersianFormat.toEnglishDigits(input).toLongOrNull()
                        if (parsed != null) viewModel.setGoldPrice(parsed)
                        showPriceDialog = false
                    }
                ) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { showPriceDialog = false }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GoldPale),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = BronzeDeep, modifier = Modifier.size(22.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BronzeDeep,
                maxLines = 1,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = BronzeDeep.copy(alpha = 0.7f),
            )
        }
    }
}
