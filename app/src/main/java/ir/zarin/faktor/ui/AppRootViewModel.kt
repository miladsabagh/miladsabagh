package ir.zarin.faktor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppRootViewModel(settingsRepository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    companion object {
        val Factory = viewModelFactory {
            initializer { AppRootViewModel(this.appContainer().settingsRepository) }
        }
    }
}
