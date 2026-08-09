package com.miladsabagh.goldinvoice.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.BuildConfig
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.ui.components.LabeledNumberField
import com.miladsabagh.goldinvoice.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val form by viewModel.form.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.settings_saved)

    LaunchedEffect(form.savedFlag) {
        if (form.savedFlag > 0) snackbarHostState.showSnackbar(savedMessage)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = stringResource(R.string.settings_shop_info)) {
                LabeledNumberField(
                    label = stringResource(R.string.settings_shop_name),
                    value = form.shopName,
                    onValueChange = { value -> viewModel.update { it.copy(shopName = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.settings_shop_phone),
                    value = form.shopPhone,
                    onValueChange = { value -> viewModel.update { it.copy(shopPhone = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.settings_shop_address),
                    value = form.shopAddress,
                    onValueChange = { value -> viewModel.update { it.copy(shopAddress = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.settings_shop_license),
                    value = form.shopLicenseNumber,
                    onValueChange = { value -> viewModel.update { it.copy(shopLicenseNumber = value) } }
                )
            }

            SectionCard(title = stringResource(R.string.settings_pricing_defaults)) {
                LabeledNumberField(
                    label = stringResource(R.string.settings_default_labor_fee),
                    value = form.defaultLaborFeePercent,
                    onValueChange = { value -> viewModel.update { it.copy(defaultLaborFeePercent = value) } },
                    suffix = "٪"
                )
                LabeledNumberField(
                    label = stringResource(R.string.settings_default_profit),
                    value = form.defaultProfitPercent,
                    onValueChange = { value -> viewModel.update { it.copy(defaultProfitPercent = value) } },
                    suffix = "٪"
                )
                LabeledNumberField(
                    label = stringResource(R.string.settings_default_tax),
                    value = form.defaultTaxPercent,
                    onValueChange = { value -> viewModel.update { it.copy(defaultTaxPercent = value) } },
                    suffix = "٪"
                )
            }

            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
