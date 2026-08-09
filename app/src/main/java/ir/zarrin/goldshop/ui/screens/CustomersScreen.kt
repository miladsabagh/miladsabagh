@file:OptIn(ExperimentalMaterial3Api::class)

package ir.zarrin.goldshop.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.local.Customer
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.SectionCard
import ir.zarrin.goldshop.ui.components.ZarrinTextField
import ir.zarrin.goldshop.ui.viewmodel.CustomerEditorViewModel
import ir.zarrin.goldshop.ui.viewmodel.CustomerListViewModel
import ir.zarrin.goldshop.ui.viewmodel.ZarrinViewModelFactory

@Composable
fun CustomerListScreen(
    bottomBar: @Composable () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    viewModel: CustomerListViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = { Text("دفتر مشتریان") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCustomer,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("مشتری جدید") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("جستجوی نام یا شماره تماس") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (state.customers.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Groups,
                    title = "مشتری‌ای ثبت نشده",
                    subtitle = "با ثبت مشتریان، صدور فاکتور سریع‌تر انجام می‌شود."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.customers, key = { it.id }) { customer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditCustomer(customer.id) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        customer.name.take(1),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        buildString {
                                            if (customer.phone.isNotBlank()) {
                                                append(PersianNumbers.toPersianDigits(customer.phone))
                                            }
                                            if (customer.address.isNotBlank()) {
                                                if (isNotEmpty()) append(" • ")
                                                append(customer.address)
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { pendingDelete = customer }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف مشتری") },
            text = { Text("«${customer.name}» از دفتر مشتریان حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(customer)
                    pendingDelete = null
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("انصراف") } }
        )
    }
}

@Composable
fun CustomerEditorScreen(
    onBack: () -> Unit,
    viewModel: CustomerEditorViewModel = viewModel(factory = ZarrinViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "مشتری جدید" else "ویرایش مشتری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(title = "اطلاعات مشتری", icon = Icons.Filled.Groups) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ZarrinTextField(
                        value = state.customer.name,
                        onValueChange = { value -> viewModel.edit { it.copy(name = value) } },
                        label = "نام و نام خانوادگی"
                    )
                    ZarrinTextField(
                        value = state.customer.phone,
                        onValueChange = { value -> viewModel.edit { it.copy(phone = value) } },
                        label = "شماره تماس",
                        keyboardType = KeyboardType.Phone
                    )
                    ZarrinTextField(
                        value = state.customer.nationalId,
                        onValueChange = { value -> viewModel.edit { it.copy(nationalId = value) } },
                        label = "کد ملی",
                        keyboardType = KeyboardType.Number
                    )
                    ZarrinTextField(
                        value = state.customer.address,
                        onValueChange = { value -> viewModel.edit { it.copy(address = value) } },
                        label = "نشانی",
                        singleLine = false,
                        minLines = 2
                    )
                    ZarrinTextField(
                        value = state.customer.note,
                        onValueChange = { value -> viewModel.edit { it.copy(note = value) } },
                        label = "یادداشت"
                    )
                }
            }
            Button(
                onClick = viewModel::save,
                enabled = state.customer.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره مشتری", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
