package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarrin.goldshop.AppContainer
import ir.zarrin.goldshop.ZarrinApplication

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZarrinApplication).container

/** Single factory that knows how to build every view model in the app. */
val ZarrinViewModelFactory = viewModelFactory {
    initializer { DashboardViewModel(container().repository, container().settingsRepository) }
    initializer { ProductListViewModel(container().repository, container().settingsRepository) }
    initializer {
        ProductEditorViewModel(container().repository, container().settingsRepository, createSavedStateHandle())
    }
    initializer { CustomerListViewModel(container().repository) }
    initializer { CustomerEditorViewModel(container().repository, createSavedStateHandle()) }
    initializer { InvoiceListViewModel(container().repository, container().settingsRepository) }
    initializer {
        InvoiceEditorViewModel(container().repository, container().settingsRepository, createSavedStateHandle())
    }
    initializer {
        InvoiceDetailViewModel(container().repository, container().settingsRepository, createSavedStateHandle())
    }
    initializer { ReportsViewModel(container().repository, container().settingsRepository) }
    initializer { SettingsViewModel(container().repository, container().settingsRepository) }
}
