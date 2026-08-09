package com.zarin.goldshop.ui.screens

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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarin.goldshop.ui.AppViewModel
import com.zarin.goldshop.ui.NumberField
import com.zarin.goldshop.ui.SectionCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var shopName by remember(settings.shopName) { mutableStateOf(settings.shopName) }
    var shopPhone by remember(settings.shopPhone) { mutableStateOf(settings.shopPhone) }
    var shopAddress by remember(settings.shopAddress) { mutableStateOf(settings.shopAddress) }
    var goldPrice by remember(settings.goldPricePerGram) { mutableStateOf(settings.goldPricePerGram.toString()) }
    var profit by remember(settings.profitPercent) { mutableStateOf(settings.profitPercent.toString()) }
    var tax by remember(settings.taxPercent) { mutableStateOf(settings.taxPercent.toString()) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = "نرخ روز طلا") {
            Spacer(Modifier.height(8.dp))
            NumberField(goldPrice, { goldPrice = it }, "قیمت هر گرم طلای ۱۸ عیار (تومان)", Modifier.fillMaxWidth())
        }

        SectionCard(title = "مشخصات فروشگاه") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("نام فروشگاه") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            NumberField(shopPhone, { shopPhone = it }, "تلفن فروشگاه", Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = shopAddress, onValueChange = { shopAddress = it }, label = { Text("آدرس") }, modifier = Modifier.fillMaxWidth())
        }

        SectionCard(title = "پارامترهای محاسبه") {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(profit, { profit = it }, "سود فروشنده (٪)", Modifier.weight(1f), decimal = true)
                NumberField(tax, { tax = it }, "مالیات (٪)", Modifier.weight(1f), decimal = true)
            }
        }

        Button(
            onClick = {
                vm.saveSettings(
                    settings.copy(
                        shopName = shopName.trim().ifBlank { "طلا و جواهر" },
                        shopPhone = shopPhone.trim(),
                        shopAddress = shopAddress.trim(),
                        goldPricePerGram = goldPrice.toLongOrNull() ?: settings.goldPricePerGram,
                        profitPercent = profit.toDoubleOrNull() ?: settings.profitPercent,
                        taxPercent = tax.toDoubleOrNull() ?: settings.taxPercent,
                    )
                )
                scope.launch { snackbar.showSnackbar("تنظیمات ذخیره شد") }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Text("  ذخیره تنظیمات")
        }

        Text(
            "نسخه ۱٫۰ — زرین طلا",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SnackbarHost(snackbar)
        Spacer(Modifier.height(16.dp))
    }
}
