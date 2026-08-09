package com.zarrin.goldshop.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zarrin.goldshop.data.CustomerEntity
import com.zarrin.goldshop.data.DashboardStats
import com.zarrin.goldshop.data.DraftInvoiceItem
import com.zarrin.goldshop.data.InvoiceEntity
import com.zarrin.goldshop.data.InvoiceWithItems
import com.zarrin.goldshop.data.ProductCategory
import com.zarrin.goldshop.data.ProductEntity
import com.zarrin.goldshop.data.ShopRepository
import com.zarrin.goldshop.data.ShopSettingsEntity
import com.zarrin.goldshop.domain.GoldPricing
import com.zarrin.goldshop.invoice.InvoicePdfWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceDraftUi(
    val selectedCustomer: CustomerEntity? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<DraftInvoiceItem> = emptyList(),
    val paidAmountText: String = "",
    val notes: String = "",
    val lastCreatedInvoiceId: Long? = null,
    val error: String? = null
) {
    fun previewTotals(settings: ShopSettingsEntity): GoldPricing.InvoiceTotals {
        val lines = items.flatMap { draft ->
            List(draft.quantity) {
                GoldPricing.lineTotal(
                    draft.product.weightGrams,
                    draft.product.purityKarat,
                    draft.product.makingFeePercent,
                    settings.goldPrice18PerGram
                )
            }
        }
        return GoldPricing.invoiceTotals(lines, settings.profitPercent, settings.vatPercent)
    }
}

class ShopViewModel(
    application: Application,
    private val repository: ShopRepository
) : AndroidViewModel(application) {

    val products = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val customers = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val invoices = repository.observeInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopSettingsEntity())

    private val _stats = MutableStateFlow(DashboardStats(0, 0, 0, 0, 0))
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _draft = MutableStateFlow(InvoiceDraftUi())
    val draft: StateFlow<InvoiceDraftUi> = _draft.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoice: StateFlow<InvoiceWithItems?> = _selectedInvoice.asStateFlow()

    val draftPreview = combine(draft, settings) { d, s -> d.previewTotals(s) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GoldPricing.InvoiceTotals(0, 0, 0, 0)
        )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshStats()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _stats.value = repository.dashboardStats()
        }
    }

    fun saveProduct(
        id: Long,
        name: String,
        category: ProductCategory,
        weight: Double,
        karat: Int,
        makingFee: Double,
        stock: Int,
        code: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.saveProduct(
                ProductEntity(
                    id = id,
                    name = name.trim(),
                    category = category,
                    weightGrams = weight,
                    purityKarat = karat,
                    makingFeePercent = makingFee,
                    stockCount = stock,
                    code = code.trim(),
                    notes = notes.trim()
                )
            )
            refreshStats()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            refreshStats()
        }
    }

    fun saveCustomer(
        id: Long,
        name: String,
        phone: String,
        nationalId: String,
        address: String
    ) {
        viewModelScope.launch {
            repository.saveCustomer(
                CustomerEntity(
                    id = id,
                    fullName = name.trim(),
                    phone = phone.trim(),
                    nationalId = nationalId.trim(),
                    address = address.trim()
                )
            )
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    fun saveSettings(updated: ShopSettingsEntity) {
        viewModelScope.launch { repository.saveSettings(updated) }
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _draft.update {
            it.copy(
                selectedCustomer = customer,
                customerName = customer?.fullName.orEmpty(),
                customerPhone = customer?.phone.orEmpty()
            )
        }
    }

    fun updateDraftCustomer(name: String, phone: String) {
        _draft.update { it.copy(customerName = name, customerPhone = phone) }
    }

    fun updatePaidAndNotes(paid: String, notes: String) {
        _draft.update { it.copy(paidAmountText = paid, notes = notes) }
    }

    fun addProductToDraft(product: ProductEntity) {
        _draft.update { state ->
            val existing = state.items.find { it.product.id == product.id }
            val items = if (existing == null) {
                state.items + DraftInvoiceItem(product, 1)
            } else {
                state.items.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            }
            state.copy(items = items, error = null)
        }
    }

    fun changeDraftQuantity(productId: Long, quantity: Int) {
        _draft.update { state ->
            state.copy(
                items = state.items.mapNotNull {
                    if (it.product.id != productId) it
                    else if (quantity <= 0) null
                    else it.copy(quantity = quantity)
                }
            )
        }
    }

    fun clearDraft(keepMessage: Boolean = false) {
        val lastId = if (keepMessage) _draft.value.lastCreatedInvoiceId else null
        _draft.value = InvoiceDraftUi(lastCreatedInvoiceId = lastId)
    }

    fun createInvoice() {
        viewModelScope.launch {
            val state = _draft.value
            if (state.items.isEmpty()) {
                _draft.update { it.copy(error = "حداقل یک کالا به فاکتور اضافه کنید") }
                return@launch
            }
            try {
                val paid = state.paidAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
                val id = repository.createInvoice(
                    customer = state.selectedCustomer,
                    customerName = state.customerName,
                    customerPhone = state.customerPhone,
                    draftItems = state.items,
                    paidAmount = paid,
                    notes = state.notes
                )
                _draft.value = InvoiceDraftUi(lastCreatedInvoiceId = id)
                refreshStats()
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

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice.id)
            refreshStats()
        }
    }

    fun shareInvoice(invoiceId: Long): Intent? {
        val current = _selectedInvoice.value
        if (current?.invoice?.id != invoiceId) return null
        val uri = InvoicePdfWriter.write(
            context = getApplication(),
            invoiceWithItems = current,
            settings = settings.value
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "فاکتور ${current.invoice.invoiceNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
