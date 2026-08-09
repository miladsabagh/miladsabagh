package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.core.JalaliDate
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.Customer
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.data.local.InvoiceItem
import ir.zarrin.goldshop.data.local.Product
import ir.zarrin.goldshop.data.local.toLineInput
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.data.settings.ShopSettings
import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.InvoiceTotals
import ir.zarrin.goldshop.domain.LineTotals
import ir.zarrin.goldshop.domain.model.InvoiceType
import ir.zarrin.goldshop.domain.model.PaymentMethod
import ir.zarrin.goldshop.domain.model.WageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InvoiceFilter(val label: String) {
    ALL("همه"),
    TODAY("امروز"),
    MONTH("این ماه"),
    UNPAID("تسویه‌نشده")
}

data class InvoiceListState(
    val invoices: List<Invoice> = emptyList(),
    val settings: ShopSettings = ShopSettings(),
    val query: String = "",
    val filter: InvoiceFilter = InvoiceFilter.ALL,
    val totalPayable: Long = 0L
)

class InvoiceListViewModel(
    private val repository: ShopRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(InvoiceFilter.ALL)

    val state: StateFlow<InvoiceListState> = combine(
        repository.observeInvoices(),
        settingsRepository.settings,
        query,
        filter
    ) { invoices, settings, text, selectedFilter ->
        val now = System.currentTimeMillis()
        val startOfToday = PersianCalendar.startOfDay(now)
        val startOfMonth = PersianCalendar.startOfJalaliMonth(now)
        val normalized = text.trim()
        val filtered = invoices
            .filter { invoice ->
                when (selectedFilter) {
                    InvoiceFilter.ALL -> true
                    InvoiceFilter.TODAY -> invoice.dateMillis >= startOfToday
                    InvoiceFilter.MONTH -> invoice.dateMillis >= startOfMonth
                    InvoiceFilter.UNPAID -> invoice.payable - invoice.paid > 0L
                }
            }
            .filter { invoice ->
                normalized.isEmpty() ||
                    invoice.customerName.contains(normalized, ignoreCase = true) ||
                    invoice.number.contains(normalized) ||
                    invoice.customerPhone.contains(normalized)
            }
        InvoiceListState(
            invoices = filtered,
            settings = settings,
            query = text,
            filter = selectedFilter,
            totalPayable = filtered.filter { it.type == InvoiceType.SALE }.sumOf { it.payable }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceListState())

    fun onQueryChange(value: String) = query.update { value }

    fun onFilterChange(value: InvoiceFilter) = filter.update { value }

    fun delete(invoiceId: Long) {
        viewModelScope.launch { repository.deleteInvoice(invoiceId) }
    }
}

data class InvoiceEditorState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val invoice: Invoice = Invoice(),
    val items: List<InvoiceItem> = emptyList(),
    val lineTotals: List<LineTotals> = emptyList(),
    val settings: ShopSettings = ShopSettings(),
    val customers: List<Customer> = emptyList(),
    val products: List<Product> = emptyList(),
    val totals: InvoiceTotals = InvoiceTotals(),
    val savedInvoiceId: Long? = null,
    val message: String? = null
) {
    val jalaliDate: JalaliDate get() = PersianCalendar.fromEpochMillis(invoice.dateMillis)
}

