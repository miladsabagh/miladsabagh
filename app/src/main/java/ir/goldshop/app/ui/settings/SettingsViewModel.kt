package ir.goldshop.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.goldshop.app.data.entity.ShopSettings
import ir.goldshop.app.data.repository.GoldShopRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: GoldShopRepository) : ViewModel() {

    val settings: StateFlow<ShopSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    fun save(settings: ShopSettings, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            onSaved()
        }
    }
}
