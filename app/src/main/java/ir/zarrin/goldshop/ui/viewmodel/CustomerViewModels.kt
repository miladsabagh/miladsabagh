package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerListState(
    val customers: List<Customer> = emptyList(),
    val query: String = ""
)

class CustomerListViewModel(private val repository: ShopRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<CustomerListState> =
        combine(repository.observeCustomers(), query) { customers, text ->
            val normalized = text.trim()
            CustomerListState(
                customers = if (normalized.isEmpty()) customers else customers.filter {
                    it.name.contains(normalized, ignoreCase = true) || it.phone.contains(normalized)
                },
                query = text
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerListState())

    fun onQueryChange(value: String) = query.update { value }

    fun delete(customer: Customer) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }
}

data class CustomerEditorState(
    val customer: Customer = Customer(),
    val isNew: Boolean = true,
    val saved: Boolean = false
)

class CustomerEditorViewModel(
    private val repository: ShopRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<String>("customerId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(CustomerEditorState(isNew = customerId == 0L))
    val state: StateFlow<CustomerEditorState> = _state.asStateFlow()

    init {
        if (customerId != 0L) {
            viewModelScope.launch {
                repository.findCustomer(customerId)?.let { customer ->
                    _state.update { it.copy(customer = customer, isNew = false) }
                }
            }
        }
    }

    fun edit(transform: (Customer) -> Customer) {
        _state.update { it.copy(customer = transform(it.customer)) }
    }

    fun save() {
        val customer = _state.value.customer
        if (customer.name.isBlank()) return
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _state.update { it.copy(saved = true) }
        }
    }
}
