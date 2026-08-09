package ir.zarin.faktor.ui.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.repository.CustomerRepository
import ir.zarin.faktor.ui.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomersUiState(
    val query: String = "",
    val customers: List<Customer> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CustomersViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<CustomersUiState> =
        combine(query, query.flatMapLatest { repository.search(it) }) { text, customers ->
            CustomersUiState(query = text, customers = customers)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CustomersUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { repository.delete(customer) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CustomersViewModel(this.appContainer().customerRepository) }
        }
    }
}

class CustomerEditViewModel(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val customerId: Long =
        savedStateHandle.get<String>(Routes.CUSTOMER_ID_ARG)?.toLongOrNull() ?: 0L

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    val isNew: Boolean get() = customerId == 0L

    init {
        viewModelScope.launch {
            _customer.value =
                if (customerId == 0L) Customer() else repository.getById(customerId) ?: Customer()
        }
    }

    fun save(customer: Customer, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.save(customer.copy(id = customerId))
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _customer.value ?: return
        if (customerId == 0L) return
        viewModelScope.launch {
            repository.delete(current.copy(id = customerId))
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CustomerEditViewModel(this.appContainer().customerRepository, this.createSavedStateHandle())
            }
        }
    }
}
