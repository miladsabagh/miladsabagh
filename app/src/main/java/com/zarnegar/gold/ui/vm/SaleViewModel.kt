package com.zarnegar.gold.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnegar.gold.AppContainer
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.InvoiceTotals
import com.zarnegar.gold.domain.model.PaymentMethod
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.ShopSettings
import com.zarnegar.gold.domain.pricing.GoldPricing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** بخش‌هایی از حالت سبد که کاربر مستقیماً تغییرشان می‌دهد. */
data class CartState(
    val items: List<SaleItem> = emptyList(),
    val customer: Customer? = null,
    val invoiceDiscount: Long = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    /** `null` یعنی «پرداخت کامل»؛ با تغییر مبلغ فاکتور خودکار به‌روز می‌شود. */
    val paidAmount: Long? = null,
    val note: String = "",
)

data class SaleUiState(
    val cart: CartState = CartState(),
    val settings: ShopSettings = ShopSettings(),
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
) {
    val totals: InvoiceTotals
        get() = GoldPricing.priceInvoice(cart.items, settings.pricing(), cart.invoiceDiscount)

    val effectivePaidAmount: Long get() = cart.paidAmount ?: totals.payable

    val status: InvoiceStatus get() = InvoiceStatus.of(totals.payable, effectivePaidAmount)
}

class SaleViewModel(private val container: AppContainer) : ViewModel() {

    private val cart = MutableStateFlow(CartState())

    private val _savedInvoiceId = MutableStateFlow<Long?>(null)
    val savedInvoiceId: StateFlow<Long?> = _savedInvoiceId.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val uiState: StateFlow<SaleUiState> = combine(
        cart,
        container.settingsRepository.settings,
        container.productRepository.products,
        container.customerRepository.customers,
    ) { cartState, settings, products, customers ->
        SaleUiState(cartState, settings, products, customers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SaleUiState())

    fun addProduct(product: Product) {
        cart.update { state ->
            val index = state.items.indexOfFirst { it.productId == product.id && it.discount == 0L }
            val items = if (index >= 0) {
                state.items.toMutableList().also {
                    it[index] = it[index].copy(quantity = it[index].quantity + 1)
                }
            } else {
                state.items + SaleItem.fromProduct(product)
            }
            state.copy(items = items)
        }
        _message.value = "«${product.name}» به فاکتور اضافه شد"
    }

    fun addCustomItem(item: SaleItem) {
        cart.update { it.copy(items = it.items + item) }
    }

    fun updateItem(index: Int, transform: (SaleItem) -> SaleItem) {
        cart.update { state ->
            if (index !in state.items.indices) return@update state
            state.copy(
                items = state.items.toMutableList().also { it[index] = transform(it[index]) },
            )
        }
    }

    fun changeQuantity(index: Int, delta: Int) {
        updateItem(index) { it.copy(quantity = (it.quantity + delta).coerceAtLeast(1)) }
    }

    fun setLineDiscount(index: Int, discount: Long) {
        updateItem(index) { it.copy(discount = discount.coerceAtLeast(0)) }
    }

    fun removeItem(index: Int) {
        cart.update { state ->
            if (index !in state.items.indices) return@update state
            state.copy(items = state.items.filterIndexed { i, _ -> i != index })
        }
    }

    fun setCustomer(customer: Customer?) = cart.update { it.copy(customer = customer) }

    fun setInvoiceDiscount(value: Long) =
        cart.update { it.copy(invoiceDiscount = value.coerceAtLeast(0)) }

    fun setPaymentMethod(value: PaymentMethod) = cart.update {
        val paid = if (value == PaymentMethod.CREDIT) 0L else null
        it.copy(paymentMethod = value, paidAmount = paid)
    }

    fun setPaidAmount(value: Long?) = cart.update { it.copy(paidAmount = value) }

    fun setNote(value: String) = cart.update { it.copy(note = value) }

    fun clear() {
        cart.value = CartState()
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeSavedInvoice() {
        _savedInvoiceId.value = null
    }

    /** ثبت فاکتور و بازگرداندن شناسهٔ آن از طریق [savedInvoiceId]. */
    fun submit() {
        val state = uiState.value
        if (state.cart.items.isEmpty()) {
            _message.value = "ابتدا حداقل یک کالا به فاکتور اضافه کنید"
            return
        }
        viewModelScope.launch {
            val totals = state.totals
            val now = System.currentTimeMillis()
            val number = container.invoiceRepository.nextInvoiceNumber(now)
            val customer = state.cart.customer
            val paid = state.effectivePaidAmount

            val invoice = Invoice(
                number = number,
                createdAt = now,
                customerId = customer?.id,
                customerName = customer?.fullName ?: "مشتری متفرقه",
                customerPhone = customer?.phone.orEmpty(),
                customerNationalCode = customer?.nationalCode.orEmpty(),
                goldRateSnapshot = state.settings.goldRatePerGram18k,
                vatPercentSnapshot = state.settings.vatPercent,
                grossBeforeTax = totals.grossBeforeTax,
                itemDiscountTotal = totals.itemDiscountTotal,
                invoiceDiscount = totals.invoiceDiscount,
                vatTotal = totals.vatTotal,
                roundingAdjustment = totals.roundingAdjustment,
                payable = totals.payable,
                paidAmount = paid,
                paymentMethod = state.cart.paymentMethod,
                status = InvoiceStatus.of(totals.payable, paid),
                note = state.cart.note,
            )

            val lines = totals.lines.map { line ->
                InvoiceLine(
                    productId = line.item.productId.takeIf { it != 0L },
                    title = line.item.title,
                    description = line.item.description,
                    karat = line.item.karat,
                    weightGrams = line.item.weightGrams,
                    quantity = line.quantity,
                    ratePerGram = line.ratePerGram,
                    goldValue = line.unitGoldValue * line.quantity,
                    wage = line.unitWage * line.quantity,
                    profit = line.unitProfit * line.quantity,
                    stoneValue = line.unitStoneValue * line.quantity,
                    discount = line.discount,
                    vat = line.vat,
                    total = line.total,
                )
            }

            val id = container.invoiceRepository.save(invoice, lines)
            clear()
            _savedInvoiceId.value = id
        }
    }
}
