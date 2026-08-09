package com.miladsabagh.goldshop.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.ui.LocalViewModelFactory
import com.miladsabagh.goldshop.ui.components.NumberField
import com.miladsabagh.goldshop.util.JalaliDate
import com.miladsabagh.goldshop.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var storeName by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var laborFee by remember { mutableStateOf(7.0) }
    var profit by remember { mutableStateOf(7.0) }
    var tax by remember { mutableStateOf(9.0) }
    var goldPrice by remember { mutableStateOf(0.0) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.settings) {
        if (!initialized) {
            storeName = state.settings.storeName
            storePhone = state.settings.storePhone
            storeAddress = state.settings.storeAddress
            laborFee = state.settings.defaultLaborFeePercent
            profit = state.settings.defaultProfitPercent
            tax = state.settings.defaultTaxPercent
            goldPrice = state.settings.currentGoldPrice
            initialized = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("تنظیمات") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اطلاعات فروشگاه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("نام فروشگاه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = storePhone,
                        onValueChange = { storePhone = it },
                        label = { Text("شماره تماس") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("آدرس") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.updateStoreInfo(storeName, storePhone, storeAddress) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ذخیره اطلاعات فروشگاه") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("مقادیر پیش‌فرض فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(label = "اجرت ساخت", value = laborFee, onValueChange = { laborFee = it }, suffix = "%", modifier = Modifier.weight(1f))
                        NumberField(label = "سود فروشنده", value = profit, onValueChange = { profit = it }, suffix = "%", modifier = Modifier.weight(1f))
                    }
                    NumberField(label = "مالیات ارزش افزوده", value = tax, onValueChange = { tax = it }, suffix = "%")
                    Button(
                        onClick = { viewModel.updateDefaults(laborFee, profit, tax) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ذخیره مقادیر پیش‌فرض") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("نرخ روز طلا", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    NumberField(label = "نرخ هر گرم طلا ۱۸ عيار (تومان)", value = goldPrice, onValueChange = { goldPrice = it })
                    Button(
                        onClick = { viewModel.updateGoldPrice(goldPrice) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ثبت نرخ جدید") }

                    if (state.priceHistory.isNotEmpty()) {
                        Text("تاریخچه اخیر نرخ طلا", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        state.priceHistory.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(JalaliDate.formatNumeric(entry.recordedAt), color = MaterialTheme.colorScheme.outline)
                                Text(PersianFormat.formatToman(entry.pricePerGram18k))
                            }
                        }
                    }
                }
            }
        }
    }
}
