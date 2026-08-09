package com.miladsabagh.goldinvoice.ui.products

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.entity.AVAILABLE_KARATS
import com.miladsabagh.goldinvoice.data.entity.JEWELRY_CATEGORIES
import com.miladsabagh.goldinvoice.ui.components.LabeledDropdown
import com.miladsabagh.goldinvoice.ui.components.LabeledNumberField
import com.miladsabagh.goldinvoice.ui.components.SectionCard
import com.miladsabagh.goldinvoice.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    viewModel: ProductEditViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val saved by viewModel.saveCompleted.collectAsState()
    val isEditing = state.id != 0L

    LaunchedEffect(saved) { if (saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) R.string.product_edit_title else R.string.product_add_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
            SectionCard {
                LabeledNumberField(
                    label = stringResource(R.string.product_name),
                    value = state.name,
                    onValueChange = { value -> viewModel.update { it.copy(name = value, nameError = false) } },
                    isError = state.nameError,
                    supportingText = if (state.nameError) stringResource(R.string.error_required_field) else null
                )
                LabeledDropdown(
                    label = stringResource(R.string.product_category),
                    options = JEWELRY_CATEGORIES,
                    selected = state.category,
                    optionLabel = { it },
                    onSelected = { value -> viewModel.update { it.copy(category = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.product_code),
                    value = state.code,
                    onValueChange = { value -> viewModel.update { it.copy(code = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.product_description),
                    value = state.description,
                    onValueChange = { value -> viewModel.update { it.copy(description = value) } }
                )
            }

            SectionCard(title = "مشخصات طلا") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    LabeledNumberField(
                        label = stringResource(R.string.product_weight),
                        value = state.weightGrams,
                        onValueChange = { value -> viewModel.update { it.copy(weightGrams = value, weightError = false) } },
                        modifier = Modifier.weight(1f),
                        suffix = stringResource(R.string.unit_gram),
                        isError = state.weightError,
                        supportingText = if (state.weightError) stringResource(R.string.error_invalid_number) else null
                    )
                    LabeledDropdown(
                        label = stringResource(R.string.product_karat),
                        options = AVAILABLE_KARATS,
                        selected = state.karat,
                        optionLabel = { it.toString().toPersianDigits() },
                        onSelected = { value -> viewModel.update { it.copy(karat = value) } },
                        modifier = Modifier.weight(1f)
                    )
                }
                LabeledNumberField(
                    label = stringResource(R.string.product_quantity),
                    value = state.quantity,
                    onValueChange = { value -> viewModel.update { it.copy(quantity = value) } },
                    suffix = stringResource(R.string.unit_count)
                )
            }

            SectionCard(title = "درصدهای قیمت‌گذاری") {
                LabeledNumberField(
                    label = stringResource(R.string.product_labor_fee),
                    value = state.laborFeePercent,
                    onValueChange = { value -> viewModel.update { it.copy(laborFeePercent = value) } },
                    suffix = "٪"
                )
                LabeledNumberField(
                    label = stringResource(R.string.product_profit),
                    value = state.profitPercent,
                    onValueChange = { value -> viewModel.update { it.copy(profitPercent = value) } },
                    suffix = "٪"
                )
                LabeledNumberField(
                    label = stringResource(R.string.product_tax),
                    value = state.taxPercent,
                    onValueChange = { value -> viewModel.update { it.copy(taxPercent = value) } },
                    suffix = "٪"
                )
            }

            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
