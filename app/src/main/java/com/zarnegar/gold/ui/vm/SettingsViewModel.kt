package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.domain.model.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<ShopSettings> = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(settings: ShopSettings) {
        viewModelScope.launch {
            container.settingsRepository.save(settings)
            _saved.value = true
        }
    }

    fun updateGoldRate(rate: Long) {
        viewModelScope.launch { container.settingsRepository.updateGoldRate(rate) }
    }

    fun consumeSaved() {
        _saved.value = false
    }
}
