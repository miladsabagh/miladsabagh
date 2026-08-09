package com.miladsabagh.goldinvoice.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.entity.Product
import com.miladsabagh.goldinvoice.data.repository.ProductRepository
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryState: StateFlow<String> = query

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.observeAll() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun delete(product: Product) {
        viewModelScope.launch { repository.delete(product) }
    }
}

data class ProductFormState(
    val id: Long = 0,
    val name: String = "",
    val category: String = com.miladsabagh.goldinvoice.data.entity.JEWELRY_CATEGORIES.first(),
    val code: String = "",
    val weightGrams: String = "",
    val karat: Int = 18,
    val laborFeePercent: String = "",
    val profitPercent: String = "",
    val taxPercent: String = "",
    val quantity: String = "1",
    val description: String = "",
    val isLoaded: Boolean = false,
    val nameError: Boolean = false,
    val weightError: Boolean = false
)

class ProductEditViewModel(
    private val repository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val productId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(ProductFormState())
    val state: StateFlow<ProductFormState> = _state

    var saveCompleted = MutableStateFlow(false)
        private set

    init {
        viewModelScope.launch {
            val existing = productId?.let { repository.getById(it) }
            if (existing != null) {
                _state.value = ProductFormState(
                    id = existing.id,
                    name = existing.name,
                    category = existing.category.ifBlank { com.miladsabagh.goldinvoice.data.entity.JEWELRY_CATEGORIES.first() },
                    code = existing.code,
                    weightGrams = existing.weightGrams.toString(),
                    karat = existing.karat,
                    laborFeePercent = existing.laborFeePercent.toString(),
                    profitPercent = existing.profitPercent.toString(),
                    taxPercent = existing.taxPercent.toString(),
                    quantity = existing.quantity.toString(),
                    description = existing.description,
                    isLoaded = true
                )
            } else {
                val settings = settingsRepository.settingsFlow.first()
                _state.value = _state.value.copy(
                    laborFeePercent = settings.defaultLaborFeePercent.toString(),
                    profitPercent = settings.defaultProfitPercent.toString(),
                    taxPercent = settings.defaultTaxPercent.toString(),
                    isLoaded = true
                )
            }
        }
    }

    fun update(transform: (ProductFormState) -> ProductFormState) {
        _state.value = transform(_state.value)
    }

    fun save() {
        val current = _state.value
        val weight = com.miladsabagh.goldinvoice.util.parseLocalizedDouble(current.weightGrams)
        val nameValid = current.name.isNotBlank()
        val weightValid = weight != null && weight > 0
        if (!nameValid || !weightValid) {
            _state.value = current.copy(nameError = !nameValid, weightError = !weightValid)
            return
        }
        viewModelScope.launch {
            val product = Product(
                id = current.id,
                name = current.name.trim(),
                category = current.category,
                code = current.code.trim(),
                weightGrams = weight!!,
                karat = current.karat,
                laborFeePercent = com.miladsabagh.goldinvoice.util.parseLocalizedDouble(current.laborFeePercent) ?: 0.0,
                profitPercent = com.miladsabagh.goldinvoice.util.parseLocalizedDouble(current.profitPercent) ?: 0.0,
                taxPercent = com.miladsabagh.goldinvoice.util.parseLocalizedDouble(current.taxPercent) ?: 0.0,
                quantity = com.miladsabagh.goldinvoice.util.parseLocalizedInt(current.quantity) ?: 1,
                description = current.description.trim()
            )
            repository.save(product)
            saveCompleted.value = true
        }
    }
}
