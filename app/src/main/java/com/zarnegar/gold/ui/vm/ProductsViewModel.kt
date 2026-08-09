package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ProductCategory
import com.zarnegar.gold.domain.model.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val query: String = "",
    val category: ProductCategory? = null,
    val settings: ShopSettings = ShopSettings(),
) {
    val filtered: List<Product>
        get() = products.filter { product ->
            (category == null || product.category == category) &&
                (
                    query.isBlank() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.code.contains(query, ignoreCase = true)
                    )
        }
}

class ProductsViewModel(private val container: AppContainer) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ProductCategory?>(null)

    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    val uiState: StateFlow<ProductsUiState> = combine(
        container.productRepository.products,
        query,
        category,
        container.settingsRepository.settings,
    ) { products, q, cat, settings ->
        ProductsUiState(products, q, cat, settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: ProductCategory?) {
        category.value = value
    }

    fun save(product: Product) {
        viewModelScope.launch {
            container.productRepository.upsert(product)
            _lastMessage.value = if (product.id == 0L) "کالا ثبت شد" else "کالا به‌روزرسانی شد"
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            container.productRepository.delete(product)
            _lastMessage.value = "کالا حذف شد"
        }
    }

    fun consumeMessage() {
        _lastMessage.value = null
    }

    suspend fun find(id: Long): Product? = container.productRepository.find(id)
}
