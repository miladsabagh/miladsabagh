package ir.zarin.faktor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings?> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(settings: AppSettings) {
        viewModelScope.launch {
            repository.update(settings)
            _saved.value = true
        }
    }

    fun consumeSaved() {
        _saved.value = false
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SettingsViewModel(this.appContainer().settingsRepository) }
        }
    }
}
