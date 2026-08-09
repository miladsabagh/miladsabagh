package com.goldjewelry.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldjewelry.app.GoldJewelryApp
import com.goldjewelry.app.data.model.Customer
import com.goldjewelry.app.data.model.Invoice
import com.goldjewelry.app.data.model.InvoiceWithItems
import com.goldjewelry.app.data.model.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoicesViewModel(app: GoldJewelryApp) : ViewModel() {

    private val invoiceRepo = app.invoiceRepository
    private val settingsRepo = app.settingsRepository

    val invoices: StateFlow<List<Invoice>> = invoiceRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoice: StateFlow<InvoiceWithItems?> = _selectedInvoice.asStateFlow()

    private val _settings = MutableStateFlow<ShopSettings?>(null)
    val settings: StateFlow<ShopSettings?> = _settings.asStateFlow()

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            _selectedInvoice.value = invoiceRepo.getInvoiceWithItems(id)
            _settings.value = settingsRepo.getSettingsOnce()
        }
    }

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InvoicesViewModel(app) as T
        }
    }
}

class CustomersViewModel(app: GoldJewelryApp) : ViewModel() {

    private val customerRepo = app.customerRepository

    val customers = customerRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(customer: Customer, onDone: () -> Unit) {
        viewModelScope.launch {
            customerRepo.insert(customer)
            onDone()
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            customerRepo.delete(id)
        }
    }

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomersViewModel(app) as T
        }
    }
}

class SettingsViewModel(app: GoldJewelryApp) : ViewModel() {

    private val settingsRepo = app.settingsRepository

    val settings = settingsRepo.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSettings(settings: ShopSettings) {
        viewModelScope.launch {
            settingsRepo.update(settings)
        }
    }

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(app) as T
        }
    }
}
