package com.miladsabagh.goldshop.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldshop.data.local.entity.Customer
import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.data.local.entity.InvoiceStatus
import com.miladsabagh.goldshop.data.local.entity.Product
import com.miladsabagh.goldshop.data.local.entity.ProductCategory
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import com.miladsabagh.goldshop.data.settings.SettingsRepository
import com.miladsabagh.goldshop.util.PriceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DraftInvoiceItem(
    val localId: Long,
    val productId: Long? = null,
    val name: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val weightGrams: Double,
    val karat: Int,
    val laborFeePercent: Double,
    val profitPercent: Double,
    val stonePrice: Double = 0.0,
    val quantity: Int = 1
)

data class NewInvoiceUiState(
    val customer: Customer? = null,
    val walkInName: String = "مشتری نقدی",
    val walkInPhone: String = "",
    val goldPricePerGram: Double = 0.0,
    val taxPercent: Double = 9.0,
    val discountAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val items: List<DraftInvoiceItem> = emptyList(),
    val savedInvoiceId: Long? = null
) {
    val customerDisplayName: String get() = customer?.fullName?.ifBlank { walkInName } ?: walkInName
    val customerDisplayPhone: String get() = customer?.phone ?: walkInPhone

    fun breakdownFor(item: DraftInvoiceItem) = PriceCalculator.calculate(
        weightGrams = item.weightGrams,
        karat = item.karat,
        pricePerGram18k = goldPricePerGram,
        laborFeePercent = item.laborFeePercent,
        profitPercent = item.profitPercent,
        taxPercent = taxPercent,
        stonePrice = item.stonePrice,
        quantity = item.quantity
    )

    val totalWeight: Double get() = items.sumOf { it.weightGrams * it.quantity }
    val subtotal: Double get() = items.sumOf { breakdownFor(it).lineTotal - breakdownFor(it).taxAmount * it.quantity }
    val totalTax: Double get() = items.sumOf { breakdownFor(it).taxAmount * it.quantity }
    val grandTotalBeforeDiscount: Double get() = items.sumOf { breakdownFor(it).lineTotal }
    val grandTotal: Double get() = (grandTotalBeforeDiscount - discountAmount).coerceAtLeast(0.0)
    val remaining: Double get() = (grandTotal - paidAmount).coerceAtLeast(0.0)
    val canSave: Boolean get() = items.isNotEmpty() && goldPricePerGram > 0
}

class NewInvoiceViewModel(
    private val repository: GoldShopRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewInvoiceUiState())
    val uiState: StateFlow<NewInvoiceUiState> = _uiState.asStateFlow()

    private var itemIdCounter = 0L

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow
            settings.collect { s ->
                _uiState.update { current ->
                    if (current.items.isEmpty() && current.goldPricePerGram == 0.0) {
                        current.copy(goldPricePerGram = s.currentGoldPrice, taxPercent = s.defaultTaxPercent)
                    } else current
                }
            }
        }
    }

    fun selectCustomer(customer: Customer?) {
        _uiState.update { it.copy(customer = customer) }
    }

    fun setWalkInName(name: String) {
        _uiState.update { it.copy(walkInName = name, customer = null) }
    }

    fun setWalkInPhone(phone: String) {
        _uiState.update { it.copy(walkInPhone = phone) }
    }

    fun updateGoldPrice(price: Double) {
        _uiState.update { it.copy(goldPricePerGram = price) }
    }

    fun updateTaxPercent(percent: Double) {
        _uiState.update { it.copy(taxPercent = percent) }
    }

    fun updateDiscount(amount: Double) {
        _uiState.update { it.copy(discountAmount = amount) }
    }

    fun updatePaidAmount(amount: Double) {
        _uiState.update { it.copy(paidAmount = amount) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun addItemFromProduct(product: Product, quantity: Int = 1) {
        val draft = DraftInvoiceItem(
            localId = ++itemIdCounter,
            productId = product.id,
            name = product.name,
            category = product.category,
            weightGrams = product.weightGrams,
            karat = product.karat,
            laborFeePercent = product.laborFeePercent,
            profitPercent = product.profitPercent,
            stonePrice = product.stonePrice,
            quantity = quantity
        )
        _uiState.update { it.copy(items = it.items + draft) }
    }

    fun addCustomItem(
        name: String,
        category: ProductCategory,
        weightGrams: Double,
        karat: Int,
        laborFeePercent: Double,
        profitPercent: Double,
        stonePrice: Double,
        quantity: Int
    ) {
        val draft = DraftInvoiceItem(
            localId = ++itemIdCounter,
            productId = null,
            name = name,
            category = category,
            weightGrams = weightGrams,
            karat = karat,
            laborFeePercent = laborFeePercent,
            profitPercent = profitPercent,
            stonePrice = stonePrice,
            quantity = quantity
        )
        _uiState.update { it.copy(items = it.items + draft) }
    }

    fun removeItem(localId: Long) {
        _uiState.update { it.copy(items = it.items.filterNot { item -> item.localId == localId }) }
    }

    fun saveInvoice() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            val number = repository.nextInvoiceNumber()
            val invoice = Invoice(
                invoiceNumber = number,
                customerId = state.customer?.id,
                customerName = state.customerDisplayName,
                customerPhone = state.customerDisplayPhone,
                goldPriceAtSale = state.goldPricePerGram,
                taxPercent = state.taxPercent,
                discountAmount = state.discountAmount,
                totalWeightGrams = state.totalWeight,
                subtotalAmount = state.subtotal,
                taxAmount = state.totalTax,
                totalAmount = state.grandTotal,
                paidAmount = state.paidAmount,
                notes = state.notes,
                status = when {
                    state.paidAmount >= state.grandTotal -> InvoiceStatus.PAID
                    state.paidAmount <= 0.0 -> InvoiceStatus.UNPAID
                    else -> InvoiceStatus.PARTIAL
                }
            )
            val invoiceItems = state.items.map { item ->
                val breakdown = state.breakdownFor(item)
                InvoiceItem(
                    productId = item.productId,
                    itemName = item.name,
                    category = item.category,
                    weightGrams = item.weightGrams,
                    karat = item.karat,
                    quantity = item.quantity,
                    goldPricePerGram = PriceCalculator.pricePerGramForKarat(state.goldPricePerGram, item.karat),
                    laborFeePercent = item.laborFeePercent,
                    laborFeeAmount = breakdown.laborFeeAmount,
                    profitPercent = item.profitPercent,
                    profitAmount = breakdown.profitAmount,
                    stonePrice = item.stonePrice,
                    taxAmount = breakdown.taxAmount * item.quantity,
                    lineTotal = breakdown.lineTotal
                )
            }
            val id = repository.createInvoice(invoice, invoiceItems)
            _uiState.update { it.copy(savedInvoiceId = id) }
        }
    }

    fun resetSavedFlag() {
        _uiState.update { it.copy(savedInvoiceId = null) }
    }
}
