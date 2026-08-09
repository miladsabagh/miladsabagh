package com.miladsabagh.goldshop.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerListUiState(
    val query: String = "",
    val customers: List<Customer> = emptyList()
)

class CustomerViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CustomerListUiState> = combine(
        queryFlow,
        queryFlow.flatMapLatest { repository.searchCustomers(it) }
    ) { query, customers -> CustomerListUiState(query, customers) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerListUiState())

    fun onQueryChange(query: String) {
        queryFlow.update { query }
    }

    suspend fun getCustomer(id: Long): Customer? = repository.getCustomer(id)

    fun saveCustomer(customer: Customer, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveCustomer(customer)
            onSaved(id)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
}
