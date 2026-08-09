package com.goldgallery.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldgallery.app.data.ShopRepository
import com.goldgallery.app.data.db.InvoiceWithItems
import com.goldgallery.app.data.db.ProductEntity
import com.goldgallery.app.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ShopRepository) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val invoices: StateFlow<List<InvoiceWithItems>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _goldPrice18 = MutableStateFlow(4_500_000L)
    val goldPrice18: StateFlow<Long> = _goldPrice18.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun setGoldPrice(price: Long) {
        if (price > 0) _goldPrice18.value = price
    }

    fun addToCart(product: ProductEntity) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = current[index]
            if (item.quantity < product.stock) {
                current[index] = item.copy(quantity = item.quantity + 1)
            }
        } else if (product.stock > 0) {
            current += CartItem(product, 1)
        }
        _cart.value = current
    }

    fun increment(productId: Long) {
        _cart.value = _cart.value.map { item ->
            if (item.product.id == productId && item.quantity < item.product.stock) {
                item.copy(quantity = item.quantity + 1)
            } else item
        }
    }

    fun decrement(productId: Long) {
        _cart.value = _cart.value.mapNotNull { item ->
            if (item.product.id == productId) {
                if (item.quantity > 1) item.copy(quantity = item.quantity - 1) else null
            } else item
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun issueInvoice(customerName: String, customerPhone: String, onIssued: (Long) -> Unit) {
        val cartSnapshot = _cart.value
        if (cartSnapshot.isEmpty() || customerName.isBlank()) return
        viewModelScope.launch {
            val id = repository.issueInvoice(
                customerName = customerName,
                customerPhone = customerPhone,
                goldPrice18 = _goldPrice18.value,
                cart = cartSnapshot,
            )
            _cart.value = emptyList()
            onIssued(id)
        }
    }

    fun invoiceFlow(id: Long) = repository.invoice(id)

    companion object {
        fun factory(repository: ShopRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ShopViewModel(repository) as T
            }
    }
}
