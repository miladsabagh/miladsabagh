@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.settings.ShopSettings
import ir.zarrin.goldshop.domain.model.Currency
import ir.zarrin.goldshop.ui.components.AmountField
import ir.zarrin.goldshop.ui.components.DecimalField
import ir.zarrin.goldshop.ui.components.DropdownSelector
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.ZarrinTextField
import ir.zarrin.goldshop.ui.viewmodel.SettingsViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun SettingsScreen(
    bottomBar: @Composable () -> Unit,
    onOpenReports: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات فروشگاه") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        if (state.loading) return@Scaffold

        SettingsForm(
            initial = state.settings,
            seeding = state.seeding,
            modifier = Modifier.padding(padding),
            onSave = viewModel::save,
            onSeed = viewModel::seedSampleData,
            onOpenReports = onOpenReports
        )
    }
}

@Composable
private fun SettingsForm(
    initial: ShopSettings,
    seeding: Boolean,
    modifier: Modifier = Modifier,
    onSave: (ShopSettings) -> Unit,
    onSeed: () -> Unit,
    onOpenReports: () -> Unit
) {
    var shopName by remember { mutableStateOf(initial.shopName) }
    var ownerName by remember { mutableStateOf(initial.ownerName) }
    var phone by remember { mutableStateOf(initial.phone) }
    var address by remember { mutableStateOf(initial.address) }
    var goldRateText by remember { mutableStateOf(initial.goldRate18.toString()) }
    var taxText by remember { mutableStateOf(initial.taxPercent.toString()) }
    var wageText by remember { mutableStateOf(initial.defaultWagePercent.toString()) }
    var profitText by remember { mutableStateOf(initial.defaultProfitPercent.toString()) }
    var currency by remember { mutableStateOf(initial.currency) }
    var footer by remember { mutableStateOf(initial.invoiceFooter) }

    val settings = initial.copy(
        shopName = shopName.trim(),
        ownerName = ownerName.trim(),
        phone = phone.trim(),
        address = address.trim(),
        goldRate18 = PersianNumbers.parseLong(goldRateText) ?: 0L,
        taxPercent = PersianNumbers.parseDouble(taxText) ?: 0.0,
        defaultWagePercent = PersianNumbers.parseDouble(wageText) ?: 0.0,
        defaultProfitPercent = PersianNumbers.parseDouble(profitText) ?: 0.0,
        currency = currency,
        invoiceFooter = footer
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "مشخصات فروشگاه", icon = Icons.Filled.Storefront) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZarrinTextField(value = shopName, onValueChange = { shopName = it }, label = "نام فروشگاه")
                ZarrinTextField(value = ownerName, onValueChange = { ownerName = it }, label = "نام مسئول / پروانه کسب")
                ZarrinTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "تلفن",
                    keyboardType = KeyboardType.Phone
                )
                ZarrinTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "نشانی",
                    singleLine = false,
                    minLines = 2
                )
            }
        }

        SectionCard(title = "قیمت‌گذاری پیش‌فرض", icon = Icons.Filled.Percent) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AmountField(
                    label = "نرخ هر گرم طلای ۱۸ عیار",
                    text = goldRateText,
                    onTextChange = { goldRateText = it },
                    currency = currency
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DecimalField(
                        label = "اجرت پیش‌فرض",
                        text = wageText,
                        onTextChange = { wageText = it },
                        suffix = "٪",
                        modifier = Modifier.weight(1f)
                    )
                    DecimalField(
                        label = "سود پیش‌فرض",
                        text = profitText,
                        onTextChange = { profitText = it },
                        suffix = "٪",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DecimalField(
                        label = "مالیات بر ارزش افزوده",
                        text = taxText,
                        onTextChange = { taxText = it },
                        suffix = "٪",
                        modifier = Modifier.weight(1f)
                    )
                    DropdownSelector(
                        label = "واحد پول",
                        options = Currency.entries,
                        selected = currency,
                        optionLabel = { it.label },
                        onSelect = { currency = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "طبق قانون، مالیات بر ارزش افزوده طلای ساخته‌شده تنها روی «اجرت و سود» محاسبه می‌شود؛ " +
                        "سکه و طلای آبشده معاف هستند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionCard(title = "متن پایین فاکتور", icon = Icons.Filled.Storefront) {
            ZarrinTextField(
                value = footer,
                onValueChange = { footer = it },
                label = "یادداشت چاپی",
                singleLine = false,
                minLines = 2
            )
        }

        Button(onClick = { onSave(settings) }, modifier = Modifier.fillMaxWidth()) {
            Text("ذخیره تنظیمات", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onSeed, enabled = !seeding, modifier = Modifier.fillMaxWidth()) {
            Text(if (seeding) "در حال افزودن..." else "افزودن داده‌های نمونه برای آزمایش")
        }
        OutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.BarChart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("مشاهده گزارش فروش")
        }
        Spacer(Modifier.height(80.dp))
    }
}
