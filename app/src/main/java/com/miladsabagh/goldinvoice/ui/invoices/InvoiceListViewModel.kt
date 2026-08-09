package com.miladsabagh.goldinvoice.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InvoiceListViewModel(repository: InvoiceRepository) : ViewModel() {
    val invoices: StateFlow<List<Invoice>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
