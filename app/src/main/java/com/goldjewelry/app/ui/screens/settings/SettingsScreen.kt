package com.goldjewelry.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goldjewelry.app.data.model.ShopSettings
import com.goldjewelry.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var shopLicense by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("") }
    var taxPercent by remember { mutableStateOf("") }
    var invoicePrefix by remember { mutableStateOf("") }

    LaunchedEffect(settings) {
        settings?.let {
            shopName = it.shopName
            shopAddress = it.shopAddress
            shopPhone = it.shopPhone
            shopLicense = it.shopLicense
            goldPrice = it.goldPricePerGram.toString()
            taxPercent = it.taxPercent.toString()
            invoicePrefix = it.invoicePrefix
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "تنظیمات",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "اطلاعات فروشگاه",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsField("نام فروشگاه", shopName) { shopName = it }
                SettingsField("آدرس", shopAddress) { shopAddress = it }
                SettingsField("تلفن", shopPhone) { shopPhone = it }
                SettingsField("پروانه کسب", shopLicense) { shopLicense = it }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "تنظیمات مالی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingsField(
                    label = "نرخ طلا (ریال به ازای هر گرم)",
                    value = goldPrice,
                    onValueChange = { goldPrice = it },
                    keyboardType = KeyboardType.Number
                )
                SettingsField(
                    label = "درصد مالیات",
                    value = taxPercent,
                    onValueChange = { taxPercent = it },
                    keyboardType = KeyboardType.Decimal
                )
                SettingsField("پیشوند شماره فاکتور", invoicePrefix) { invoicePrefix = it }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val updated = ShopSettings(
                            shopName = shopName,
                            shopAddress = shopAddress,
                            shopPhone = shopPhone,
                            shopLicense = shopLicense,
                            goldPricePerGram = goldPrice.toLongOrNull() ?: 3_500_000,
                            taxPercent = taxPercent.toDoubleOrNull() ?: 9.0,
                            invoicePrefix = invoicePrefix.ifBlank { "INV" },
                            lastInvoiceNumber = settings?.lastInvoiceNumber ?: 1000
                        )
                        viewModel.updateSettings(updated)
                        scope.launch {
                            snackbarHostState.showSnackbar("تنظیمات ذخیره شد")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ذخیره تنظیمات")
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
