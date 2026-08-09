package com.miladsabagh.goldshop.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.Invoice
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

data class InvoiceListUiState(
    val query: String = "",
    val invoices: List<Invoice> = emptyList()
)

class InvoiceListViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InvoiceListUiState> = combine(
        queryFlow,
        queryFlow.flatMapLatest { repository.searchInvoices(it) }
    ) { query, invoices -> InvoiceListUiState(query, invoices) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvoiceListUiState())

    fun onQueryChange(query: String) {
        queryFlow.update { query }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}
