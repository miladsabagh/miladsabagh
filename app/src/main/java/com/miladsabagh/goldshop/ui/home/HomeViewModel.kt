package com.miladsabagh.goldshop.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import com.miladsabagh.goldshop.data.settings.SettingsRepository
import com.miladsabagh.goldshop.data.settings.StoreSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val storeSettings: StoreSettings = StoreSettings(),
    val todayInvoiceCount: Int = 0,
    val todayTotalAmount: Double = 0.0,
    val todayTotalWeight: Double = 0.0,
    val productCount: Int = 0,
    val customerCount: Int = 0
)

class HomeViewModel(
    private val repository: GoldShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val todayStatsFlow = combine(
        repository.observeTodayInvoiceCount(startOfTodayMillis()),
        repository.observeTodayTotal(startOfTodayMillis()),
        repository.observeTodayWeight(startOfTodayMillis())
    ) { count, total, weight -> Triple(count, total, weight) }

    private val countsFlow = combine(
        repository.observeProducts(),
        repository.observeCustomers()
    ) { products, customers -> products.size to customers.size }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        todayStatsFlow,
        countsFlow
    ) { settings, todayStats, counts ->
        HomeUiState(
            storeSettings = settings,
            todayInvoiceCount = todayStats.first,
            todayTotalAmount = todayStats.second,
            todayTotalWeight = todayStats.third,
            productCount = counts.first,
            customerCount = counts.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateGoldPrice(price: Double) {
        viewModelScope.launch {
            settingsRepository.updateGoldPrice(price)
            repository.recordGoldPrice(price)
        }
    }
}
