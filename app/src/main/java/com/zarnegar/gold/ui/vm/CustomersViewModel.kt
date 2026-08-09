package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.domain.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomersUiState(
    val customers: List<Customer> = emptyList(),
    val query: String = "",
) {
    val filtered: List<Customer>
        get() = customers.filter {
            query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) ||
                it.phone.contains(query) ||
                it.nationalCode.contains(query)
        }
}

class CustomersViewModel(private val container: AppContainer) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<CustomersUiState> = combine(
        container.customerRepository.customers,
        query,
    ) { customers, q -> CustomersUiState(customers, q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomersUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun save(customer: Customer) {
        viewModelScope.launch { container.customerRepository.upsert(customer) }
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { container.customerRepository.delete(customer) }
    }
}
