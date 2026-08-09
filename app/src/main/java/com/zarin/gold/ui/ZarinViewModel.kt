package com.zarin.gold.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zarin.gold.data.Customer
import com.zarin.gold.data.Invoice
import com.zarin.gold.data.InvoiceLine
import com.zarin.gold.data.Product
import com.zarin.gold.data.ProductCategory
import com.zarin.gold.data.ZarinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val selectedCategory: ProductCategory? = null,
    val query: String = ""
) {
    val filtered: List<Product>
        get() = products.filter { product ->
            val catOk = selectedCategory == null || product.category == selectedCategory
            val q = query.trim()
            val queryOk = q.isEmpty() || product.name.contains(q, ignoreCase = true)
            catOk && queryOk
        }
}

data class InvoiceDraft(
    val customerName: String = "",
    val customerPhone: String = "",
    val lines: List<InvoiceLine> = emptyList(),
    val discountText: String = "0",
    val note: String = "",
    val lastCreatedId: Long? = null
) {
    val discount: Long get() = discountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val subtotal: Long get() = lines.sumOf { it.lineTotal }
    val tax: Long get() = ((subtotal - discount).coerceAtLeast(0) * 0.09).toLong()
    val total: Long get() = subtotal - discount + tax
}

data class HomeUiState(
    val goldPrice18: Long = 0,
    val productCount: Int = 0,
    val invoiceCount: Int = 0,
    val recentInvoices: List<Invoice> = emptyList()
)

class ZarinViewModel(private val repository: ZarinRepository) : ViewModel() {

    val goldPrice18 = repository.goldPrice18

    val homeState: StateFlow<HomeUiState> = combine(
        repository.goldPrice18,
        repository.products,
        repository.invoices
    ) { price, products, invoices ->
        HomeUiState(
            goldPrice18 = price,
            productCount = products.size,
            invoiceCount = invoices.size,
            recentInvoices = invoices.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _catalog = MutableStateFlow(CatalogUiState())
    val catalog: StateFlow<CatalogUiState> = _catalog.asStateFlow()

    private val _draft = MutableStateFlow(InvoiceDraft())
    val draft: StateFlow<InvoiceDraft> = _draft.asStateFlow()

    val invoices = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customers = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.seedIfNeeded() }
        viewModelScope.launch {
            repository.products.collect { list ->
                _catalog.update { it.copy(products = list) }
            }
        }
    }

    fun setCategory(category: ProductCategory?) {
        _catalog.update { it.copy(selectedCategory = category) }
    }

    fun setQuery(query: String) {
        _catalog.update { it.copy(query = query) }
    }

    fun updateGoldPrice(raw: String) {
        val value = raw.filter { it.isDigit() }.toLongOrNull() ?: return
        repository.updateGoldPrice(value)
    }

    fun selectCustomer(customer: Customer) {
        _draft.update {
            it.copy(customerName = customer.name, customerPhone = customer.phone)
        }
    }

    fun updateDraftCustomer(name: String, phone: String) {
        _draft.update { it.copy(customerName = name, customerPhone = phone) }
    }

    fun updateDiscount(value: String) {
        _draft.update { it.copy(discountText = value.filter { ch -> ch.isDigit() }) }
    }

    fun updateNote(note: String) {
        _draft.update { it.copy(note = note) }
    }

    fun addProductToDraft(product: Product) {
        _draft.update { state ->
            val existing = state.lines.indexOfFirst { it.productId == product.id }
            val lines = if (existing >= 0) {
                state.lines.toMutableList().also { list ->
                    val line = list[existing]
                    list[existing] = line.copy(quantity = line.quantity + 1)
                }
            } else {
                state.lines + repository.lineFromProduct(product)
            }
            state.copy(lines = lines)
        }
    }

    fun changeQuantity(productId: Long, delta: Int) {
        _draft.update { state ->
            val lines = state.lines.mapNotNull { line ->
                if (line.productId != productId) line
                else {
                    val q = line.quantity + delta
                    if (q <= 0) null else line.copy(quantity = q)
                }
            }
            state.copy(lines = lines)
        }
    }

    fun removeLine(productId: Long) {
        _draft.update { it.copy(lines = it.lines.filterNot { line -> line.productId == productId }) }
    }

    fun clearDraftCreatedFlag() {
        _draft.update { it.copy(lastCreatedId = null) }
    }

    fun submitInvoice(onDone: (Long) -> Unit) {
        val draft = _draft.value
        if (draft.customerName.isBlank() || draft.lines.isEmpty()) return
        viewModelScope.launch {
            val id = repository.createInvoice(
                customerName = draft.customerName,
                customerPhone = draft.customerPhone,
                lines = draft.lines,
                discount = draft.discount,
                note = draft.note
            )
            _draft.value = InvoiceDraft(lastCreatedId = id)
            onDone(id)
        }
    }

    suspend fun getInvoice(id: Long): Invoice? = repository.getInvoice(id)

    fun addCustomer(name: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCustomer(name, phone) }
    }

    companion object {
        fun factory(repository: ZarinRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ZarinViewModel(repository) as T
                }
            }
    }
}
