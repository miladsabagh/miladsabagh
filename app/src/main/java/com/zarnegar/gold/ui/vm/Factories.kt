package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.ZarnegarApp

internal fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as ZarnegarApp).container

object ZarnegarViewModels {
    val products = viewModelFactory {
        initializer { ProductsViewModel(container()) }
    }
    val customers = viewModelFactory {
        initializer { CustomersViewModel(container()) }
    }
    val sale = viewModelFactory {
        initializer { SaleViewModel(container()) }
    }
    val invoices = viewModelFactory {
        initializer { InvoicesViewModel(container()) }
    }
    val settings = viewModelFactory {
        initializer { SettingsViewModel(container()) }
    }
    val dashboard = viewModelFactory {
        initializer { DashboardViewModel(container()) }
    }
}