class InvoiceEditorViewModel(
    private val repository: ShopRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.get<String>("invoiceId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(InvoiceEditorState())
    val state: StateFlow<InvoiceEditorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val existing = if (invoiceId != 0L) repository.findInvoice(invoiceId) else null
            val invoice = existing?.invoice ?: Invoice(
                number = repository.nextInvoiceNumber(),
                baseGoldRate = settings.goldRate18,
                taxPercent = settings.taxPercent,
                customerName = ""
            )
            _state.update {
                it.copy(
                    loading = false,
                    isNew = existing == null,
                    invoice = invoice,
                    items = existing?.orderedItems.orEmpty(),
                    settings = settings
                )
            }
            recompute()
        }
        viewModelScope.launch {
            repository.observeCustomers().collect { customers ->
                _state.update { it.copy(customers = customers) }
            }
        }
        viewModelScope.launch {
            repository.observeProducts().collect { products ->
                _state.update { it.copy(products = products) }
            }
        }
    }

    private fun recompute() {
        _state.update { current ->
            val pairs = current.items.map { item ->
                val input = item.toLineInput()
                input to GoldCalculator.calculateLine(input)
            }
            current.copy(
                lineTotals = pairs.map { it.second },
                totals = GoldCalculator.summarize(
                    pairs,
                    current.invoice.discount,
                    current.invoice.paid
                )
            )
        }
    }

    private fun editInvoice(transform: (Invoice) -> Invoice) {
        _state.update { it.copy(invoice = transform(it.invoice)) }
        recompute()
    }

    fun setType(type: InvoiceType) = editInvoice { it.copy(type = type) }

    fun setDate(date: JalaliDate) = editInvoice { it.copy(dateMillis = date.toEpochMillis()) }

    fun setPaymentMethod(method: PaymentMethod) = editInvoice { it.copy(paymentMethod = method) }

    fun setDiscount(value: Long) = editInvoice { it.copy(discount = value.coerceAtLeast(0L)) }

    fun setPaid(value: Long) = editInvoice { it.copy(paid = value.coerceAtLeast(0L)) }

    fun setNote(value: String) = editInvoice { it.copy(note = value) }

    fun setCustomerName(value: String) = editInvoice { it.copy(customerName = value, customerId = null) }

    fun setCustomerPhone(value: String) = editInvoice { it.copy(customerPhone = value) }

    fun selectCustomer(customer: Customer) = editInvoice {
        it.copy(
            customerId = customer.id,
            customerName = customer.name,
            customerPhone = customer.phone,
            customerNationalId = customer.nationalId,
            customerAddress = customer.address
        )
    }

    /** Re-prices every weight based line when the daily gold rate changes. */
    fun setBaseGoldRate(rate: Long) {
        _state.update { current ->
            current.copy(
                invoice = current.invoice.copy(baseGoldRate = rate),
                items = current.items.map { item ->
                    if (item.unitPriceOverride != null) {
                        item
                    } else {
                        item.copy(goldRatePerGram = GoldCalculator.rateForKarat(rate, item.karat))
                    }
                }
            )
        }
        recompute()
    }

    fun setTaxPercent(percent: Double) {
        _state.update { current ->
            current.copy(
                invoice = current.invoice.copy(taxPercent = percent),
                items = current.items.map { it.copy(taxPercent = percent) }
            )
        }
        recompute()
    }

    fun addItem(item: InvoiceItem) {
        _state.update { it.copy(items = it.items + item) }
        recompute()
    }

    fun updateItem(index: Int, item: InvoiceItem) {
        _state.update { current ->
            current.copy(
                items = current.items.toMutableList().also { list ->
                    if (index in list.indices) list[index] = item
                }
            )
        }
        recompute()
    }

    fun removeItem(index: Int) {
        _state.update { current ->
            current.copy(
                items = current.items.toMutableList().also { list ->
                    if (index in list.indices) list.removeAt(index)
                }
            )
        }
        recompute()
    }

    fun newItemTemplate(): InvoiceItem {
        val current = _state.value
        return InvoiceItem(
            goldRatePerGram = GoldCalculator.rateForKarat(current.invoice.baseGoldRate, 750),
            wageMode = WageMode.PERCENT,
            wageValue = current.settings.defaultWagePercent,
            profitPercent = current.settings.defaultProfitPercent,
            taxPercent = current.invoice.taxPercent
        )
    }

    fun itemFromProduct(product: Product): InvoiceItem {
        val current = _state.value
        return InvoiceItem(
            productId = product.id,
            title = product.name,
            kind = product.kind,
            karat = product.karat,
            weightGrams = product.weightGrams,
            quantity = 1,
            goldRatePerGram = GoldCalculator.rateForKarat(current.invoice.baseGoldRate, product.karat),
            wageMode = product.wageMode,
            wageValue = product.wageValue,
            profitPercent = product.profitPercent,
            stonePrice = product.stonePrice,
            taxBasis = product.kind.defaultTaxBasis,
            taxPercent = current.invoice.taxPercent,
            unitPriceOverride = product.unitPriceOverride
        )
    }

    fun addCustomer(name: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.saveCustomer(Customer(name = name.trim(), phone = phone.trim()))
            repository.findCustomer(id)?.let { selectCustomer(it) }
        }
    }

    fun save() {
        val current = _state.value
        if (current.items.isEmpty()) {
            _state.update { it.copy(message = "برای صدور فاکتور حداقل یک کالا اضافه کنید.") }
            return
        }
        viewModelScope.launch {
            val invoice = current.invoice.copy(
                customerName = current.invoice.customerName.ifBlank { "مشتری متفرقه" }
            )
            val id = repository.saveInvoice(invoice, current.items)
            _state.update { it.copy(savedInvoiceId = id) }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}

data class InvoiceDetailState(
    val loading: Boolean = true,
    val invoice: Invoice? = null,
    val items: List<InvoiceItem> = emptyList(),
    val lineTotals: List<LineTotals> = emptyList(),
    val totals: InvoiceTotals = InvoiceTotals(),
    val settings: ShopSettings = ShopSettings()
)

class InvoiceDetailViewModel(
    private val repository: ShopRepository,
    settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.get<String>("invoiceId")?.toLongOrNull() ?: 0L

    val state: StateFlow<InvoiceDetailState> = combine(
        repository.observeInvoice(invoiceId),
        settingsRepository.settings
    ) { invoiceWithItems, settings ->
        if (invoiceWithItems == null) {
            InvoiceDetailState(loading = false, settings = settings)
        } else {
            val items = invoiceWithItems.orderedItems
            val pairs = items.map { item ->
                val input = item.toLineInput()
                input to GoldCalculator.calculateLine(input)
            }
            InvoiceDetailState(
                loading = false,
                invoice = invoiceWithItems.invoice,
                items = items,
                lineTotals = pairs.map { it.second },
                totals = GoldCalculator.summarize(
                    pairs,
                    invoiceWithItems.invoice.discount,
                    invoiceWithItems.invoice.paid
                ),
                settings = settings
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceDetailState())

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteInvoice(invoiceId)
            onDeleted()
        }
    }
}
