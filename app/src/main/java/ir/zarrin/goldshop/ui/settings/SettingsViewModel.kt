package ir.zarrin.goldshop.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.Currency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun saveShopInfo(name: String, owner: String, phone: String, address: String) {
        viewModelScope.launch { repository.updateShopInfo(name, owner, phone, address) }
    }

    fun saveGoldRate(rate: Long) {
        viewModelScope.launch { repository.updateGoldRate(rate) }
    }

    fun saveDefaults(tax: Double, wage: Double, profit: Double) {
        viewModelScope.launch { repository.updateDefaults(tax, wage, profit) }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { repository.updateCurrency(currency) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.updateDarkTheme(enabled) }
    }
}
