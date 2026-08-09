package com.miladsabagh.goldshop.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import com.miladsabagh.goldshop.data.settings.SettingsRepository
import com.miladsabagh.goldshop.data.settings.StoreSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class InvoiceDetailUiState(
    val invoice: Invoice? = null,
    val items: List<InvoiceItem> = emptyList(),
    val storeSettings: StoreSettings = StoreSettings(),
    val isLoading: Boolean = true,
    val deleted: Boolean = false
)

class InvoiceDetailViewModel(
    private val repository: GoldShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    fun load(invoiceId: Long) {
        viewModelScope.launch {
            val invoice = repository.getInvoice(invoiceId)
            val items = repository.getInvoiceItems(invoiceId)
            val settings = settingsRepository.settingsFlow.first()
            _uiState.value = InvoiceDetailUiState(
                invoice = invoice,
                items = items,
                storeSettings = settings,
                isLoading = false
            )
        }
    }

    fun deleteInvoice() {
        val invoice = _uiState.value.invoice ?: return
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
