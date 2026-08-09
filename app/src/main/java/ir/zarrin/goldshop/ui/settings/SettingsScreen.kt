package ir.zarrin.goldshop.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.domain.Currency
import ir.zarrin.goldshop.domain.Karats
import ir.zarrin.goldshop.domain.PricingEngine
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.LabeledRow
import ir.zarrin.goldshop.ui.components.PlainField
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.asAmount
import ir.zarrin.goldshop.ui.components.asDecimal
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenReports: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(container.settingsRepository)
    }
    val settings by viewModel.settings.collectAsState()

    var shopName by remember { mutableStateOf(settings.shopName) }
    var ownerName by remember { mutableStateOf(settings.ownerName) }
    var phone by remember { mutableStateOf(settings.phone) }
    var address by remember { mutableStateOf(settings.address) }
    var rate by remember { mutableStateOf("") }
    var tax by remember { mutableStateOf(settings.taxPercent.toString()) }
    var wage by remember { mutableStateOf(settings.defaultWagePercent.toString()) }
    var profit by remember { mutableStateOf(settings.defaultProfitPercent.toString()) }

    LaunchedEffect(settings.shopName, settings.ownerName, settings.phone, settings.address) {
        shopName = settings.shopName
        ownerName = settings.ownerName
        phone = settings.phone
        address = settings.address
    }
    LaunchedEffect(settings.goldRate18, settings.currency) {
        rate = settings.display(settings.goldRate18).toString()
    }
    LaunchedEffect(settings.taxPercent, settings.defaultWagePercent, settings.defaultProfitPercent) {
        tax = settings.taxPercent.toString()
        wage = settings.defaultWagePercent.toString()
        profit = settings.defaultProfitPercent.toString()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(title = "مشخصات فروشگاه") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlainField(shopName, { shopName = it }, "نام فروشگاه")
                        PlainField(ownerName, { ownerName = it }, "نام مدیر / فروشنده")
                        PlainField(phone, { phone = it }, "تلفن", keyboardType = KeyboardType.Phone)
                        PlainField(address, { address = it }, "نشانی", singleLine = false, minLines = 2)
                        Button(
                            onClick = { viewModel.saveShopInfo(shopName, ownerName, phone, address) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("ذخیره مشخصات") }
                    }
                }
            }

            item {
                SectionCard(title = "نرخ روز طلا") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AmountField(
                            value = rate,
                            onValueChange = { rate = it },
                            label = "نرخ هر گرم طلای ۱۸ عیار",
                            suffix = settings.currencyLabel
                        )
                        Button(
                            onClick = {
                                val entered = rate.asAmount()
                                if (entered > 0) {
                                    viewModel.saveGoldRate(entered / settings.currency.multiplier)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("ثبت نرخ") }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "نرخ سایر عیارها بر اساس نرخ ۱۸ عیار:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Karats.COMMON.forEach { karat ->
                            LabeledRow(
                                "هر گرم ${karat.toPersianDigits()} عیار",
                                settings.money(PricingEngine.ratePerGram(settings.goldRate18, karat))
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "مقادیر پیش‌فرض محاسبات") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DecimalField(tax, { tax = it }, "مالیات بر ارزش افزوده", suffix = "٪")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DecimalField(
                                wage,
                                { wage = it },
                                "اجرت پیش‌فرض",
                                suffix = "٪",
                                modifier = Modifier.weight(1f)
                            )
                            DecimalField(
                                profit,
                                { profit = it },
                                "سود پیش‌فرض",
                                suffix = "٪",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            "مالیات بر ارزش افزوده طلا فقط بر اجرت ساخت و سود فروشنده اعمال می‌شود.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                viewModel.saveDefaults(
                                    tax.asDecimal(),
                                    wage.asDecimal(),
                                    profit.asDecimal()
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("ذخیره مقادیر") }
                    }
                }
            }

            item {
                SectionCard(title = "نمایش") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("واحد پول", style = MaterialTheme.typography.labelMedium)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            Currency.entries.forEachIndexed { index, currency ->
                                SegmentedButton(
                                    selected = settings.currency == currency,
                                    onClick = { viewModel.setCurrency(currency) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = Currency.entries.size
                                    )
                                ) { Text(currency.label) }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("حالت تاریک", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = settings.darkTheme,
                                onCheckedChange = viewModel::setDarkTheme
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onOpenReports,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text("مشاهده گزارش فروش")
                }
            }

            item {
                SectionCard(title = "درباره برنامه") {
                    Column {
                        Text(
                            "زرین - نرم‌افزار مدیریت فروش طلا و جواهر",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "نسخه ۱٫۰٫۰ • تمام اطلاعات به صورت محلی روی همین دستگاه ذخیره می‌شود.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
