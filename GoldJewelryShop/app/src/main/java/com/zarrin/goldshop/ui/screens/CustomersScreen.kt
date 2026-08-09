package com.zarrin.goldshop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarrin.goldshop.data.CustomerEntity
import com.zarrin.goldshop.domain.toPersianDigits
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.components.AppTextField
import com.zarrin.goldshop.ui.components.EmptyState
import com.zarrin.goldshop.ui.components.ListDivider
import com.zarrin.goldshop.ui.components.PrimaryActionButton
import com.zarrin.goldshop.ui.components.ScreenScaffold

@Composable
fun CustomersScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<CustomerEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        CustomerEditor(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { customer ->
                viewModel.saveCustomer(
                    id = customer.id,
                    name = customer.fullName,
                    phone = customer.phone,
                    nationalId = customer.nationalId,
                    address = customer.address
                )
                showEditor = false
            }
        )
        return
    }

    ScreenScaffold(
        title = "مشتریان",
        subtitle = "اطلاعات خریداران برای صدور فاکتور",
        onBack = onBack,
        actions = {
            TextButton(onClick = {
                editing = null
                showEditor = true
            }) { Text("افزودن") }
        }
    ) {
        if (customers.isEmpty()) {
            EmptyState("مشتری ثبت نشده است")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(customers, key = { it.id }) { customer ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editing = customer
                                showEditor = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(customer.fullName, style = MaterialTheme.typography.titleMedium)
                        if (customer.phone.isNotBlank()) {
                            Text(
                                toPersianDigits(customer.phone),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (customer.address.isNotBlank()) {
                            Text(
                                customer.address,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        TextButton(onClick = { viewModel.deleteCustomer(customer) }) {
                            Text("حذف", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    ListDivider()
                }
            }
        }
    }
}

@Composable
private fun CustomerEditor(
    initial: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.fullName.orEmpty()) }
    var phone by remember { mutableStateOf(initial?.phone.orEmpty()) }
    var nationalId by remember { mutableStateOf(initial?.nationalId.orEmpty()) }
    var address by remember { mutableStateOf(initial?.address.orEmpty()) }

    ScreenScaffold(
        title = if (initial == null) "مشتری جدید" else "ویرایش مشتری",
        onBack = onDismiss
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            AppTextField(name, { name = it }, "نام و نام خانوادگی")
            Spacer(Modifier.height(10.dp))
            AppTextField(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(10.dp))
            AppTextField(nationalId, { nationalId = it }, "کد ملی", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(10.dp))
            AppTextField(address, { address = it }, "آدرس", singleLine = false)
            Spacer(Modifier.height(20.dp))
            PrimaryActionButton(
                text = "ذخیره مشتری",
                onClick = {
                    if (name.isBlank()) return@PrimaryActionButton
                    onSave(
                        CustomerEntity(
                            id = initial?.id ?: 0,
                            fullName = name,
                            phone = phone,
                            nationalId = nationalId,
                            address = address
                        )
                    )
                }
            )
        }
    }
}
