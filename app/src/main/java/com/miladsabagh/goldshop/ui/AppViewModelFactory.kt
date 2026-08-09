package com.miladsabagh.goldshop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.miladsabagh.goldshop.GoldShopApp
import com.miladsabagh.goldshop.ui.customers.CustomerViewModel
import com.miladsabagh.goldshop.ui.home.HomeViewModel
import com.miladsabagh.goldshop.ui.invoice.InvoiceDetailViewModel
import com.miladsabagh.goldshop.ui.invoice.InvoiceListViewModel
import com.miladsabagh.goldshop.ui.invoice.NewInvoiceViewModel
import com.miladsabagh.goldshop.ui.products.ProductViewModel
import com.miladsabagh.goldshop.ui.reports.ReportsViewModel
import com.miladsabagh.goldshop.ui.settings.SettingsViewModel

/**
 * Lightweight manual DI: builds ViewModels wired to the singleton repository/settings
 * held by [GoldShopApp], avoiding the need for a DI framework.
 */
class AppViewModelFactory(private val app: GoldShopApp) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            HomeViewModel::class.java ->
                HomeViewModel(app.repository, app.settingsRepository) as T
            ProductViewModel::class.java ->
                ProductViewModel(app.repository) as T
            CustomerViewModel::class.java ->
                CustomerViewModel(app.repository) as T
            NewInvoiceViewModel::class.java ->
                NewInvoiceViewModel(app.repository, app.settingsRepository) as T
            InvoiceListViewModel::class.java ->
                InvoiceListViewModel(app.repository) as T
            InvoiceDetailViewModel::class.java ->
                InvoiceDetailViewModel(app.repository, app.settingsRepository) as T
            SettingsViewModel::class.java ->
                SettingsViewModel(app.repository, app.settingsRepository) as T
            ReportsViewModel::class.java ->
                ReportsViewModel(app.repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
