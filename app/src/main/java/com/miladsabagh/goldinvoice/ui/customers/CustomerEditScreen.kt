package com.miladsabagh.goldinvoice.ui.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.miladsabagh.goldinvoice.ui.components.LabeledNumberField
import com.miladsabagh.goldinvoice.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    viewModel: CustomerEditViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val saved by viewModel.saveCompleted.collectAsState()
    val isEditing = state.id != 0L

    LaunchedEffect(saved) { if (saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) R.string.customer_edit_title else R.string.customer_add_title)) },
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
                    label = stringResource(R.string.customer_name),
                    value = state.fullName,
                    onValueChange = { value -> viewModel.update { it.copy(fullName = value, nameError = false) } },
                    isError = state.nameError,
                    supportingText = if (state.nameError) stringResource(R.string.error_required_field) else null
                )
                LabeledNumberField(
                    label = stringResource(R.string.customer_phone),
                    value = state.phone,
                    onValueChange = { value -> viewModel.update { it.copy(phone = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.customer_address),
                    value = state.address,
                    onValueChange = { value -> viewModel.update { it.copy(address = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.customer_national_id),
                    value = state.nationalId,
                    onValueChange = { value -> viewModel.update { it.copy(nationalId = value) } }
                )
                LabeledNumberField(
                    label = stringResource(R.string.customer_notes),
                    value = state.notes,
                    onValueChange = { value -> viewModel.update { it.copy(notes = value) } }
                )
            }

            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
