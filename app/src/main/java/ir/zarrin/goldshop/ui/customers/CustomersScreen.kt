package ir.zarrin.goldshop.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.CustomerEntity
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.PlainField
import ir.zarrin.goldshop.ui.components.SearchField
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen() {
    val container = LocalAppContainer.current
    val viewModel: CustomersViewModel = viewModel {
        CustomersViewModel(
            container.customerRepository,
            container.invoiceRepository,
            container.settingsRepository
        )
    }
    val state by viewModel.state.collectAsState()
    var editing by remember { mutableStateOf<CustomerEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("مشتریان") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = CustomerEntity(name = "") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "مشتری جدید")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "جستجوی نام یا شماره تماس"
                )
            }

            if (state.customers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "مشتری‌ای ثبت نشده",
                    message = "برای افزودن مشتری جدید روی دکمه + بزنید."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.customers, key = { it.id }) { customer ->
                        CustomerRow(
                            customer = customer,
                            purchases = state.purchaseTotals[customer.id] ?: 0L,
                            money = { state.settings.money(it) },
                            onClick = { editing = customer },
                            onDelete = { pendingDelete = customer }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    editing?.let { customer ->
        CustomerDialog(
            customer = customer,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            }
        )
    }

    pendingDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف مشتری") },
            text = { Text("آیا «${customer.name}» حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(customer)
                    pendingDelete = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun CustomerRow(
    customer: CustomerEntity,
    purchases: Long,
    money: (Long) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(3.dp))
                Text(
                    customer.phone.ifBlank { "بدون شماره تماس" }.toPersianDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (purchases > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "مجموع خرید: ${money(purchases)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CustomerDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var nationalId by remember { mutableStateOf(customer.nationalId) }
    var address by remember { mutableStateOf(customer.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer.id > 0) "ویرایش مشتری" else "مشتری جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlainField(value = name, onValueChange = { name = it }, label = "نام و نام خانوادگی")
                PlainField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "شماره تماس",
                    keyboardType = KeyboardType.Phone
                )
                PlainField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = "کد ملی (اختیاری)",
                    keyboardType = KeyboardType.Number
                )
                PlainField(
                    value = address,
                    onValueChange = { address = it },
                    label = "نشانی (اختیاری)",
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        customer.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            nationalId = nationalId.trim(),
                            address = address.trim()
                        )
                    )
                }
            ) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
