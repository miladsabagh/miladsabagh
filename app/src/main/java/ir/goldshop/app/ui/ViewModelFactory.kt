package ir.goldshop.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.goldshop.app.data.repository.GoldShopRepository
import ir.goldshop.app.ui.customers.CustomerViewModel
import ir.goldshop.app.ui.dashboard.DashboardViewModel
import ir.goldshop.app.ui.invoice.InvoiceViewModel
import ir.goldshop.app.ui.products.ProductViewModel
import ir.goldshop.app.ui.settings.SettingsViewModel

class GoldShopViewModelFactory(private val repository: GoldShopRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(ProductViewModel::class.java) ->
                ProductViewModel(repository) as T
            modelClass.isAssignableFrom(CustomerViewModel::class.java) ->
                CustomerViewModel(repository) as T
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) ->
                InvoiceViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
