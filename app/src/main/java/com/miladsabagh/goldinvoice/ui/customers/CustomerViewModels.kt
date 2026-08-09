package com.miladsabagh.goldinvoice.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.entity.Customer
import com.miladsabagh.goldinvoice.data.repository.CustomerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerListViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryState: StateFlow<String> = query

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.observeAll() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { repository.delete(customer) }
    }
}

data class CustomerFormState(
    val id: Long = 0,
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val nationalId: String = "",
    val notes: String = "",
    val nameError: Boolean = false
)

class CustomerEditViewModel(
    private val repository: CustomerRepository,
    private val customerId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerFormState())
    val state: StateFlow<CustomerFormState> = _state

    val saveCompleted = MutableStateFlow(false)

    init {
        if (customerId != null) {
            viewModelScope.launch {
                repository.getById(customerId)?.let { customer ->
                    _state.value = CustomerFormState(
                        id = customer.id,
                        fullName = customer.fullName,
                        phone = customer.phone,
                        address = customer.address,
                        nationalId = customer.nationalId,
                        notes = customer.notes
                    )
                }
            }
        }
    }

    fun update(transform: (CustomerFormState) -> CustomerFormState) {
        _state.value = transform(_state.value)
    }

    fun save() {
        val current = _state.value
        if (current.fullName.isBlank()) {
            _state.value = current.copy(nameError = true)
            return
        }
        viewModelScope.launch {
            repository.save(
                Customer(
                    id = current.id,
                    fullName = current.fullName.trim(),
                    phone = current.phone.trim(),
                    address = current.address.trim(),
                    nationalId = current.nationalId.trim(),
                    notes = current.notes.trim()
                )
            )
            saveCompleted.value = true
        }
    }
}
