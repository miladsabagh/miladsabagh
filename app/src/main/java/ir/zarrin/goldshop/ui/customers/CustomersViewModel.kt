package ir.zarrin.goldshop.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.db.CustomerEntity
import ir.zarrin.goldshop.data.db.InvoiceEntity
import ir.zarrin.goldshop.data.repo.CustomerRepository
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomersState(
    val query: String = "",
    val customers: List<CustomerEntity> = emptyList(),
    val purchaseTotals: Map<Long, Long> = emptyMap(),
    val settings: AppSettings = AppSettings()
)

@OptIn(ExperimentalCoroutinesApi::class)
class CustomersViewModel(
    private val repository: CustomerRepository,
    invoiceRepository: InvoiceRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val totals = invoiceRepository.observeAll().map { invoices ->
        invoices.mapNotNull { it.invoice.customerId?.to(it.invoice) }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.sumOf { invoice: InvoiceEntity -> invoice.grandTotal } }
    }

    val state: StateFlow<CustomersState> = query
        .flatMapLatest { q -> repository.search(q).map { q to it } }
        .combine(totals) { (q, list), totalsMap -> Triple(q, list, totalsMap) }
        .combine(settingsRepository.settings) { (q, list, totalsMap), settings ->
            CustomersState(
                query = q,
                customers = list,
                purchaseTotals = totalsMap,
                settings = settings
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomersState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun save(customer: CustomerEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.save(customer)
            onSaved(id)
        }
    }

    fun delete(customer: CustomerEntity) {
        viewModelScope.launch { repository.delete(customer) }
    }
}
