package ir.goldshop.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.goldshop.app.data.entity.ShopSettings
import ir.goldshop.app.data.repository.GoldShopRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val shopName: String = "فروشگاه طلا و جواهر",
    val goldPricePerGram: Double = 0.0,
    val todayInvoiceCount: Int = 0,
    val todayTotalSales: Double = 0.0,
    val totalCustomers: Int = 0,
    val totalProducts: Int = 0,
    val totalInvoices: Int = 0
)

class DashboardViewModel(private val repository: GoldShopRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeSettings(),
        repository.observeTodayInvoiceCount(),
        repository.observeTodayTotalSales(),
        repository.observeCustomerCount(),
        repository.observeProductCount(),
        repository.observeInvoiceCount()
    ) { values ->
        val settings = values[0] as ShopSettings
        val todayCount = values[1] as Int
        val todaySales = values[2] as Double
        val customers = values[3] as Int
        val products = values[4] as Int
        val invoices = values[5] as Int
        DashboardUiState(
            shopName = settings.shopName,
            goldPricePerGram = settings.goldPricePerGram,
            todayInvoiceCount = todayCount,
            todayTotalSales = todaySales,
            totalCustomers = customers,
            totalProducts = products,
            totalInvoices = invoices
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun updateGoldPrice(newPrice: Double) {
        viewModelScope.launch {
            val current = repository.getSettingsOrDefault()
            repository.saveSettings(current.copy(goldPricePerGram = newPrice))
        }
    }
}
