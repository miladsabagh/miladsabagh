package com.goldshop.app.ui.screens.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goldshop.app.data.model.Customer
import com.goldshop.app.ui.components.AppTextField
import com.goldshop.app.ui.components.EmptyState
import com.goldshop.app.ui.components.ProductRow
import com.goldshop.app.ui.components.ScreenHeader
import com.goldshop.app.ui.theme.Gold
import com.goldshop.app.ui.theme.Ink
import com.goldshop.app.util.toPersianDigits

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    onSave: (
        id: Long,
        name: String,
        phone: String,
        nationalId: String,
        address: String,
        notes: String
    ) -> Unit,
    onDelete: (Customer) -> Unit,
    onSelectForInvoice: (Customer) -> Unit
) {
    var editing by remember { mutableStateOf<Customer?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showEditor = true
                },
                containerColor = Gold,
                contentColor = Ink
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن مشتری")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenHeader(
                title = "مشتریان",
                subtitle = "${customers.size.toPersianDigits()} مشتری ثبت‌شده"
            )

            if (customers.isEmpty()) {
                EmptyState(
                    message = "لیست مشتریان خالی است.",
                    actionLabel = "افزودن مشتری",
                    onAction = {
                        editing = null
                        showEditor = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        ProductRow(
                            title = customer.fullName,
                            subtitle = buildString {
                                append(customer.phone.ifBlank { "بدون موبایل" }.toPersianDigits())
                                if (customer.address.isNotBlank()) {
                                    append(" · ")
                                    append(customer.address)
                                }
                            },
                            price = "انتخاب برای فاکتور",
                            trailing = {
                                IconButton(onClick = {
                                    editing = customer
                                    showEditor = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = { onDelete(customer) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف")
                                }
                            },
                            onClick = { onSelectForInvoice(customer) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showEditor) {
        CustomerEditorDialog(
            customer = editing,
            onDismiss = { showEditor = false },
            onSave = { id, name, phone, nationalId, address, notes ->
                onSave(id, name, phone, nationalId, address, notes)
                showEditor = false
            }
        )
    }
}

@Composable
private fun CustomerEditorDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        phone: String,
        nationalId: String,
        address: String,
        notes: String
    ) -> Unit
) {
    var name by remember(customer) { mutableStateOf(customer?.fullName.orEmpty()) }
    var phone by remember(customer) { mutableStateOf(customer?.phone.orEmpty()) }
    var nationalId by remember(customer) { mutableStateOf(customer?.nationalId.orEmpty()) }
    var address by remember(customer) { mutableStateOf(customer?.address.orEmpty()) }
    var notes by remember(customer) { mutableStateOf(customer?.notes.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "مشتری جدید" else "ویرایش مشتری") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTextField(value = name, onValueChange = { name = it }, label = "نام و نام خانوادگی")
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "موبایل",
                    keyboardType = KeyboardType.Phone
                )
                AppTextField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = "کد ملی",
                    keyboardType = KeyboardType.Number
                )
                AppTextField(value = address, onValueChange = { address = it }, label = "آدرس", singleLine = false)
                AppTextField(value = notes, onValueChange = { notes = it }, label = "یادداشت", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(customer?.id ?: 0L, name, phone, nationalId, address, notes)
                    }
                }
            ) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
