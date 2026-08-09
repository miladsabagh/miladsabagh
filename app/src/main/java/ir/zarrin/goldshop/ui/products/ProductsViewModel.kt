package ir.zarrin.goldshop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.data.repo.ProductRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.ProductCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductsState(
    val query: String = "",
    val category: ProductCategory? = null,
    val products: List<ProductEntity> = emptyList(),
    val settings: AppSettings = AppSettings()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(
    private val repository: ProductRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ProductCategory?>(null)

    val state: StateFlow<ProductsState> =
        combine(query, category, settingsRepository.settings) { q, c, s -> Triple(q, c, s) }
            .flatMapLatest { (q, c, s) ->
                repository.search(q, c?.name.orEmpty()).map { list ->
                    ProductsState(query = q, category = c, products = list, settings = s)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: ProductCategory?) {
        category.value = value
    }

    fun delete(product: ProductEntity) {
        viewModelScope.launch { repository.delete(product) }
    }
}

data class ProductFormState(
    val id: Long = 0L,
    val code: String = "",
    val name: String = "",
    val category: ProductCategory = ProductCategory.RING,
    val karat: Int = 18,
    val weight: String = "",
    val wagePercent: String = "",
    val profitPercent: String = "",
    val stonePrice: String = "",
    val pricingMode: PricingMode = PricingMode.BY_WEIGHT,
    val fixedPrice: String = "",
    val taxable: Boolean = true,
    val stockQty: String = "1",
    val note: String = "",
    val loaded: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val error: String? = null,
    val saved: Boolean = false
)

class ProductEditViewModel(
    private val repository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val productId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ProductFormState())
    val state: StateFlow<ProductFormState> = _state

    init {
        viewModelScope.launch {
            val settings = settingsRepository.current()
            val existing = if (productId > 0) repository.findById(productId) else null
            _state.value = if (existing != null) {
                ProductFormState(
                    id = existing.id,
                    code = existing.code,
                    name = existing.name,
                    category = ProductCategory.fromName(existing.category),
                    karat = existing.karat,
                    weight = existing.weightGrams.takeIf { it > 0 }?.toString().orEmpty(),
                    wagePercent = existing.wagePercent.toString(),
                    profitPercent = existing.profitPercent.toString(),
                    stonePrice = existing.stonePrice.takeIf { it > 0 }?.toString().orEmpty(),
                    pricingMode = PricingMode.fromName(existing.pricingMode),
                    fixedPrice = existing.fixedPrice.takeIf { it > 0 }?.toString().orEmpty(),
                    taxable = existing.taxable,
                    stockQty = existing.stockQty.toString(),
                    note = existing.note,
                    loaded = true,
                    settings = settings
                )
            } else {
                ProductFormState(
                    code = suggestCode(),
                    wagePercent = settings.defaultWagePercent.toString(),
                    profitPercent = settings.defaultProfitPercent.toString(),
                    loaded = true,
                    settings = settings
                )
            }
        }
    }

    fun update(transform: (ProductFormState) -> ProductFormState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun save(entity: ProductEntity) {
        viewModelScope.launch {
            runCatching { repository.save(entity) }
                .onSuccess { _state.value = _state.value.copy(saved = true) }
                .onFailure {
                    _state.value = _state.value.copy(error = "کد کالا تکراری است یا ذخیره ناموفق بود")
                }
        }
    }

    private fun suggestCode(): String =
        "K-" + (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
}
