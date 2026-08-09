package com.miladsabagh.goldinvoice.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository
import com.miladsabagh.goldinvoice.data.repository.ShopSettings
import com.miladsabagh.goldinvoice.util.parseLocalizedDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsFormState(
    val shopName: String = "",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val shopLicenseNumber: String = "",
    val defaultLaborFeePercent: String = "",
    val defaultProfitPercent: String = "",
    val defaultTaxPercent: String = "",
    val isLoaded: Boolean = false,
    val savedFlag: Int = 0
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _form = MutableStateFlow(SettingsFormState())
    val form: StateFlow<SettingsFormState> = _form

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings: ShopSettings ->
                if (!_form.value.isLoaded) {
                    _form.value = SettingsFormState(
                        shopName = settings.shopName,
                        shopPhone = settings.shopPhone,
                        shopAddress = settings.shopAddress,
                        shopLicenseNumber = settings.shopLicenseNumber,
                        defaultLaborFeePercent = settings.defaultLaborFeePercent.toString(),
                        defaultProfitPercent = settings.defaultProfitPercent.toString(),
                        defaultTaxPercent = settings.defaultTaxPercent.toString(),
                        isLoaded = true
                    )
                }
            }
        }
    }

    fun update(transform: (SettingsFormState) -> SettingsFormState) {
        _form.value = transform(_form.value)
    }

    fun save() {
        val current = _form.value
        viewModelScope.launch {
            repository.updateShopInfo(
                shopName = current.shopName.trim(),
                shopPhone = current.shopPhone.trim(),
                shopAddress = current.shopAddress.trim(),
                shopLicenseNumber = current.shopLicenseNumber.trim()
            )
            repository.updatePricingDefaults(
                laborFeePercent = parseLocalizedDouble(current.defaultLaborFeePercent) ?: 0.0,
                profitPercent = parseLocalizedDouble(current.defaultProfitPercent) ?: 0.0,
                taxPercent = parseLocalizedDouble(current.defaultTaxPercent) ?: 0.0
            )
            _form.value = current.copy(savedFlag = current.savedFlag + 1)
        }
    }
}
