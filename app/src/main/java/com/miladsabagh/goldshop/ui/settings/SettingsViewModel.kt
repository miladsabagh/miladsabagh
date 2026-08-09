package com.miladsabagh.goldshop.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.GoldPriceEntry
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import com.miladsabagh.goldshop.data.settings.SettingsRepository
import com.miladsabagh.goldshop.data.settings.StoreSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: StoreSettings = StoreSettings(),
    val priceHistory: List<GoldPriceEntry> = emptyList()
)

class SettingsViewModel(
    private val repository: GoldShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        repository.observeGoldPriceHistory(10)
    ) { settings, history -> SettingsUiState(settings, history) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateStoreInfo(name: String, phone: String, address: String) {
        viewModelScope.launch {
            settingsRepository.updateStoreInfo(name, phone, address)
        }
    }

    fun updateDefaults(laborFeePercent: Double, profitPercent: Double, taxPercent: Double) {
        viewModelScope.launch {
            settingsRepository.updateDefaults(laborFeePercent, profitPercent, taxPercent)
        }
    }

    fun updateGoldPrice(price: Double) {
        viewModelScope.launch {
            settingsRepository.updateGoldPrice(price)
            repository.recordGoldPrice(price)
        }
    }
}
