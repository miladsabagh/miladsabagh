package ir.goldshop.app.ui.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.goldshop.app.data.entity.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    viewModel: CustomerViewModel,
    customerId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var loadedId by remember { mutableStateOf(0L) }

    LaunchedEffect(customerId) {
        if (customerId != null) {
            viewModel.getCustomer(customerId)?.let { customer ->
                fullName = customer.fullName
                phone = customer.phoneNumber
                address = customer.address
                nationalId = customer.nationalId
                notes = customer.notes
                loadedId = customer.id
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customerId == null) "افزودن مشتری" else "ویرایش مشتری") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("نام و نام خانوادگی") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("شماره تماس") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nationalId,
                onValueChange = { nationalId = it },
                label = { Text("کد ملی (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val customer = Customer(
                        id = loadedId,
                        fullName = fullName.ifBlank { "مشتری بدون‌نام" },
                        phoneNumber = phone,
                        address = address,
                        nationalId = nationalId,
                        notes = notes
                    )
                    viewModel.saveCustomer(customer, onSaved)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره مشتری")
            }
        }
    }
}
