package com.zarrin.goldshop.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.data.ShopSettingsEntity
import com.zarrin.goldshop.domain.parseDoubleOrZero
import com.zarrin.goldshop.domain.parseLongOrZero
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.AppTextField
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.components.ScreenScaffold
import com.zarrin.goldshop.ui.components.SectionLabel

@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var shopName by remember { mutableStateOf(settings.shopName) }
    var shopPhone by remember { mutableStateOf(settings.shopPhone) }
    var shopAddress by remember { mutableStateOf(settings.shopAddress) }
    var goldPrice by remember { mutableStateOf(settings.goldPrice18PerGram.toString()) }
    var profit by remember { mutableStateOf(settings.profitPercent.toString()) }
    var vat by remember { mutableStateOf(settings.vatPercent.toString()) }
    var prefix by remember { mutableStateOf(settings.invoicePrefix) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        shopName = settings.shopName
        shopPhone = settings.shopPhone
        shopAddress = settings.shopAddress
        goldPrice = settings.goldPrice18PerGram.toString()
        profit = settings.profitPercent.toString()
        vat = settings.vatPercent.toString()
        prefix = settings.invoicePrefix
    }

    ScreenScaffold(
        title = "تنظیمات فروشگاه",
        subtitle = "قیمت روز، سود و مشخصات فاکتور",
        onBack = onBack
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            SectionLabel("اطلاعات فروشگاه")
            AppTextField(shopName, { shopName = it; saved = false }, "نام فروشگاه")
            Spacer(Modifier.height(10.dp))
            AppTextField(shopPhone, { shopPhone = it; saved = false }, "تلفن", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(10.dp))
            AppTextField(shopAddress, { shopAddress = it; saved = false }, "آدرس", singleLine = false)
            Spacer(Modifier.height(18.dp))
            SectionLabel("قیمت‌گذاری")
            AppTextField(
                goldPrice,
                { goldPrice = it; saved = false },
                "قیمت هر گرم طلای ۱۸ (تومان)",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(10.dp))
            AppTextField(profit, { profit = it; saved = false }, "سود فروشنده (٪)", keyboardType = KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            AppTextField(vat, { vat = it; saved = false }, "مالیات بر ارزش افزوده (٪)", keyboardType = KeyboardType.Decimal)
            Spacer(Modifier.height(10.dp))
            AppTextField(prefix, { prefix = it; saved = false }, "پیشوند شماره فاکتور")
            Spacer(Modifier.height(20.dp))
            PrimaryActionButton(
                text = "ذخیره تنظیمات",
                onClick = {
                    viewModel.saveSettings(
                        ShopSettingsEntity(
                            shopName = shopName.trim().ifBlank { "زرین گالری" },
                            shopPhone = shopPhone.trim(),
                            shopAddress = shopAddress.trim(),
                            goldPrice18PerGram = parseLongOrZero(goldPrice).coerceAtLeast(1),
                            profitPercent = parseDoubleOrZero(profit),
                            vatPercent = parseDoubleOrZero(vat),
                            invoicePrefix = prefix.trim().ifBlank { "INV" }
                        )
                    )
                    saved = true
                }
            )
            if (saved) {
                Spacer(Modifier.height(12.dp))
                Text("تنظیمات ذخیره شد.")
            }
        }
    }
}
