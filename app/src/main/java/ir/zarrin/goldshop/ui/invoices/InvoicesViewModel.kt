package ir.zarrin.goldshop.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.db.InvoiceWithItems
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.InvoiceStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InvoicesState(
    val query: String = "",
    val status: InvoiceStatus? = null,
    val invoices: List<InvoiceWithItems> = emptyList(),
    val settings: AppSettings = AppSettings()
) {
    val totalAmount: Long get() = invoices.sumOf { it.invoice.grandTotal }
    val unpaidAmount: Long
        get() = invoices.sumOf { (it.invoice.grandTotal - it.invoice.paidAmount).coerceAtLeast(0L) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class InvoicesViewModel(
    private val repository: InvoiceRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val status = MutableStateFlow<InvoiceStatus?>(null)

    val state: StateFlow<InvoicesState> =
        combine(query, status, settingsRepository.settings) { q, s, settings -> Triple(q, s, settings) }
            .flatMapLatest { (q, s, settings) ->
                repository.search(q, s?.name.orEmpty()).map { list ->
                    InvoicesState(query = q, status = s, invoices = list, settings = settings)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoicesState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusChange(value: InvoiceStatus?) {
        status.value = value
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}

data class InvoiceDetailState(
    val invoice: InvoiceWithItems? = null,
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true
)

class InvoiceDetailViewModel(
    private val repository: InvoiceRepository,
    settingsRepository: SettingsRepository,
    invoiceId: Long
) : ViewModel() {

    val state: StateFlow<InvoiceDetailState> = combine(
        repository.observeById(invoiceId),
        settingsRepository.settings
    ) { invoice, settings ->
        InvoiceDetailState(invoice = invoice, settings = settings, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceDetailState())

    fun registerPayment(id: Long, paid: Long, total: Long) {
        viewModelScope.launch { repository.updatePayment(id, paid, total) }
    }

    fun delete(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            onDeleted()
        }
    }
}
