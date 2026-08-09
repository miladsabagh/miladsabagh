package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.data.settings.ShopSettings
import ir.zarrin.goldshop.domain.model.InvoiceType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardState(
    val settings: ShopSettings = ShopSettings(),
    val todaySales: Long = 0L,
    val todayCount: Int = 0,
    val monthSales: Long = 0L,
    val monthCount: Int = 0,
    val receivables: Long = 0L,
    val productCount: Int = 0,
    val customerCount: Int = 0,
    val recentInvoices: List<Invoice> = emptyList()
)

class DashboardViewModel(
    private val repository: ShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val state = combine(
        settingsRepository.settings,
        repository.observeInvoices(),
        repository.observeProductCount(),
        repository.observeCustomerCount()
    ) { settings, invoices, productCount, customerCount ->
        val now = System.currentTimeMillis()
        val startOfToday = PersianCalendar.startOfDay(now)
        val startOfMonth = PersianCalendar.startOfJalaliMonth(now)
        val sales = invoices.filter { it.type == InvoiceType.SALE }
        val today = sales.filter { it.dateMillis >= startOfToday }
        val month = sales.filter { it.dateMillis >= startOfMonth }
        DashboardState(
            settings = settings,
            todaySales = today.sumOf { it.payable },
            todayCount = today.size,
            monthSales = month.sumOf { it.payable },
            monthCount = month.size,
            receivables = sales.sumOf { (it.payable - it.paid).coerceAtLeast(0L) },
            productCount = productCount,
            customerCount = customerCount,
            recentInvoices = invoices.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun updateGoldRate(rate: Long) {
        viewModelScope.launch { settingsRepository.updateGoldRate(rate) }
    }
}
