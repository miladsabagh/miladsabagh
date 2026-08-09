package ir.goldshop.app.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.goldshop.app.data.dao.InvoiceWithItems
import ir.goldshop.app.data.entity.Customer
import ir.goldshop.app.data.entity.Invoice
import ir.goldshop.app.data.entity.InvoiceItem
import ir.goldshop.app.data.entity.PaymentMethod
import ir.goldshop.app.data.entity.Product
import ir.goldshop.app.data.repository.GoldShopRepository
import ir.goldshop.app.domain.PricingCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewInvoiceUiState(
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val goldPricePerGram: String = "",
    val defaultTaxPercent: Double = 9.0,
    val items: List<DraftInvoiceItem> = emptyList(),
    val discountAmount: String = "0",
    val paidAmount: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = ""
) {
    val subtotal: Double get() = items.sumOf { it.result.lineTotal }
    val totalWeight: Double get() = items.sumOf { it.weightGrams * it.quantity }
    val discount: Double get() = discountAmount.toDoubleOrNull() ?: 0.0
    val total: Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val isValid: Boolean get() = customerName.isNotBlank() && items.isNotEmpty() &&
        (goldPricePerGram.toDoubleOrNull() ?: 0.0) > 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceViewModel(private val repository: GoldShopRepository) : ViewModel() {

    // ---- List / search of saved invoices ----
    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    val invoices: StateFlow<List<Invoice>> = searchQuery
        .flatMapLatest { repository.searchInvoices(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    val customers: StateFlow<List<Customer>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- New invoice draft state ----
    private val _draft = MutableStateFlow(NewInvoiceUiState())
    val draft: StateFlow<NewInvoiceUiState> = _draft

    private var nextLocalId = 1L

    fun initDraftFromSettings() {
        viewModelScope.launch {
            val settings = repository.getSettingsOrDefault()
            _draft.value = _draft.value.copy(
                goldPricePerGram = if (settings.goldPricePerGram > 0) settings.goldPricePerGram.toString() else "",
                defaultTaxPercent = settings.defaultTaxPercent
            )
        }
    }

    fun resetDraft() {
        _draft.value = NewInvoiceUiState()
        nextLocalId = 1L
    }

    fun selectCustomer(customer: Customer) {
        _draft.value = _draft.value.copy(
            customerId = customer.id,
            customerName = customer.fullName,
            customerPhone = customer.phoneNumber,
            customerAddress = customer.address
        )
    }

    fun updateManualCustomer(name: String, phone: String, address: String) {
        _draft.value = _draft.value.copy(
            customerId = null,
            customerName = name,
            customerPhone = phone,
            customerAddress = address
        )
    }

    fun updateGoldPrice(price: String) {
        _draft.value = _draft.value.copy(goldPricePerGram = price)
    }

    fun updateDiscount(discount: String) {
        _draft.value = _draft.value.copy(discountAmount = discount)
    }

    fun updatePaidAmount(paid: String) {
        _draft.value = _draft.value.copy(paidAmount = paid)
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _draft.value = _draft.value.copy(paymentMethod = method)
    }

    fun updateNotes(notes: String) {
        _draft.value = _draft.value.copy(notes = notes)
    }

    fun addItemFromProduct(product: Product) {
        val currentGoldPrice = _draft.value.goldPricePerGram.toDoubleOrNull() ?: 0.0
        val pricePerGram = PricingCalculator.pricePerGramForKarat(currentGoldPrice, product.karat)
        val item = DraftInvoiceItem(
            localId = nextLocalId++,
            productId = product.id,
            itemName = product.name,
            karat = product.karat,
            weightGrams = product.weightGrams,
            quantity = 1,
            pricePerGram = pricePerGram,
            laborPercent = product.laborPercent,
            profitPercent = product.profitPercent,
            taxPercent = _draft.value.defaultTaxPercent
        )
        _draft.value = _draft.value.copy(items = _draft.value.items + item)
    }

    fun addBlankItem() {
        val currentGoldPrice = _draft.value.goldPricePerGram.toDoubleOrNull() ?: 0.0
        val item = DraftInvoiceItem(
            localId = nextLocalId++,
            itemName = "",
            karat = 18,
            weightGrams = 0.0,
            quantity = 1,
            pricePerGram = PricingCalculator.pricePerGramForKarat(currentGoldPrice, 18),
            laborPercent = 7.0,
            profitPercent = 7.0,
            taxPercent = _draft.value.defaultTaxPercent
        )
        _draft.value = _draft.value.copy(items = _draft.value.items + item)
    }

    fun updateItem(updated: DraftInvoiceItem) {
        _draft.value = _draft.value.copy(
            items = _draft.value.items.map { if (it.localId == updated.localId) updated else it }
        )
    }

    fun removeItem(localId: Long) {
        _draft.value = _draft.value.copy(items = _draft.value.items.filterNot { it.localId == localId })
    }

    suspend fun submitInvoice(): Long? {
        val state = _draft.value
        if (!state.isValid) return null

        val invoiceNumber = repository.generateNextInvoiceNumber()
        val goldPrice = state.goldPricePerGram.toDoubleOrNull() ?: 0.0

        val invoice = Invoice(
            invoiceNumber = invoiceNumber,
            customerId = state.customerId,
            customerName = state.customerName,
            customerPhone = state.customerPhone,
            customerAddress = state.customerAddress,
            goldPricePerGramSnapshot = goldPrice,
            totalWeightGrams = state.totalWeight,
            subtotalAmount = state.subtotal,
            discountAmount = state.discount,
            totalAmount = state.total,
            paidAmount = state.paidAmount.toDoubleOrNull() ?: state.total,
            paymentMethod = state.paymentMethod.persianLabel,
            notes = state.notes
        )

        val items = state.items.map { draft ->
            val result = draft.result
            InvoiceItem(
                invoiceId = 0L,
                productId = draft.productId,
                itemName = draft.itemName.ifBlank { "قلم بدون‌نام" },
                karat = draft.karat,
                weightGrams = draft.weightGrams,
                quantity = draft.quantity,
                pricePerGram = draft.pricePerGram,
                laborPercent = draft.laborPercent,
                profitPercent = draft.profitPercent,
                taxPercent = draft.taxPercent,
                baseAmount = result.baseAmount,
                laborAmount = result.laborAmount,
                profitAmount = result.profitAmount,
                taxAmount = result.taxAmount,
                lineTotal = result.lineTotal
            )
        }

        return repository.createInvoice(invoice, items)
    }

    // ---- Invoice detail ----
    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? = repository.getInvoiceWithItems(id)

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}
