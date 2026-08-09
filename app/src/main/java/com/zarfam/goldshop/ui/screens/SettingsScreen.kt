package com.zarfam.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.domain.parseDecimal
import com.zarfam.goldshop.domain.parseMoney
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.ui.components.AppTextField
import com.zarfam.goldshop.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var shopName by remember { mutableStateOf(settings.shopName) }
    var shopPhone by remember { mutableStateOf(settings.shopPhone) }
    var shopAddress by remember { mutableStateOf(settings.shopAddress) }
    var goldPrice by remember { mutableStateOf(if (settings.goldPricePerGram18 > 0) settings.goldPricePerGram18.toString() else "") }
    var taxPercent by remember { mutableStateOf(settings.taxPercent.toString()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (!loaded && settings != com.zarfam.goldshop.data.db.ShopSettings()) {
            shopName = settings.shopName
            shopPhone = settings.shopPhone
            shopAddress = settings.shopAddress
            goldPrice = if (settings.goldPricePerGram18 > 0) settings.goldPricePerGram18.toString() else ""
            taxPercent = settings.taxPercent.toString()
            loaded = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("مشخصات فروشگاه", style = MaterialTheme.typography.titleMedium)
            AppTextField(shopName, { shopName = it }, "نام فروشگاه")
            AppTextField(shopPhone, { shopPhone = it }, "تلفن فروشگاه", numeric = true)
            AppTextField(shopAddress, { shopAddress = it }, "آدرس فروشگاه", singleLine = false)

            Text("قیمت و مالیات", style = MaterialTheme.typography.titleMedium)
            AppTextField(
                value = goldPrice,
                onValueChange = { goldPrice = it },
                label = "قیمت هر گرم طلای ۱۸ عیار",
                numeric = true,
                suffix = "تومان",
                supportingText = goldPrice.parseMoney()?.let { "${it.toMoney()} تومان" },
            )
            AppTextField(
                value = taxPercent,
                onValueChange = { taxPercent = it },
                label = "درصد مالیات بر ارزش افزوده",
                decimal = true,
                suffix = "٪",
                supportingText = "مالیات فقط بر اجرت ساخت و سود فروشنده اعمال می‌شود",
            )

            Button(
                onClick = {
                    viewModel.save(
                        settings.copy(
                            shopName = shopName.trim().ifBlank { "گالری طلا و جواهر" },
                            shopPhone = shopPhone.trim(),
                            shopAddress = shopAddress.trim(),
                            goldPricePerGram18 = goldPrice.parseMoney() ?: 0,
                            taxPercent = taxPercent.parseDecimal() ?: 9.0,
                        ),
                    )
                    scope.launch { snackbarHostState.showSnackbar("تنظیمات ذخیره شد") }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Text("ذخیره تنظیمات")
            }
        }
    }
}
