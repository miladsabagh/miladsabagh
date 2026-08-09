package com.goldjewelry.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldjewelry.app.GoldJewelryApp
import com.goldjewelry.app.data.model.Invoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val productCount: Int = 0,
    val customerCount: Int = 0,
    val invoiceCount: Int = 0,
    val totalSales: Long = 0,
    val goldPrice: Long = 0,
    val recentInvoices: List<Invoice> = emptyList(),
    val shopName: String = ""
)

class DashboardViewModel(app: GoldJewelryApp) : ViewModel() {

    private val productRepo = app.productRepository
    private val customerRepo = app.customerRepository
    private val invoiceRepo = app.invoiceRepository
    private val settingsRepo = app.settingsRepository

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            productRepo.countActive(),
            customerRepo.count(),
            invoiceRepo.count()
        ) { productCount, customerCount, invoiceCount ->
            Triple(productCount, customerCount, invoiceCount)
        },
        combine(
            invoiceRepo.totalSales(),
            settingsRepo.getSettings(),
            invoiceRepo.getRecent(5)
        ) { totalSales, settings, recent ->
            Triple(totalSales, settings, recent)
        }
    ) { counts, salesData ->
        val (productCount, customerCount, invoiceCount) = counts
        val (totalSales, settings, recent) = salesData
        DashboardUiState(
            productCount = productCount,
            customerCount = customerCount,
            invoiceCount = invoiceCount,
            totalSales = totalSales,
            goldPrice = settings?.goldPricePerGram ?: 0,
            recentInvoices = recent,
            shopName = settings?.shopName ?: ""
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(app) as T
        }
    }
}
