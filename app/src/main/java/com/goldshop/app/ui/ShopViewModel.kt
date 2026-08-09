package com.goldshop.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldshop.app.GoldShopApplication
import com.goldshop.app.data.model.CartItem
import com.goldshop.app.data.model.Customer
import com.goldshop.app.data.model.DashboardStats
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.InvoiceWithItems
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ProductCategory
import com.goldshop.app.data.model.ShopSettings
import com.goldshop.app.data.repository.ShopRepository
import com.goldshop.app.util.InvoicePdfGenerator
import com.goldshop.app.util.PriceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceDraftUi(
    val cart: List<CartItem> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val discountText: String = "0",
    val notes: String = "",
    val paidText: String = "",
    val lastCreatedInvoiceId: Long? = null,
    val error: String? = null
)

class ShopViewModel(
    application: Application,
    private val repository: ShopRepository
) : AndroidViewModel(application) {

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.observeInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<ShopSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettings())

    val dashboard: StateFlow<DashboardStats> = repository.observeDashboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    private val _draft = MutableStateFlow(InvoiceDraftUi())
    val draft: StateFlow<InvoiceDraftUi> = _draft.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoice: StateFlow<InvoiceWithItems?> = _selectedInvoice.asStateFlow()

    val cartTotals = combine(draft, settings) { d, s ->
        val discount = d.discountText.toLongOrNull() ?: 0L
        val subtotal = PriceCalculator.cartSubtotal(d.cart, s.goldPricePerGram18)
        val tax = PriceCalculator.taxAmount(subtotal, discount, s.taxPercent)
        val total = PriceCalculator.grandTotal(subtotal, discount, tax)
        Triple(subtotal, tax, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Triple(0L, 0L, 0L))

    fun saveProduct(
        id: Long,
        name: String,
        category: ProductCategory,
        weight: String,
        purity: String,
        labor: String,
        profit: String,
        stock: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.saveProduct(
                Product(
                    id = id,
                    name = name.trim(),
                    category = category,
                    weightGrams = weight.toDoubleOrNull() ?: 0.0,
                    purity = purity.toIntOrNull() ?: 750,
                    laborPercent = labor.toDoubleOrNull() ?: 0.0,
                    profitPercent = profit.toDoubleOrNull() ?: 0.0,
                    stockQuantity = stock.toIntOrNull() ?: 0,
                    description = description.trim()
                )
            )
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun saveCustomer(
        id: Long,
        name: String,
        phone: String,
        nationalId: String,
        address: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.saveCustomer(
                Customer(
                    id = id,
                    fullName = name.trim(),
                    phone = phone.trim(),
                    nationalId = nationalId.trim(),
                    address = address.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    fun updateSettings(shopSettings: ShopSettings) {
        viewModelScope.launch { repository.saveSettings(shopSettings) }
    }

    fun addToCart(product: Product) {
        _draft.update { state ->
            val existing = state.cart.find { it.product.id == product.id }
            val newCart = if (existing != null) {
                state.cart.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cart + CartItem(product)
            }
            state.copy(cart = newCart, error = null)
        }
    }

    fun removeFromCart(productId: Long) {
        _draft.update { it.copy(cart = it.cart.filterNot { c -> c.product.id == productId }) }
    }

    fun changeCartQty(productId: Long, qty: Int) {
        if (qty <= 0) {
            removeFromCart(productId)
            return
        }
        _draft.update { state ->
            state.copy(cart = state.cart.map {
                if (it.product.id == productId) it.copy(quantity = qty) else it
            })
        }
    }

    fun selectCustomer(customer: Customer?) {
        _draft.update {
            it.copy(
                selectedCustomer = customer,
                customerName = customer?.fullName.orEmpty(),
                customerPhone = customer?.phone.orEmpty()
            )
        }
    }

    fun updateDraft(
        customerName: String? = null,
        customerPhone: String? = null,
        discountText: String? = null,
        notes: String? = null,
        paidText: String? = null
    ) {
        _draft.update {
            it.copy(
                customerName = customerName ?: it.customerName,
                customerPhone = customerPhone ?: it.customerPhone,
                discountText = discountText ?: it.discountText,
                notes = notes ?: it.notes,
                paidText = paidText ?: it.paidText
            )
        }
    }

    fun clearDraft() {
        _draft.value = InvoiceDraftUi()
    }

    fun issueInvoice(onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val d = _draft.value
            if (d.cart.isEmpty()) {
                _draft.update { it.copy(error = "لطفاً حداقل یک کالا به فاکتور اضافه کنید") }
                return@launch
            }
            try {
                val id = repository.createInvoice(
                    cart = d.cart,
                    customer = d.selectedCustomer,
                    customerNameOverride = d.customerName,
                    customerPhoneOverride = d.customerPhone,
                    discount = d.discountText.toLongOrNull() ?: 0L,
                    notes = d.notes,
                    paidAmount = d.paidText.toLongOrNull() ?: 0L
                )
                _draft.value = InvoiceDraftUi(lastCreatedInvoiceId = id)
                onDone(id)
            } catch (e: Exception) {
                _draft.update { it.copy(error = e.message ?: "خطا در صدور فاکتور") }
            }
        }
    }

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            _selectedInvoice.value = repository.getInvoiceWithItems(id)
        }
    }

    fun clearSelectedInvoice() {
        _selectedInvoice.value = null
    }

    fun exportInvoicePdf(invoiceId: Long, onReady: (java.io.File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val detail = repository.getInvoiceWithItems(invoiceId)
            if (detail == null) {
                onError("فاکتور پیدا نشد")
                return@launch
            }
            try {
                val file = InvoicePdfGenerator.generate(
                    context = getApplication(),
                    invoiceWithItems = detail,
                    settings = settings.value
                )
                onReady(file)
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ساخت PDF")
            }
        }
    }

    companion object {
        fun factory(app: GoldShopApplication): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShopViewModel(app, app.repository) as T
                }
            }
    }
}
