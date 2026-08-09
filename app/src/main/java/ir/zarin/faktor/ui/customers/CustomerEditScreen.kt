package ir.zarin.faktor.ui.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.ui.common.ConfirmDialog
import ir.zarin.faktor.ui.common.SectionCard
import ir.zarin.faktor.ui.common.ZarinTextField
import ir.zarin.faktor.ui.common.ZarinTopBar

@Composable
fun CustomerEditScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerEditViewModel = viewModel(factory = CustomerEditViewModel.Factory),
) {
    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val current = customer ?: return

    Scaffold(
        modifier = modifier,
        topBar = {
            ZarinTopBar(
                title = stringResource(if (viewModel.isNew) R.string.add_customer else R.string.edit_customer),
                onBack = onDone,
                actions = {
                    if (!viewModel.isNew) {
                        var confirming by remember { mutableStateOf(false) }
                        IconButton(onClick = { confirming = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                        if (confirming) {
                            ConfirmDialog(
                                text = stringResource(R.string.delete_customer_confirm),
                                onConfirm = {
                                    confirming = false
                                    viewModel.delete(onDone)
                                },
                                onDismiss = { confirming = false },
                                confirmLabel = stringResource(R.string.delete),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        CustomerEditForm(
            initial = current,
            onSave = { updated -> viewModel.save(updated, onDone) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
fun CustomerEditForm(
    initial: Customer,
    onSave: (Customer) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(initial.id) { mutableStateOf(initial.name) }
    var phone by remember(initial.id) { mutableStateOf(initial.phone) }
    var nationalCode by remember(initial.id) { mutableStateOf(initial.nationalCode) }
    var address by remember(initial.id) { mutableStateOf(initial.address) }
    var note by remember(initial.id) { mutableStateOf(initial.note) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZarinTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.customer_name),
                )
                ZarinTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(R.string.phone),
                    keyboardType = KeyboardType.Phone,
                )
                ZarinTextField(
                    value = nationalCode,
                    onValueChange = { nationalCode = it },
                    label = "${stringResource(R.string.national_code)} (${stringResource(R.string.optional)})",
                    keyboardType = KeyboardType.Number,
                )
                ZarinTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = stringResource(R.string.address),
                    singleLine = false,
                    minLines = 2,
                )
                ZarinTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(R.string.note),
                    singleLine = false,
                    minLines = 2,
                )
            }
        }

        Button(
            onClick = {
                onSave(
                    initial.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        nationalCode = nationalCode.trim(),
                        address = address.trim(),
                        note = note.trim(),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
