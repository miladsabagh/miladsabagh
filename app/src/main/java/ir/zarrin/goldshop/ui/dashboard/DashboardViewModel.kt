package ir.zarrin.goldshop.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.db.InvoiceEntity
import ir.zarrin.goldshop.data.repo.CustomerRepository
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.repo.ProductRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.util.JalaliDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val settings: AppSettings = AppSettings(),
    val todayTotal: Long = 0L,
    val todayCount: Int = 0,
    val monthTotal: Long = 0L,
    val monthCount: Int = 0,
    val receivables: Long = 0L,
    val productCount: Int = 0,
    val stockCount: Int = 0,
    val stockWeight: Double = 0.0,
    val customerCount: Int = 0,
    val recentInvoices: List<InvoiceEntity> = emptyList()
)

class DashboardViewModel(
    private val invoiceRepository: InvoiceRepository,
    productRepository: ProductRepository,
    customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val invoiceStats = invoiceRepository.observeAll().map { invoices ->
        val startOfToday = startOfToday()
        val startOfMonth = startOfJalaliMonth()
        val today = invoices.filter { it.invoice.dateMillis >= startOfToday }
        val month = invoices.filter { it.invoice.dateMillis >= startOfMonth }
        Stats(
            todayTotal = today.sumOf { it.invoice.grandTotal },
            todayCount = today.size,
            monthTotal = month.sumOf { it.invoice.grandTotal },
            monthCount = month.size,
            receivables = invoices.sumOf {
                (it.invoice.grandTotal - it.invoice.paidAmount).coerceAtLeast(0L)
            },
            recent = invoices.take(5).map { it.invoice }
        )
    }

    private val inventoryStats = combine(
        productRepository.observeCount(),
        productRepository.observeTotalStock(),
        productRepository.observeTotalWeight(),
        customerRepository.observeCount()
    ) { count, stock, weight, customers ->
        Inventory(count, stock, weight, customers)
    }

    val state: StateFlow<DashboardState> = combine(
        settingsRepository.settings,
        invoiceStats,
        inventoryStats
    ) { settings, stats, inventory ->
        DashboardState(
            settings = settings,
            todayTotal = stats.todayTotal,
            todayCount = stats.todayCount,
            monthTotal = stats.monthTotal,
            monthCount = stats.monthCount,
            receivables = stats.receivables,
            productCount = inventory.productCount,
            stockCount = inventory.stockCount,
            stockWeight = inventory.stockWeight,
            customerCount = inventory.customerCount,
            recentInvoices = stats.recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun updateGoldRate(rate: Long) {
        viewModelScope.launch { settingsRepository.updateGoldRate(rate) }
    }

    private data class Stats(
        val todayTotal: Long,
        val todayCount: Int,
        val monthTotal: Long,
        val monthCount: Int,
        val receivables: Long,
        val recent: List<InvoiceEntity>
    )

    private data class Inventory(
        val productCount: Int,
        val stockCount: Int,
        val stockWeight: Double,
        val customerCount: Int
    )

    companion object {
        fun startOfToday(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun startOfJalaliMonth(): Long {
            val today = JalaliDate.now()
            return JalaliDate(today.year, today.month, 1).toEpochMillis()
        }
    }
}
