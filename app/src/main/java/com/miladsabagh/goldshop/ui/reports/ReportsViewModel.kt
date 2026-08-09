package com.miladsabagh.goldshop.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class PeriodSummary(
    val label: String,
    val invoiceCount: Int,
    val totalAmount: Double,
    val totalWeight: Double
)

data class ReportsUiState(
    val today: PeriodSummary = PeriodSummary("امروز", 0, 0.0, 0.0),
    val thisWeek: PeriodSummary = PeriodSummary("۷ روز اخیر", 0, 0.0, 0.0),
    val thisMonth: PeriodSummary = PeriodSummary("این ماه", 0, 0.0, 0.0),
    val isLoading: Boolean = true
)

class ReportsViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val startOfDay = startOfDayMillis()
            val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
            val startOfMonth = startOfMonthMillis()

            val today = PeriodSummary(
                "امروز",
                repository.countBetween(startOfDay, now),
                repository.sumTotalBetween(startOfDay, now),
                repository.sumWeightBetween(startOfDay, now)
            )
            val week = PeriodSummary(
                "۷ روز اخیر",
                repository.countBetween(sevenDaysAgo, now),
                repository.sumTotalBetween(sevenDaysAgo, now),
                repository.sumWeightBetween(sevenDaysAgo, now)
            )
            val month = PeriodSummary(
                "این ماه",
                repository.countBetween(startOfMonth, now),
                repository.sumTotalBetween(startOfMonth, now),
                repository.sumWeightBetween(startOfMonth, now)
            )
            _uiState.value = ReportsUiState(today, week, month, isLoading = false)
        }
    }

    private fun startOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
