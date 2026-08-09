package com.zarfam.goldshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarfam.goldshop.data.db.Customer
import com.zarfam.goldshop.domain.toPersianDigits
import com.zarfam.goldshop.ui.components.AppTextField
import com.zarfam.goldshop.ui.components.ConfirmDeleteDialog
import com.zarfam.goldshop.ui.components.EmptyState
import com.zarfam.goldshop.ui.viewmodel.CustomersViewModel

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel = viewModel(factory = CustomersViewModel.Factory),
) {
    val customers by viewModel.customers.collectAsState()
    var editing by remember { mutableStateOf<Customer?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Customer?>(null) }

    if (showDialog) {
        CustomerDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.upsert(it)
                showDialog = false
            },
        )
    }

    deleting?.let { customer ->
        ConfirmDeleteDialog(
            title = "حذف مشتری",
            text = "«${customer.name}» حذف شود؟",
            onConfirm = {
                viewModel.delete(customer)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن مشتری")
            }
        },
    ) { padding ->
        if (customers.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(Icons.Default.People, "مشتری‌ای ثبت نشده است. با دکمه + اضافه کنید.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(customers, key = { it.id }) { customer ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    customer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (customer.phone.isNotBlank()) {
                                    Text(
                                        customer.phone.toPersianDigits(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (customer.address.isNotBlank()) {
                                    Text(
                                        customer.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    editing = customer
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { deleting = customer }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerDialog(
    initial: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var nationalId by remember { mutableStateOf(initial?.nationalId ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "افزودن مشتری" else "ویرایش مشتری") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(name, { name = it }, "نام و نام خانوادگی *")
                AppTextField(phone, { phone = it }, "شماره تلفن", numeric = true)
                AppTextField(nationalId, { nationalId = it }, "کد ملی (اختیاری)", numeric = true)
                AppTextField(address, { address = it }, "آدرس (اختیاری)", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Customer(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            phone = phone.trim(),
                            nationalId = nationalId.trim(),
                            address = address.trim(),
                            createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                        ),
                    )
                },
            ) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
