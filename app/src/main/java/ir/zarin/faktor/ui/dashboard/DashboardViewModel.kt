package ir.zarin.faktor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val settings: AppSettings = AppSettings(),
    val todaySalesRial: Long = 0,
    val monthSalesRial: Long = 0,
    val todayInvoiceCount: Int = 0,
    val outstandingRial: Long = 0,
    val recentInvoices: List<Invoice> = emptyList(),
    val todayMillis: Long = System.currentTimeMillis(),
)

class DashboardViewModel(
    invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val startOfToday = JalaliCalendar.startOfToday()
    private val startOfMonth = JalaliCalendar.startOfJalaliMonth()

    private val statsFlow = combine(
        invoiceRepository.observeSalesSince(startOfToday),
        invoiceRepository.observeSalesSince(startOfMonth),
        invoiceRepository.observeCountSince(startOfToday),
        invoiceRepository.observeOutstanding(),
        invoiceRepository.observeRecent(limit = 5),
    ) { todaySales, monthSales, todayCount, outstanding, recent ->
        DashboardUiState(
            todaySalesRial = todaySales,
            monthSalesRial = monthSales,
            todayInvoiceCount = todayCount,
            outstandingRial = outstanding,
            recentInvoices = recent,
        )
    }

    val uiState: StateFlow<DashboardUiState> =
        combine(statsFlow, settingsRepository.settings) { stats, settings ->
            stats.copy(settings = settings)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    fun updateGoldRate(rateRial: Long) {
        viewModelScope.launch { settingsRepository.updateGoldRate(rateRial) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this.appContainer()
                DashboardViewModel(container.invoiceRepository, container.settingsRepository)
            }
        }
    }
}
