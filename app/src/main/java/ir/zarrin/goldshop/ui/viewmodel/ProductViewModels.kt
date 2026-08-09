package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.Product
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.data.settings.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductListState(
    val products: List<Product> = emptyList(),
    val settings: ShopSettings = ShopSettings(),
    val query: String = ""
)

class ProductListViewModel(
    private val repository: ShopRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<ProductListState> = combine(
        repository.observeProducts(),
        settingsRepository.settings,
        query
    ) { products, settings, text ->
        val normalized = text.trim()
        ProductListState(
            products = if (normalized.isEmpty()) products else products.filter {
                it.name.contains(normalized, ignoreCase = true) ||
                    it.code.contains(normalized, ignoreCase = true)
            },
            settings = settings,
            query = text
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductListState())

    fun onQueryChange(value: String) = query.update { value }

    fun delete(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }
}

data class ProductEditorState(
    val product: Product = Product(),
    val settings: ShopSettings = ShopSettings(),
    val isNew: Boolean = true,
    val loading: Boolean = true,
    val saved: Boolean = false
)

class ProductEditorViewModel(
    private val repository: ShopRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(ProductEditorState(isNew = productId == 0L))
    val state: StateFlow<ProductEditorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val existing = if (productId != 0L) repository.findProduct(productId) else null
            _state.update { current ->
                current.copy(
                    settings = settings,
                    isNew = existing == null,
                    loading = false,
                    product = existing ?: current.product.copy(
                        wageValue = settings.defaultWagePercent,
                        profitPercent = settings.defaultProfitPercent
                    )
                )
            }
        }
    }

    fun save(product: Product) {
        if (product.name.isBlank()) return
        viewModelScope.launch {
            repository.saveProduct(product)
            _state.update { it.copy(product = product, saved = true) }
        }
    }
}
