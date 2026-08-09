package com.miladsabagh.goldshop.ui.customers

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.ui.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    customerId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: CustomerViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var existing by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(customerId) {
        if (customerId != null && customerId > 0) {
            val customer = viewModel.getCustomer(customerId)
            if (customer != null) {
                existing = customer
                firstName = customer.firstName
                lastName = customer.lastName
                phone = customer.phone
                address = customer.address
                nationalCode = customer.nationalCode
                notes = customer.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "مشتری جدید" else "ویرایش مشتری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("نام") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("نام خانوادگی") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("شماره تماس") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nationalCode,
                onValueChange = { nationalCode = it },
                label = { Text("کد ملی (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val customer = (existing ?: Customer(firstName = firstName)).copy(
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        address = address,
                        nationalCode = nationalCode,
                        notes = notes
                    )
                    viewModel.saveCustomer(customer) { onDone() }
                },
                enabled = firstName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره مشتری")
            }
        }
    }
}
