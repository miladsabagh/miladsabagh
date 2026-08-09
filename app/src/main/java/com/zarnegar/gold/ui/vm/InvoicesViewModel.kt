package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InvoicesUiState(
    val invoices: List<Invoice> = emptyList(),
    val query: String = "",
    val status: InvoiceStatus? = null,
    val settings: ShopSettings = ShopSettings(),
) {
    val filtered: List<Invoice>
        get() = invoices.filter { invoice ->
            (status == null || invoice.status == status) &&
                (
                    query.isBlank() ||
                        invoice.number.contains(query) ||
                        invoice.customerName.contains(query, ignoreCase = true) ||
                        invoice.customerPhone.contains(query)
                    )
        }

    val totalSales: Long get() = invoices.sumOf { it.payable }
    val totalReceivable: Long get() = invoices.sumOf { it.remaining }
}

class InvoicesViewModel(private val container: AppContainer) : ViewModel() {

    private val query = MutableStateFlow("")
    private val status = MutableStateFlow<InvoiceStatus?>(null)

    val uiState: StateFlow<InvoicesUiState> = combine(
        container.invoiceRepository.invoices,
        query,
        status,
        container.settingsRepository.settings,
    ) { invoices, q, s, settings ->
        InvoicesUiState(invoices, q, s, settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoicesUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusChange(value: InvoiceStatus?) {
        status.value = value
    }

    fun delete(invoice: Invoice) {
        viewModelScope.launch { container.invoiceRepository.delete(invoice.id) }
    }

    suspend fun load(id: Long): Invoice? = container.invoiceRepository.find(id)

    fun registerPayment(invoice: Invoice, amount: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val paid = (invoice.paidAmount + amount).coerceAtLeast(0)
            container.invoiceRepository.update(
                invoice.copy(
                    paidAmount = paid,
                    status = InvoiceStatus.of(invoice.payable, paid),
                ),
            )
            onDone()
        }
    }
}
