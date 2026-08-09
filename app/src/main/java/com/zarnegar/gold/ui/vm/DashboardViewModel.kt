package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ShopSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val settings: ShopSettings = ShopSettings(),
    val invoices: List<Invoice> = emptyList(),
    val products: List<Product> = emptyList(),
    val customerCount: Int = 0,
) {
    private val today: JalaliDate get() = JalaliDate.now()

    val todayInvoices: List<Invoice>
        get() = invoices.filter { JalaliDate.fromEpochMillis(it.createdAt) == today }

    val todaySales: Long get() = todayInvoices.sumOf { it.payable }
    val todayWeight: Double get() = todayInvoices.sumOf { it.totalWeightGrams }
    val totalReceivable: Long get() = invoices.sumOf { it.remaining }
    val lowStock: List<Product> get() = products.filter { it.stock in 0..1 }
    val recentInvoices: List<Invoice> get() = invoices.take(5)
}

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        container.settingsRepository.settings,
        container.invoiceRepository.invoices,
        container.productRepository.products,
        container.customerRepository.customers,
    ) { settings, invoices, products, customers ->
        DashboardUiState(settings, invoices, products, customers.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun updateGoldRate(rate: Long) {
        viewModelScope.launch { container.settingsRepository.updateGoldRate(rate) }
    }
}
