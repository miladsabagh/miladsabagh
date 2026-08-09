package com.miladsabagh.goldinvoice.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.repository.CustomerRepository
import com.miladsabagh.goldinvoice.data.repository.InvoiceRepository
import com.miladsabagh.goldinvoice.data.repository.ProductRepository
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository
import com.miladsabagh.goldinvoice.data.repository.ShopSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val goldPricePerGram18k: Double = 0.0,
    val todaySalesTotal: Double = 0.0,
    val todayInvoiceCount: Int = 0,
    val customerCount: Int = 0,
    val productCount: Int = 0,
    val recentInvoices: List<Invoice> = emptyList(),
    val shopName: String = ""
)

class DashboardViewModel(
    private val settingsRepository: SettingsRepository,
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private data class Counters(
        val settings: ShopSettings,
        val todaySalesTotal: Double,
        val todayInvoiceCount: Int,
        val customerCount: Int,
        val productCount: Int
    )

    private val countersFlow = combine(
        settingsRepository.settingsFlow,
        invoiceRepository.observeTodaySalesTotal(),
        invoiceRepository.observeTodayCount(),
        customerRepository.observeCount(),
        productRepository.observeCount()
    ) { settings, salesTotal, invoiceCount, customerCount, productCount ->
        Counters(settings, salesTotal, invoiceCount, customerCount, productCount)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        countersFlow,
        invoiceRepository.observeAll()
    ) { counters, invoices ->
        DashboardUiState(
            goldPricePerGram18k = counters.settings.goldPricePerGram18k,
            todaySalesTotal = counters.todaySalesTotal,
            todayInvoiceCount = counters.todayInvoiceCount,
            customerCount = counters.customerCount,
            productCount = counters.productCount,
            recentInvoices = invoices.take(5),
            shopName = counters.settings.shopName
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun updateGoldPrice(price: Double) {
        viewModelScope.launch {
            settingsRepository.updateGoldPrice(price)
        }
    }
}
