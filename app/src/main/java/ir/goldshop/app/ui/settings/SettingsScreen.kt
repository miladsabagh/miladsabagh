package ir.goldshop.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.goldshop.app.ui.components.NumberField
import ir.goldshop.app.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var economicCode by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("") }
    var laborPercent by remember { mutableStateOf("") }
    var profitPercent by remember { mutableStateOf("") }
    var taxPercent by remember { mutableStateOf("") }
    var applyTaxOnLaborProfitOnly by remember { mutableStateOf(true) }
    var footerNote by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (!initialized) {
            shopName = settings.shopName
            shopAddress = settings.shopAddress
            shopPhone = settings.shopPhone
            economicCode = settings.economicCode
            nationalId = settings.nationalId
            goldPrice = if (settings.goldPricePerGram == 0.0) "" else settings.goldPricePerGram.toString()
            laborPercent = settings.defaultLaborPercent.toString()
            profitPercent = settings.defaultProfitPercent.toString()
            taxPercent = settings.defaultTaxPercent.toString()
            applyTaxOnLaborProfitOnly = settings.applyTaxOnLaborAndProfitOnly
            footerNote = settings.invoiceFooterNote
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات فروشگاه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "نرخ روز طلا") {
                NumberField(
                    label = "نرخ هر گرم طلای ۱۸ عیار",
                    value = goldPrice,
                    onValueChange = { goldPrice = it },
                    suffix = "تومان"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        label = "درصد اجرت پیش‌فرض",
                        value = laborPercent,
                        onValueChange = { laborPercent = it },
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        label = "درصد سود پیش‌فرض",
                        value = profitPercent,
                        onValueChange = { profitPercent = it },
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberField(
                    label = "درصد مالیات ارزش‌افزوده",
                    value = taxPercent,
                    onValueChange = { taxPercent = it },
                    suffix = "%"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("مالیات فقط بر اجرت و سود اعمال شود", modifier = Modifier.weight(1f))
                    Switch(checked = applyTaxOnLaborProfitOnly, onCheckedChange = { applyTaxOnLaborProfitOnly = it })
                }
            }

            SectionCard(title = "اطلاعات فروشگاه (برای فاکتور)") {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("نام فروشگاه") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("آدرس فروشگاه") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shopPhone,
                    onValueChange = { shopPhone = it },
                    label = { Text("شماره تماس فروشگاه") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = economicCode,
                        onValueChange = { economicCode = it },
                        label = { Text("کد اقتصادی (اختیاری)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nationalId,
                        onValueChange = { nationalId = it },
                        label = { Text("شناسه ملی (اختیاری)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = footerNote,
                    onValueChange = { footerNote = it },
                    label = { Text("متن پایین فاکتور") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    viewModel.save(
                        settings.copy(
                            shopName = shopName.ifBlank { "فروشگاه طلا و جواهر" },
                            shopAddress = shopAddress,
                            shopPhone = shopPhone,
                            economicCode = economicCode,
                            nationalId = nationalId,
                            goldPricePerGram = goldPrice.toDoubleOrNull() ?: 0.0,
                            defaultLaborPercent = laborPercent.toDoubleOrNull() ?: 0.0,
                            defaultProfitPercent = profitPercent.toDoubleOrNull() ?: 0.0,
                            defaultTaxPercent = taxPercent.toDoubleOrNull() ?: 0.0,
                            applyTaxOnLaborAndProfitOnly = applyTaxOnLaborProfitOnly,
                            invoiceFooterNote = footerNote
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره تنظیمات")
            }
        }
    }
}
