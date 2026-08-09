package ir.zarin.faktor.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.repository.ProductRepository
import ir.zarin.faktor.data.settings.SettingsRepository
import ir.zarin.faktor.domain.PricingContext
import ir.zarin.faktor.ui.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductsUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    /** برای نمایش قیمت تقریبی هر کالا با نرخ روز. */
    val pricingContext: PricingContext = PricingContext(0, 0.0, 0.0),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(
    private val repository: ProductRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<ProductsUiState> = combine(
        query,
        query.flatMapLatest { repository.search(it) },
        settingsRepository.settings,
    ) { text, products, settings ->
        ProductsUiState(
            query = text,
            products = products,
            pricingContext = settings.pricingContext,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductsUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun delete(product: Product) {
        viewModelScope.launch { repository.delete(product) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this.appContainer()
                ProductsViewModel(container.productRepository, container.settingsRepository)
            }
        }
    }
}

class ProductEditViewModel(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>(Routes.PRODUCT_ID_ARG)?.toLongOrNull() ?: 0L

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    val isNew: Boolean get() = productId == 0L

    init {
        viewModelScope.launch {
            _product.value = if (productId == 0L) Product() else repository.getById(productId) ?: Product()
        }
    }

    fun save(product: Product, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.save(product.copy(id = productId))
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _product.value ?: return
        if (productId == 0L) return
        viewModelScope.launch {
            repository.delete(current.copy(id = productId))
            onDeleted()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ProductEditViewModel(this.appContainer().productRepository, this.createSavedStateHandle())
            }
        }
    }
}
