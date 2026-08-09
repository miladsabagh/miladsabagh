package com.goldjewelry.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldjewelry.app.GoldJewelryApp
import com.goldjewelry.app.data.model.Product
import com.goldjewelry.app.data.model.ProductCategory
import com.goldjewelry.app.data.model.ShopSettings
import com.goldjewelry.app.util.GoldPriceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedCategory: ProductCategory? = null,
    val searchQuery: String = "",
    val goldPrice: Long = 0
)

class ProductsViewModel(app: GoldJewelryApp) : ViewModel() {

    private val productRepo = app.productRepository
    private val settingsRepo = app.settingsRepository

    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ProductsUiState> = combine(
        productRepo.getAllActive(),
        settingsRepo.getSettings(),
        _selectedCategory,
        _searchQuery
    ) { products, settings, category, query ->
        val filtered = products
            .filter { category == null || it.category == category }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.sku.contains(query, ignoreCase = true)
            }
        ProductsUiState(
            products = products,
            filteredProducts = filtered,
            selectedCategory = category,
            searchQuery = query,
            goldPrice = settings?.goldPricePerGram ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductsUiState())

    fun setCategory(category: ProductCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getProductPrice(product: Product, goldPrice: Long): Long {
        return GoldPriceCalculator.calculateProductPrice(product, goldPrice)
    }

    fun addProduct(product: Product, onDone: () -> Unit) {
        viewModelScope.launch {
            productRepo.insert(product)
            onDone()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            productRepo.softDelete(id)
        }
    }

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductsViewModel(app) as T
        }
    }
}
