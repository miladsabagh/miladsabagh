package ir.zarin.faktor.ui.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.data.settings.SettingsRepository
import ir.zarin.faktor.ui.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InvoicesUiState(
    val query: String = "",
    val invoices: List<Invoice> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class InvoicesViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<InvoicesUiState> =
        combine(query, query.flatMapLatest { repository.search(it) }) { text, invoices ->
            InvoicesUiState(query = text, invoices = invoices)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InvoicesUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { InvoicesViewModel(this.appContainer().invoiceRepository) }
        }
    }
}

data class InvoiceDetailUiState(
    val invoice: InvoiceWithItems? = null,
    val settings: AppSettings = AppSettings(),
)

class InvoiceDetailViewModel(
    private val repository: InvoiceRepository,
    settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val invoiceId: Long =
        savedStateHandle.get<String>(Routes.INVOICE_ID_ARG)?.toLongOrNull() ?: 0L

    val uiState: StateFlow<InvoiceDetailUiState> = combine(
        repository.observeWithItems(invoiceId),
        settingsRepository.settings,
    ) { invoice, settings ->
        InvoiceDetailUiState(invoice = invoice, settings = settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InvoiceDetailUiState(),
    )

    fun markAsPaid() {
        val invoice = uiState.value.invoice?.invoice ?: return
        viewModelScope.launch { repository.updatePaidAmount(invoice.id, invoice.grandTotalRial) }
    }

    fun delete(onDeleted: () -> Unit) {
        val invoice = uiState.value.invoice?.invoice ?: return
        viewModelScope.launch {
            repository.delete(invoice)
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this.appContainer()
                InvoiceDetailViewModel(
                    repository = container.invoiceRepository,
                    settingsRepository = container.settingsRepository,
                    savedStateHandle = this.createSavedStateHandle(),
                )
            }
        }
    }
}
