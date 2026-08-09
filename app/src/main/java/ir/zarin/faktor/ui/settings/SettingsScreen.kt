package ir.zarin.faktor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.core.PersianNumbers
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.ui.common.ChoiceChips
import ir.zarin.faktor.ui.common.FieldLabel
import ir.zarin.faktor.ui.common.NumericField
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.ZarinTextField

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.settings_saved)

    LaunchedEffect(saved) {
        if (saved) {
            snackbarHostState.showSnackbar(savedText)
            viewModel.consumeSaved()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        settings?.let { current ->
            SettingsContent(
                initial = current,
                onSave = viewModel::save,
                modifier = Modifier.weight(1f),
            )
        }
        SnackbarHost(snackbarHostState)
    }
}

@Composable
fun SettingsContent(
    initial: AppSettings,
    onSave: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var shopName by remember { mutableStateOf(initial.shopName) }
    var shopPhone by remember { mutableStateOf(initial.shopPhone) }
    var shopAddress by remember { mutableStateOf(initial.shopAddress) }
    var currencyUnit by remember { mutableStateOf(initial.currencyUnit) }
    var goldRate by remember {
        mutableStateOf(
            if (initial.goldRatePerGramRial == 0L) {
                ""
            } else {
                initial.currencyUnit.fromRial(initial.goldRatePerGramRial).toString()
            },
        )
    }
    var profitPercent by remember { mutableStateOf(PersianNumbers.decimal(initial.profitPercent)) }
    var vatPercent by remember { mutableStateOf(PersianNumbers.decimal(initial.vatPercent)) }
    var invoicePrefix by remember { mutableStateOf(initial.invoicePrefix) }
    var persianDigits by remember { mutableStateOf(initial.persianDigits) }

    val unitLabel = stringResource(if (currencyUnit == CurrencyUnit.RIAL) R.string.rial else R.string.toman)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(title = stringResource(R.string.shop_info)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZarinTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = stringResource(R.string.shop_name),
                )
                ZarinTextField(
                    value = shopPhone,
                    onValueChange = { shopPhone = it },
                    label = stringResource(R.string.shop_phone),
                    keyboardType = KeyboardType.Phone,
                )
                ZarinTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = stringResource(R.string.shop_address),
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        SectionCard(title = stringResource(R.string.pricing_settings)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    FieldLabel(stringResource(R.string.currency_unit))
                    ChoiceChips(
                        options = CurrencyUnit.entries,
                        selected = currencyUnit,
                        onSelect = { currencyUnit = it },
                        label = {
                            stringResource(if (it == CurrencyUnit.RIAL) R.string.rial else R.string.toman)
                        },
                    )
                }
                NumericField(
                    value = goldRate,
                    onValueChange = { goldRate = it },
                    label = stringResource(R.string.gold_rate_title),
                    suffix = unitLabel,
                )
                NumericField(
                    value = profitPercent,
                    onValueChange = { profitPercent = it },
                    label = stringResource(R.string.profit_percent),
                    suffix = "٪",
                    decimal = true,
                )
                NumericField(
                    value = vatPercent,
                    onValueChange = { vatPercent = it },
                    label = stringResource(R.string.vat_percent),
                    suffix = "٪",
                    decimal = true,
                )
                Text(
                    text = stringResource(R.string.formula_help),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionCard(title = stringResource(R.string.settings_title)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZarinTextField(
                    value = invoicePrefix,
                    onValueChange = { invoicePrefix = it },
                    label = "${stringResource(R.string.invoice_prefix)} (${stringResource(R.string.optional)})",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.persian_digits),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(checked = persianDigits, onCheckedChange = { persianDigits = it })
                }
            }
        }

        Button(
            onClick = {
                onSave(
                    initial.copy(
                        shopName = shopName.trim(),
                        shopPhone = shopPhone.trim(),
                        shopAddress = shopAddress.trim(),
                        goldRatePerGramRial = PersianNumbers.parseLong(goldRate)
                            ?.let { currencyUnit.toRial(it) } ?: 0L,
                        profitPercent = PersianNumbers.parseDouble(profitPercent) ?: 0.0,
                        vatPercent = PersianNumbers.parseDouble(vatPercent) ?: 0.0,
                        currencyUnit = currencyUnit,
                        invoicePrefix = invoicePrefix.trim(),
                        persianDigits = persianDigits,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save))
        }
        Text(
            text = "${stringResource(R.string.app_name)} — ${stringResource(R.string.app_tagline)}",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
    }
}
