package ir.goldshop.app.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.data.repository.GoldShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(private val repository: GoldShopRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    val products: StateFlow<List<Product>> = searchQuery
        .flatMapLatest { repository.searchProducts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    suspend fun getProduct(id: Long): Product? = repository.getProduct(id)

    fun saveProduct(product: Product, onSaved: () -> Unit) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.saveProduct(product)
            } else {
                repository.updateProduct(product)
            }
            onSaved()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
