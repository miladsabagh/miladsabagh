package ir.zarrin.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarrin.gold.ui.AppViewModel
import ir.zarrin.gold.ui.components.AppTextField
import ir.zarrin.gold.util.PersianFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()

    var storeName by remember(settings.storeName) { mutableStateOf(settings.storeName) }
    var storePhone by remember(settings.storePhone) { mutableStateOf(settings.storePhone) }
    var storeAddress by remember(settings.storeAddress) { mutableStateOf(settings.storeAddress) }
    var goldPrice by remember(settings.goldPricePerGram18k) {
        mutableStateOf(if (settings.goldPricePerGram18k > 0) settings.goldPricePerGram18k.toString() else "")
    }
    var taxPercent by remember(settings.taxPercent) {
        mutableStateOf(settings.taxPercent.toString().removeSuffix(".0"))
    }
    var profitPercent by remember(settings.profitPercent) {
        mutableStateOf(settings.profitPercent.toString().removeSuffix(".0"))
    }

    val goldPriceVal = PersianFormat.parseLong(goldPrice) ?: 0L
    val taxVal = PersianFormat.parseDouble(taxPercent)
    val profitVal = PersianFormat.parseDouble(profitPercent)
    val valid = storeName.isNotBlank() && taxVal != null && taxVal >= 0 &&
        profitVal != null && profitVal >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات فروشگاه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("مشخصات فروشگاه (در سربرگ فاکتور چاپ می‌شود)",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            AppTextField(value = storeName, onValueChange = { storeName = it }, label = "نام فروشگاه")
            AppTextField(value = storePhone, onValueChange = { storePhone = it }, label = "تلفن", numeric = true)
            AppTextField(value = storeAddress, onValueChange = { storeAddress = it }, label = "آدرس", singleLine = false)

            Spacer(Modifier.height(4.dp))
            Text("پارامترهای قیمت‌گذاری",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            AppTextField(
                value = goldPrice, onValueChange = { goldPrice = it },
                label = "قیمت هر گرم طلای ۱۸ عیار", numeric = true, suffix = "تومان",
            )
            AppTextField(
                value = taxPercent, onValueChange = { taxPercent = it },
                label = "نرخ مالیات بر ارزش افزوده", numeric = true, suffix = "٪",
            )
            AppTextField(
                value = profitPercent, onValueChange = { profitPercent = it },
                label = "درصد سود فروشنده", numeric = true, suffix = "٪",
            )
            Text(
                "مالیات فقط به اجرت و سود تعلق می‌گیرد؛ به اصل طلا مالیات محاسبه نمی‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.saveSettings(
                        settings.copy(
                            storeName = storeName.trim(),
                            storePhone = PersianFormat.toEnglishDigits(storePhone.trim()),
                            storeAddress = storeAddress.trim(),
                            goldPricePerGram18k = goldPriceVal,
                            taxPercent = taxVal ?: 10.0,
                            profitPercent = profitVal ?: 7.0,
                        )
                    )
                    onBack()
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("ذخیره تنظیمات") }
        }
    }
}
