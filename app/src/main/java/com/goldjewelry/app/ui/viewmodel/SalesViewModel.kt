package com.goldjewelry.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldjewelry.app.GoldJewelryApp
import com.goldjewelry.app.data.model.CartItem
import com.goldjewelry.app.data.model.Customer
import com.goldjewelry.app.data.model.Product
import com.goldjewelry.app.data.model.ShopSettings
import com.goldjewelry.app.util.GoldPriceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SalesUiState(
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val discount: String = "",
    val notes: String = "",
    val goldPrice: Long = 0,
    val taxPercent: Double = 9.0,
    val isCreating: Boolean = false,
    val createdInvoiceId: Long? = null
) {
    val subtotal: Long
        get() = cart.sumOf { it.lineTotal(goldPrice) }

    val discountAmount: Long
        get() = discount.toLongOrNull() ?: 0L

    val tax: Long
        get() = ((subtotal - discountAmount) * taxPercent / 100.0).toLong()

    val total: Long
        get() = subtotal - discountAmount + tax
}

class SalesViewModel(app: GoldJewelryApp) : ViewModel() {

    private val productRepo = app.productRepository
    private val customerRepo = app.customerRepository
    private val invoiceRepo = app.invoiceRepository
    private val settingsRepo = app.settingsRepository

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    private val _customerName = MutableStateFlow("")
    private val _customerPhone = MutableStateFlow("")
    private val _discount = MutableStateFlow("")
    private val _notes = MutableStateFlow("")
    private val _isCreating = MutableStateFlow(false)
    private val _createdInvoiceId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<SalesUiState> = combine(
        combine(
            productRepo.getAllActive(),
            customerRepo.getAll(),
            settingsRepo.getSettings()
        ) { products, customers, settings ->
            Triple(products, customers, settings)
        },
        combine(
            _cart,
            _selectedCustomer,
            _customerName,
            _customerPhone,
            _discount
        ) { cart, customer, name, phone, discount ->
            SalesFormState(cart, customer, name, phone, discount)
        },
        combine(_notes, _isCreating, _createdInvoiceId) { notes, creating, invoiceId ->
            Triple(notes, creating, invoiceId)
        }
    ) { dataTriple, formState, metaTriple ->
        val (products, customers, settings) = dataTriple
        val (notes, creating, invoiceId) = metaTriple
        SalesUiState(
            products = products,
            cart = formState.cart,
            customers = customers,
            selectedCustomer = formState.selectedCustomer,
            customerName = formState.customerName,
            customerPhone = formState.customerPhone,
            discount = formState.discount,
            notes = notes,
            goldPrice = settings?.goldPricePerGram ?: 0,
            taxPercent = settings?.taxPercent ?: 9.0,
            isCreating = creating,
            createdInvoiceId = invoiceId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesUiState())

    private data class SalesFormState(
        val cart: List<CartItem>,
        val selectedCustomer: Customer?,
        val customerName: String,
        val customerPhone: String,
        val discount: String
    )

    fun addToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(product))
        }
        _cart.value = current
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cart.value = _cart.value.map {
            if (it.product.id == productId) it.copy(quantity = quantity) else it
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        customer?.let {
            _customerName.value = it.fullName
            _customerPhone.value = it.phone
        }
    }

    fun setCustomerName(name: String) { _customerName.value = name }
    fun setCustomerPhone(phone: String) { _customerPhone.value = phone }
    fun setDiscount(discount: String) { _discount.value = discount }
    fun setNotes(notes: String) { _notes.value = notes }

    fun getProductPrice(product: Product): Long {
        val goldPrice = uiState.value.goldPrice
        return GoldPriceCalculator.calculateProductPrice(product, goldPrice)
    }

    fun createInvoice(onSuccess: (Long) -> Unit) {
        if (_cart.value.isEmpty() || _customerName.value.isBlank()) return

        viewModelScope.launch {
            _isCreating.value = true
            try {
                val updatedSettings = settingsRepo.incrementInvoiceNumber()
                val invoiceId = invoiceRepo.createInvoice(
                    cartItems = _cart.value,
                    customer = _selectedCustomer.value,
                    customerName = _customerName.value,
                    customerPhone = _customerPhone.value,
                    settings = updatedSettings,
                    discount = _discount.value.toLongOrNull() ?: 0,
                    notes = _notes.value
                )
                _createdInvoiceId.value = invoiceId
                clearCart()
                onSuccess(invoiceId)
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomer.value = null
        _customerName.value = ""
        _customerPhone.value = ""
        _discount.value = ""
        _notes.value = ""
        _createdInvoiceId.value = null
    }

    class Factory(private val app: GoldJewelryApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SalesViewModel(app) as T
        }
    }
}
