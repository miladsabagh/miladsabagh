package com.miladsabagh.zarrin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.ui.SettingsViewModel
import com.miladsabagh.zarrin.ui.components.ZarrinTopBar
import com.miladsabagh.zarrin.util.parseAmountToLong

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsContent(
        settings = settings,
        onSave = { goldPrice, profit, tax, name, phone, address ->
            viewModel.updateGoldPrice(goldPrice)
            viewModel.updateProfitPercent(profit)
            viewModel.updateTaxPercent(tax)
            viewModel.updateStoreInfo(name, phone, address)
        },
        onBack = onBack
    )
}

@Composable
fun SettingsContent(
    settings: ShopSettings,
    onSave: (goldPrice: Long, profit: Double, tax: Double, name: String, phone: String, address: String) -> Unit,
    onBack: () -> Unit
) {
    var goldPriceText by remember(settings.goldPricePerGram18k) {
        mutableStateOf(settings.goldPricePerGram18k.toString())
    }
    var profitText by remember(settings.profitPercent) {
        mutableStateOf(settings.profitPercent.toString())
    }
    var taxText by remember(settings.taxPercent) {
        mutableStateOf(settings.taxPercent.toString())
    }
    var storeName by remember(settings.storeName) { mutableStateOf(settings.storeName) }
    var storePhone by remember(settings.storePhone) { mutableStateOf(settings.storePhone) }
    var storeAddress by remember(settings.storeAddress) { mutableStateOf(settings.storeAddress) }

    Scaffold(
        topBar = { ZarrinTopBar(title = "تنظیمات", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("قیمت‌گذاری", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = goldPriceText,
                        onValueChange = { goldPriceText = it },
                        label = { Text("قیمت هر گرم طلای ۱۸ عیار (تومان)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = profitText,
                        onValueChange = { profitText = it },
                        label = { Text("درصد سود فروشنده") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taxText,
                        onValueChange = { taxText = it },
                        label = { Text("درصد مالیات ارزش افزوده") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("مشخصات فروشگاه (در فاکتور چاپ می‌شود)", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("نام فروشگاه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = storePhone,
                        onValueChange = { storePhone = it },
                        label = { Text("تلفن فروشگاه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("آدرس فروشگاه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    onSave(
                        goldPriceText.parseAmountToLong() ?: settings.goldPricePerGram18k,
                        profitText.toDoubleOrNull() ?: settings.profitPercent,
                        taxText.toDoubleOrNull() ?: settings.taxPercent,
                        storeName.trim(),
                        storePhone.trim(),
                        storeAddress.trim()
                    )
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("ذخیره تنظیمات", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
