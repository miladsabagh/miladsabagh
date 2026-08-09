package ir.zarin.faktor.ui.sale

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.zarin.faktor.R
import ir.zarin.faktor.appContainer
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.repository.CustomerRepository
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.repository.ProductRepository
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.data.settings.SettingsRepository
import ir.zarin.faktor.domain.GoldPricing
import ir.zarin.faktor.domain.InvoiceTotals
import ir.zarin.faktor.domain.PriceBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** وضعیت ویرایش فاکتور در حال ثبت. */
data class SaleDraft(
    val customer: Customer? = null,
    val lines: List<SaleLine> = emptyList(),
    val invoiceDiscountRial: Long = 0,
    val paidAmountRial: Long = 0,
    val fullyPaid: Boolean = true,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
)

data class NewSaleUiState(
    val draft: SaleDraft = SaleDraft(),
    val settings: AppSettings = AppSettings(),
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val saving: Boolean = false,
) {
    val breakdowns: List<PriceBreakdown>
        get() = draft.lines.map { it.breakdown(settings.pricingContext) }

    val totals: InvoiceTotals
        get() = GoldPricing.totals(breakdowns, draft.invoiceDiscountRial)

    val grandTotalRial: Long get() = totals.grandTotalRial

    val paidRial: Long
        get() = if (draft.fullyPaid) grandTotalRial else draft.paidAmountRial.coerceAtMost(grandTotalRial)

    val remainingRial: Long get() = (grandTotalRial - paidRial).coerceAtLeast(0)

    val canSubmit: Boolean get() = draft.lines.isNotEmpty() && !saving
}

class NewSaleViewModel(
    private val invoiceRepository: InvoiceRepository,
    productRepository: ProductRepository,
    customerRepository: CustomerRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val draft = MutableStateFlow(SaleDraft())
    private val saving = MutableStateFlow(false)

    private val _message = MutableStateFlow<Int?>(null)

    /** پیام خطا/راهنما برای نمایش در نوار پایین. */
    val message: StateFlow<Int?> = _message.asStateFlow()

    private val _savedInvoiceId = MutableStateFlow<Long?>(null)
    val savedInvoiceId: StateFlow<Long?> = _savedInvoiceId.asStateFlow()

    /**
     * تنظیمات همیشه فعال است تا ثبت فاکتور به فعال بودن مشترک [uiState] وابسته نباشد.
     */
    private val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    val uiState: StateFlow<NewSaleUiState> = combine(
        draft,
        settings,
        productRepository.observeAll(),
        customerRepository.observeAll(),
        saving,
    ) { currentDraft, settings, products, customers, isSaving ->
        NewSaleUiState(
            draft = currentDraft,
            settings = settings,
            products = products,
            customers = customers,
            saving = isSaving,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NewSaleUiState(),
    )

    fun selectCustomer(customer: Customer?) = draft.update { it.copy(customer = customer) }

    fun addLine(line: SaleLine) = draft.update { it.copy(lines = it.lines + line) }

    fun updateLine(line: SaleLine) = draft.update { current ->
        current.copy(lines = current.lines.map { if (it.key == line.key) line else it })
    }

    fun removeLine(key: String) = draft.update { current ->
        current.copy(lines = current.lines.filterNot { it.key == key })
    }

    fun setInvoiceDiscount(amountRial: Long) =
        draft.update { it.copy(invoiceDiscountRial = amountRial.coerceAtLeast(0)) }

    fun setFullyPaid(value: Boolean) = draft.update { it.copy(fullyPaid = value) }

    fun setPaidAmount(amountRial: Long) =
        draft.update { it.copy(paidAmountRial = amountRial.coerceAtLeast(0)) }

    fun setPaymentMethod(method: PaymentMethod) = draft.update { it.copy(paymentMethod = method) }

    fun setNote(note: String) = draft.update { it.copy(note = note) }

    fun consumeMessage() {
        _message.value = null
    }

    fun submit() {
        val state = NewSaleUiState(draft = draft.value, settings = settings.value)
        if (state.draft.lines.isEmpty()) {
            showMessage(R.string.add_at_least_one_item)
            return
        }
        val needsGoldRate = state.draft.lines.any { it.pricingMode == PricingMode.BY_WEIGHT }
        if (needsGoldRate && state.settings.goldRatePerGramRial <= 0) {
            showMessage(R.string.set_gold_rate_first)
            return
        }
        if (saving.value) return

        saving.value = true
        viewModelScope.launch {
            try {
                val totals = state.totals
                val invoice = Invoice(
                    dateMillis = System.currentTimeMillis(),
                    customerId = state.draft.customer?.id,
                    customerName = state.draft.customer?.name.orEmpty(),
                    customerPhone = state.draft.customer?.phone.orEmpty(),
                    goldRatePerGramRial = state.settings.goldRatePerGramRial,
                    profitPercent = state.settings.profitPercent,
                    vatPercent = state.settings.vatPercent,
                    goldTotalRial = totals.goldTotalRial,
                    wageTotalRial = totals.wageTotalRial,
                    profitTotalRial = totals.profitTotalRial,
                    stoneTotalRial = totals.stoneTotalRial,
                    vatTotalRial = totals.vatTotalRial,
                    itemsDiscountRial = totals.itemsDiscountRial,
                    invoiceDiscountRial = totals.invoiceDiscountRial,
                    grandTotalRial = totals.grandTotalRial,
                    paidAmountRial = state.paidRial,
                    paymentMethod = state.draft.paymentMethod,
                    currencyUnit = state.settings.currencyUnit,
                    note = state.draft.note.trim(),
                )
                val items = state.draft.lines.zip(state.breakdowns) { line, breakdown ->
                    line.toInvoiceItem(breakdown)
                }
                val id = invoiceRepository.createInvoice(invoice, items, state.settings.invoicePrefix)
                _savedInvoiceId.value = id
            } finally {
                saving.value = false
            }
        }
    }

    private fun showMessage(@StringRes resId: Int) {
        _message.value = resId
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this.appContainer()
                NewSaleViewModel(
                    invoiceRepository = container.invoiceRepository,
                    productRepository = container.productRepository,
                    customerRepository = container.customerRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
