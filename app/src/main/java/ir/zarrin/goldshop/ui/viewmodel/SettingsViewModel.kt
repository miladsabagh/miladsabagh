package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.data.settings.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val settings: ShopSettings = ShopSettings(),
    val loading: Boolean = true,
    val message: String? = null,
    val seeding: Boolean = false
)

class SettingsViewModel(
    private val repository: ShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _state.update { it.copy(settings = settings, loading = false) }
        }
    }

    fun save(settings: ShopSettings) {
        viewModelScope.launch {
            settingsRepository.update(settings)
            _state.update { it.copy(settings = settings, message = "تنظیمات ذخیره شد.") }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            _state.update { it.copy(seeding = true) }
            val settings = _state.value.settings
            repository.seedSampleData(settings.goldRate18, settings.taxPercent)
            _state.update { it.copy(seeding = false, message = "داده‌های نمونه اضافه شد.") }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
