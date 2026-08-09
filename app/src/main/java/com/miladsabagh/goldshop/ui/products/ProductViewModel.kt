package com.miladsabagh.goldshop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.Product
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductListUiState(
    val query: String = "",
    val products: List<Product> = emptyList()
)

class ProductViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProductListUiState> = combine(
        queryFlow,
        queryFlow.flatMapLatest { repository.searchProducts(it) }
    ) { query, products -> ProductListUiState(query, products) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductListUiState())

    fun onQueryChange(query: String) {
        queryFlow.update { query }
    }

    suspend fun getProduct(id: Long): Product? = repository.getProduct(id)

    fun saveProduct(product: Product, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveProduct(product)
            onSaved(id)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
