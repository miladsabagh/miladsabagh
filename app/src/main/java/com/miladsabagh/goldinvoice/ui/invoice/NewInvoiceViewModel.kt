package com.miladsabagh.goldinvoice.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.entity.Customer
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.entity.InvoiceItem
import com.miladsabagh.goldinvoice.data.entity.PaymentStatus
import com.miladsabagh.goldinvoice.data.entity.Product
import com.miladsabagh.goldinvoice.data.repository.CustomerRepository
import com.miladsabagh.goldinvoice.data.repository.InvoiceRepository
import com.miladsabagh.goldinvoice.data.repository.ProductRepository
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository
import com.miladsabagh.goldinvoice.domain.PriceCalculator
import com.miladsabagh.goldinvoice.util.parseLocalizedDouble
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class InvoiceLineDraft(
    val localId: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val itemName: String,
    val weightGrams: Double,
    val karat: Int,
    val laborFeePercent: Double,
    val profitPercent: Double,
    val taxPercent: Double,
    val quantity: Int
) {
    fun breakdown(goldPricePerGram18k: Double): PriceCalculator.LineBreakdown =
        PriceCalculator.computeLine(
            weightGrams = weightGrams,
            karat = karat,
            goldPricePerGram18k = goldPricePerGram18k,
            laborFeePercent = laborFeePercent,
            profitPercent = profitPercent,
            taxPercent = taxPercent,
            quantity = quantity
        )
}

data class NewInvoiceUiState(
    val customer: Customer? = null,
    val goldPricePerGram18k: String = "",
    val items: List<InvoiceLineDraft> = emptyList(),
    val discountPercent: String = "0",
    val paidAmount: String = "",
    val notes: String = "",
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val defaultLaborFeePercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val defaultTaxPercent: Double = 9.0,
    val savedInvoiceId: Long? = null,
    val errorMessage: String? = null
) {
    val goldPriceValue: Double get() = parseLocalizedDouble(goldPricePerGram18k) ?: 0.0
    val discountPercentValue: Double get() = parseLocalizedDouble(discountPercent) ?: 0.0
    val paidAmountValue: Double get() = parseLocalizedDouble(paidAmount) ?: 0.0

    val lineBreakdowns: List<Pair<InvoiceLineDraft, PriceCalculator.LineBreakdown>>
        get() = items.map { it to it.breakdown(goldPriceValue) }

    val totals: PriceCalculator.InvoiceTotals
        get() = PriceCalculator.computeInvoiceTotals(
            lineTotals = lineBreakdowns.map { it.second.lineTotal },
            discountPercent = discountPercentValue
        )
}

class NewInvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewInvoiceUiState())
    val state: StateFlow<NewInvoiceUiState> = _state

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            _state.value = _state.value.copy(
                goldPricePerGram18k = if (settings.goldPricePerGram18k > 0) settings.goldPricePerGram18k.toLong().toString() else "",
                defaultLaborFeePercent = settings.defaultLaborFeePercent,
                defaultProfitPercent = settings.defaultProfitPercent,
                defaultTaxPercent = settings.defaultTaxPercent
            )
        }
        viewModelScope.launch {
            productRepository.observeAll().collect { products ->
                _state.value = _state.value.copy(products = products)
            }
        }
        viewModelScope.launch {
            customerRepository.observeAll().collect { customers ->
                _state.value = _state.value.copy(customers = customers)
            }
        }
    }

    fun setCustomer(customer: Customer?) {
        _state.value = _state.value.copy(customer = customer)
    }

    fun setGoldPrice(value: String) {
        _state.value = _state.value.copy(goldPricePerGram18k = value)
    }

    fun setDiscountPercent(value: String) {
        _state.value = _state.value.copy(discountPercent = value)
    }

    fun setPaidAmount(value: String) {
        _state.value = _state.value.copy(paidAmount = value)
    }

    fun setNotes(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun addLineFromProduct(product: Product, quantity: Int) {
        val draft = InvoiceLineDraft(
            productId = product.id,
            itemName = product.name,
            weightGrams = product.weightGrams,
            karat = product.karat,
            laborFeePercent = product.laborFeePercent,
            profitPercent = product.profitPercent,
            taxPercent = product.taxPercent,
            quantity = quantity
        )
        _state.value = _state.value.copy(items = _state.value.items + draft)
    }

    fun addManualLine(
        name: String,
        weightGrams: Double,
        karat: Int,
        laborFeePercent: Double,
        profitPercent: Double,
        taxPercent: Double,
        quantity: Int
    ) {
        val draft = InvoiceLineDraft(
            itemName = name,
            weightGrams = weightGrams,
            karat = karat,
            laborFeePercent = laborFeePercent,
            profitPercent = profitPercent,
            taxPercent = taxPercent,
            quantity = quantity
        )
        _state.value = _state.value.copy(items = _state.value.items + draft)
    }

    fun removeLine(localId: String) {
        _state.value = _state.value.copy(items = _state.value.items.filterNot { it.localId == localId })
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun saveInvoice() {
        val current = _state.value
        if (current.items.isEmpty()) {
            _state.value = current.copy(errorMessage = "حداقل یک ردیف کالا اضافه کنید")
            return
        }
        if (current.goldPriceValue <= 0) {
            _state.value = current.copy(errorMessage = "قیمت طلا را وارد کنید")
            return
        }
        viewModelScope.launch {
            val invoiceNumber = invoiceRepository.nextInvoiceNumber()
            val totals = current.totals
            val paid = current.paidAmountValue
            val status = when {
                paid >= totals.grandTotal && totals.grandTotal > 0 -> PaymentStatus.PAID
                paid > 0 -> PaymentStatus.PARTIAL
                else -> PaymentStatus.UNPAID
            }
            val invoice = Invoice(
                invoiceNumber = invoiceNumber,
                customerId = current.customer?.id,
                customerNameSnapshot = current.customer?.fullName ?: "",
                customerPhoneSnapshot = current.customer?.phone ?: "",
                goldPricePerGram18k = current.goldPriceValue,
                subtotal = totals.subtotal,
                discountPercent = current.discountPercentValue,
                discountAmount = totals.discountAmount,
                grandTotal = totals.grandTotal,
                paidAmount = paid,
                paymentStatus = status,
                notes = current.notes.trim()
            )
            val items = current.lineBreakdowns.map { (draft, breakdown) ->
                InvoiceItem(
                    invoiceId = 0,
                    productId = draft.productId,
                    itemName = draft.itemName,
                    weightGrams = draft.weightGrams,
                    karat = draft.karat,
                    goldPricePerGram = breakdown.pricePerGramForKarat,
                    laborFeePercent = draft.laborFeePercent,
                    profitPercent = draft.profitPercent,
                    taxPercent = draft.taxPercent,
                    quantity = draft.quantity,
                    baseGoldValue = breakdown.baseGoldValue,
                    laborFeeAmount = breakdown.laborFeeAmount,
                    profitAmount = breakdown.profitAmount,
                    taxAmount = breakdown.taxAmount,
                    unitPrice = breakdown.unitPrice,
                    lineTotal = breakdown.lineTotal
                )
            }
            val id = invoiceRepository.createInvoice(invoice, items)
            _state.value = _state.value.copy(savedInvoiceId = id)
        }
    }
}
