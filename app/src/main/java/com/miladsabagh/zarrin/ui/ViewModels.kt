package com.miladsabagh.zarrin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.zarrin.data.NewInvoiceItem
import com.miladsabagh.zarrin.data.Repository
import com.miladsabagh.zarrin.data.SettingsStore
import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.data.db.CustomerEntity
import com.miladsabagh.zarrin.data.db.InvoiceWithItems
import com.miladsabagh.zarrin.data.db.ProductEntity
import com.miladsabagh.zarrin.util.LineBreakdown
import com.miladsabagh.zarrin.util.priceLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: Repository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    val settings: StateFlow<ShopSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())

    fun updateGoldPrice(value: Long) {
        viewModelScope.launch { settingsStore.setGoldPrice(value) }
    }

    private val _todaySales = MutableStateFlow(0L to 0)
    val todaySales: StateFlow<Pair<Long, Int>> = _todaySales

    private val _totalSales = MutableStateFlow(0L to 0)
    val totalSales: StateFlow<Pair<Long, Int>> = _totalSales

    val productCount: StateFlow<Int> = repository.products
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        refresh()
        viewModelScope.launch {
            repository.invoices.collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _todaySales.value = repository.todaySales()
            _totalSales.value = repository.totalSales()
        }
    }
}

class ProductsViewModel(private val repository: Repository) : ViewModel() {
    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(product: ProductEntity) {
        viewModelScope.launch { repository.saveProduct(product) }
    }

    fun delete(product: ProductEntity) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }
}

class CustomersViewModel(private val repository: Repository) : ViewModel() {
    val customers: StateFlow<List<CustomerEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(customer: CustomerEntity) {
        viewModelScope.launch { repository.saveCustomer(customer) }
    }

    fun delete(customer: CustomerEntity) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }
}

class InvoicesViewModel(repository: Repository) : ViewModel() {
    val invoices: StateFlow<List<InvoiceWithItems>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class InvoiceDetailViewModel(
    private val repository: Repository,
    settingsStore: SettingsStore,
    invoiceId: Long
) : ViewModel() {
    val invoice: StateFlow<InvoiceWithItems?> = repository.invoice(invoiceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val settings: StateFlow<ShopSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())
}

data class CartLine(
    val productId: Long?,
    val title: String,
    val karat: Int,
    val weightGrams: Double,
    val wagePerGram: Long
) {
    fun breakdown(settings: ShopSettings): LineBreakdown = priceLine(
        basePrice18kPerGram = settings.goldPricePerGram18k,
        karat = karat,
        weightGrams = weightGrams,
        wagePerGram = wagePerGram,
        profitPercent = settings.profitPercent,
        taxPercent = settings.taxPercent
    )
}

class NewInvoiceViewModel(
    private val repository: Repository,
    settingsStore: SettingsStore
) : ViewModel() {

    val settings: StateFlow<ShopSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())

    val customers: StateFlow<List<CustomerEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines

    private val _savedInvoiceId = MutableStateFlow<Long?>(null)
    val savedInvoiceId: StateFlow<Long?> = _savedInvoiceId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun addProduct(product: ProductEntity) {
        _lines.value = _lines.value + CartLine(
            productId = product.id,
            title = product.name,
            karat = product.karat,
            weightGrams = product.weightGrams,
            wagePerGram = product.wagePerGram
        )
    }

    fun addManual(title: String, karat: Int, weightGrams: Double, wagePerGram: Long) {
        _lines.value = _lines.value + CartLine(
            productId = null,
            title = title,
            karat = karat,
            weightGrams = weightGrams,
            wagePerGram = wagePerGram
        )
    }

    fun removeLine(index: Int) {
        _lines.value = _lines.value.toMutableList().also { it.removeAt(index) }
    }

    fun saveInvoice() {
        val currentLines = _lines.value
        if (currentLines.isEmpty()) {
            _error.value = "فاکتور بدون قلم قابل صدور نیست"
            return
        }
        viewModelScope.launch {
            val currentSettings = settings.first()
            val customer = _selectedCustomer.value
            val id = repository.createInvoice(
                customer = customer,
                items = currentLines.map {
                    NewInvoiceItem(
                        productId = it.productId,
                        title = it.title,
                        karat = it.karat,
                        weightGrams = it.weightGrams,
                        wagePerGram = it.wagePerGram
                    )
                },
                settings = currentSettings
            )
            _savedInvoiceId.value = id
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class SettingsViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val settings: StateFlow<ShopSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())

    fun updateGoldPrice(value: Long) {
        viewModelScope.launch { settingsStore.setGoldPrice(value) }
    }

    fun updateProfitPercent(value: Double) {
        viewModelScope.launch { settingsStore.setProfitPercent(value) }
    }

    fun updateTaxPercent(value: Double) {
        viewModelScope.launch { settingsStore.setTaxPercent(value) }
    }

    fun updateStoreInfo(name: String, phone: String, address: String) {
        viewModelScope.launch { settingsStore.setStoreInfo(name, phone, address) }
    }
}
