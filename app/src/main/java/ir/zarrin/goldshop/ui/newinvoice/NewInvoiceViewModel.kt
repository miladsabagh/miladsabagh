package ir.zarrin.goldshop.ui.newinvoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.db.CustomerEntity
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.data.repo.CustomerRepository
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.repo.NewInvoiceRequest
import ir.zarrin.goldshop.data.repo.ProductRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.DraftItem
import ir.zarrin.goldshop.domain.InvoiceTotals
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.domain.PriceBreakdown
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.summarize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewInvoiceForm(
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<DraftItem> = emptyList(),
    val invoiceDiscount: Long = 0L,
    val rateOverride: Long? = null,
    val paidOverride: Long? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = ""
)

data class NewInvoiceState(
    val form: NewInvoiceForm = NewInvoiceForm(),
    val settings: AppSettings = AppSettings(),
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val savedInvoiceId: Long? = null,
    val isSaving: Boolean = false,
    val message: String? = null
) {
    val goldRate: Long get() = form.rateOverride ?: settings.goldRate18

    val taxPercent: Double get() = settings.taxPercent

    val breakdowns: List<Pair<DraftItem, PriceBreakdown>>
        get() = form.items.map { it to it.breakdown(goldRate, taxPercent) }

    val totalWeight: Double
        get() = form.items.sumOf {
            if (it.pricingMode == PricingMode.BY_WEIGHT) it.weightGrams * it.quantity else 0.0
        }

    val totals: InvoiceTotals
        get() = breakdowns.map { it.second }.summarize(form.invoiceDiscount, totalWeight)

    val paidAmount: Long get() = form.paidOverride ?: totals.grandTotal

    val remaining: Long get() = (totals.grandTotal - paidAmount).coerceAtLeast(0L)

    val canSave: Boolean get() = form.items.isNotEmpty() && !isSaving
}

class NewInvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val form = MutableStateFlow(NewInvoiceForm())
    private val saving = MutableStateFlow(false)
    private val savedId = MutableStateFlow<Long?>(null)
    private val message = MutableStateFlow<String?>(null)

    private var nextKey = 1L

    val state: StateFlow<NewInvoiceState> = combine(
        form,
        settingsRepository.settings,
        productRepository.observeAll(),
        customerRepository.observeAll(),
        combine(saving, savedId, message) { isSaving, id, msg -> Triple(isSaving, id, msg) }
    ) { formValue, settings, products, customers, status ->
        NewInvoiceState(
            form = formValue,
            settings = settings,
            products = products,
            customers = customers,
            isSaving = status.first,
            savedInvoiceId = status.second,
            message = status.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewInvoiceState())

    fun addProduct(product: ProductEntity) {
        val existing = form.value.items.firstOrNull { it.productId == product.id }
        if (existing != null) {
            changeQuantity(existing.key, existing.quantity + 1)
            return
        }
        form.value = form.value.copy(
            items = form.value.items + DraftItem.fromProduct(product, nextKey++)
        )
    }

    fun addCustomItem(item: DraftItem) {
        form.value = form.value.copy(items = form.value.items + item.copy(key = nextKey++))
    }

    fun updateItem(key: Long, transform: (DraftItem) -> DraftItem) {
        form.value = form.value.copy(
            items = form.value.items.map { if (it.key == key) transform(it) else it }
        )
    }

    fun changeQuantity(key: Long, quantity: Int) {
        updateItem(key) { item ->
            val max = if (item.availableStock > 0) item.availableStock else Int.MAX_VALUE
            item.copy(quantity = quantity.coerceIn(1, max))
        }
    }

    fun removeItem(key: Long) {
        form.value = form.value.copy(items = form.value.items.filterNot { it.key == key })
    }

    fun selectCustomer(customer: CustomerEntity?) {
        form.value = form.value.copy(
            customerId = customer?.id,
            customerName = customer?.name.orEmpty(),
            customerPhone = customer?.phone.orEmpty()
        )
    }

    fun setWalkInCustomer(name: String, phone: String) {
        form.value = form.value.copy(customerId = null, customerName = name, customerPhone = phone)
    }

    fun createCustomer(name: String, phone: String) {
        viewModelScope.launch {
            val entity = CustomerEntity(name = name, phone = phone)
            val id = customerRepository.save(entity)
            form.value = form.value.copy(customerId = id, customerName = name, customerPhone = phone)
        }
    }

    fun setInvoiceDiscount(value: Long) {
        form.value = form.value.copy(invoiceDiscount = value.coerceAtLeast(0L))
    }

    fun setGoldRate(value: Long?) {
        form.value = form.value.copy(rateOverride = value)
    }

    fun setPaidAmount(value: Long?) {
        form.value = form.value.copy(paidOverride = value)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        form.value = form.value.copy(
            paymentMethod = method,
            paidOverride = if (method == PaymentMethod.CREDIT) 0L else form.value.paidOverride
        )
    }

    fun setNote(value: String) {
        form.value = form.value.copy(note = value)
    }

    fun consumeMessage() {
        message.value = null
    }

    fun save() {
        val snapshot = state.value
        if (!snapshot.canSave) {
            message.value = "حداقل یک کالا به فاکتور اضافه کنید"
            return
        }
        saving.value = true
        viewModelScope.launch {
            runCatching {
                invoiceRepository.create(
                    NewInvoiceRequest(
                        customerId = snapshot.form.customerId,
                        customerName = snapshot.form.customerName,
                        customerPhone = snapshot.form.customerPhone,
                        items = snapshot.form.items,
                        goldRate18 = snapshot.goldRate,
                        taxPercent = snapshot.taxPercent,
                        invoiceDiscount = snapshot.form.invoiceDiscount,
                        paidAmount = snapshot.paidAmount,
                        paymentMethod = snapshot.form.paymentMethod,
                        note = snapshot.form.note
                    )
                )
            }.onSuccess { id ->
                saving.value = false
                savedId.value = id
            }.onFailure {
                saving.value = false
                message.value = "ثبت فاکتور ناموفق بود: ${it.message.orEmpty()}"
            }
        }
    }
}
