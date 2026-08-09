package ir.goldshop.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.goldshop.app.data.entity.Customer
import ir.goldshop.app.data.repository.GoldShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    val customers: StateFlow<List<Customer>> = searchQuery
        .flatMapLatest { repository.searchCustomers(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    suspend fun getCustomer(id: Long): Customer? = repository.getCustomer(id)

    fun saveCustomer(customer: Customer, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (customer.id == 0L) {
                repository.saveCustomer(customer)
            } else {
                repository.updateCustomer(customer)
                customer.id
            }
            onSaved(id)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
}
