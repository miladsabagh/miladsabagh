package com.goldshop.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goldshop.app.data.model.ShopSettings
import com.goldshop.app.ui.components.AppTextField
import com.goldshop.app.ui.components.PrimaryButton
import com.goldshop.app.ui.components.ScreenHeader
import com.goldshop.app.ui.components.SectionLabel

@Composable
fun SettingsScreen(
    settings: ShopSettings,
    onSave: (ShopSettings) -> Unit
) {
    var shopName by remember(settings) { mutableStateOf(settings.shopName) }
    var shopPhone by remember(settings) { mutableStateOf(settings.shopPhone) }
    var shopAddress by remember(settings) { mutableStateOf(settings.shopAddress) }
    var goldPrice by remember(settings) { mutableStateOf(settings.goldPricePerGram18.toString()) }
    var taxPercent by remember(settings) { mutableStateOf(settings.taxPercent.toString()) }
    var invoicePrefix by remember(settings) { mutableStateOf(settings.invoicePrefix) }
    var savedHint by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "تنظیمات",
            subtitle = "اطلاعات فروشگاه و نرخ طلا"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionLabel("فروشگاه")
            AppTextField(value = shopName, onValueChange = { shopName = it }, label = "نام فروشگاه")
            AppTextField(
                value = shopPhone,
                onValueChange = { shopPhone = it },
                label = "تلفن",
                keyboardType = KeyboardType.Phone
            )
            AppTextField(
                value = shopAddress,
                onValueChange = { shopAddress = it },
                label = "آدرس",
                singleLine = false
            )

            SectionLabel("قیمت‌گذاری")
            AppTextField(
                value = goldPrice,
                onValueChange = { goldPrice = it.filter { c -> c.isDigit() } },
                label = "قیمت هر گرم طلای ۱۸ عیار (ریال)",
                keyboardType = KeyboardType.Number
            )
            AppTextField(
                value = taxPercent,
                onValueChange = { taxPercent = it },
                label = "مالیات %",
                keyboardType = KeyboardType.Decimal
            )
            AppTextField(
                value = invoicePrefix,
                onValueChange = { invoicePrefix = it },
                label = "پیشوند شماره فاکتور"
            )

            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "ذخیره تنظیمات",
                onClick = {
                    onSave(
                        settings.copy(
                            shopName = shopName.trim().ifBlank { settings.shopName },
                            shopPhone = shopPhone.trim(),
                            shopAddress = shopAddress.trim(),
                            goldPricePerGram18 = goldPrice.toLongOrNull() ?: settings.goldPricePerGram18,
                            taxPercent = taxPercent.toDoubleOrNull() ?: 0.0,
                            invoicePrefix = invoicePrefix.trim().ifBlank { "INV" }
                        )
                    )
                    savedHint = true
                }
            )
            if (savedHint) {
                Text(text = "تنظیمات ذخیره شد.")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
