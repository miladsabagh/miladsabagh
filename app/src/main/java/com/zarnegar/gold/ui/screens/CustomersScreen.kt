package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.ui.components.EmptyState
import com.zarnegar.gold.ui.components.TextInputField
import com.zarnegar.gold.ui.vm.CustomersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: CustomersViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Customer?>(null) }
    var pendingDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مشتریان") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Customer(fullName = "") }) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن مشتری")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("جستجوی نام، تلفن یا کد ملی") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val customers = state.filtered
            if (customers.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Groups,
                    title = "مشتری‌ای ثبت نشده است",
                    message = "با دکمهٔ + مشتری جدید اضافه کنید تا در فاکتورها قابل انتخاب باشد.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(customers, key = { it.id }) { customer ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editing = customer },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.fullName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (customer.phone.isNotBlank()) {
                                        Text(
                                            text = PersianText.formatCode(customer.phone),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (customer.address.isNotBlank()) {
                                        Text(
                                            text = customer.address,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = { pendingDelete = customer }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { customer ->
        CustomerDialog(
            initial = customer,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            },
        )
    }

    pendingDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف مشتری") },
            text = { Text("«${customer.fullName}» حذف شود؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(customer)
                    pendingDelete = null
                }) { Text("حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDelete = null }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun CustomerDialog(
    initial: Customer,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit,
) {
    var customer by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "مشتری جدید" else "ویرایش مشتری") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextInputField(
                    label = "نام و نام خانوادگی",
                    value = customer.fullName,
                    onValueChange = { customer = customer.copy(fullName = it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextInputField(
                    label = "شمارهٔ تماس",
                    value = customer.phone,
                    onValueChange = { customer = customer.copy(phone = it) },
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextInputField(
                    label = "کد ملی",
                    value = customer.nationalCode,
                    onValueChange = { customer = customer.copy(nationalCode = it) },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextInputField(
                    label = "نشانی",
                    value = customer.address,
                    onValueChange = { customer = customer.copy(address = it) },
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(customer) },
                enabled = customer.fullName.isNotBlank(),
            ) { Text("ذخیره") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
