package ir.zarrin.gold.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.gold.GoldApp
import ir.zarrin.gold.data.Customer
import ir.zarrin.gold.data.Invoice
import ir.zarrin.gold.data.InvoiceItem
import ir.zarrin.gold.data.InvoiceWithItems
import ir.zarrin.gold.data.Product
import ir.zarrin.gold.data.StoreSettings
import ir.zarrin.gold.domain.GoldPricing
import ir.zarrin.gold.domain.PriceBreakdown
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** یک قلم از فاکتور در حال تنظیم. */
data class DraftLine(
    val product: Product?,
    val name: String,
    val karat: Int,
    val weightGrams: Double,
    val wagePercent: Double,
    val quantity: Int,
    val breakdown: PriceBreakdown,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GoldApp
    private val db = app.database
    private val settingsStore = app.settingsStore

    val products: StateFlow<List<Product>> = db.productDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val customers: StateFlow<List<Customer>> = db.customerDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val invoices: StateFlow<List<InvoiceWithItems>> = db.invoiceDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val settings: StateFlow<StoreSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, StoreSettings())

    // ---- محصولات ----

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) db.productDao().insert(product)
            else db.productDao().update(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { db.productDao().delete(product) }
    }

    // ---- مشتریان ----

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) db.customerDao().insert(customer)
            else db.customerDao().update(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { db.customerDao().delete(customer) }
    }

    // ---- تنظیمات ----

    fun saveSettings(settings: StoreSettings) {
        viewModelScope.launch { settingsStore.update(settings) }
    }

    fun setGoldPrice(price: Long) {
        viewModelScope.launch { settingsStore.setGoldPrice(price) }
    }

    // ---- فاکتور ----

    /** محاسبه قیمت یک قلم با تنظیمات فعلی. */
    fun priceLine(
        name: String,
        karat: Int,
        weightGrams: Double,
        wagePercent: Double,
        quantity: Int,
        product: Product? = null,
    ): DraftLine {
        val s = settings.value
        val breakdown = GoldPricing.calculate(
            pricePerGram18k = s.goldPricePerGram18k,
            weightGrams = weightGrams,
            karat = karat,
            wagePercent = wagePercent,
            profitPercent = s.profitPercent,
            taxPercent = s.taxPercent,
            quantity = quantity,
        )
        return DraftLine(product, name, karat, weightGrams, wagePercent, quantity, breakdown)
    }

    /**
     * ثبت فاکتور؛ شماره فاکتور به‌صورت خودکار صادر و موجودی محصولات کم می‌شود.
     */
    fun saveInvoice(
        customer: Customer?,
        guestName: String,
        lines: List<DraftLine>,
        discount: Long,
        onSaved: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val s = settingsStore.settings.first()
            val sum = lines.fold(PriceBreakdown.ZERO) { acc, l -> acc + l.breakdown }
            val number = db.invoiceDao().maxNumber() + 1
            val invoiceId = db.invoiceDao().insertInvoice(
                Invoice(
                    number = number,
                    customerId = customer?.id,
                    customerName = customer?.name ?: guestName.ifBlank { "مشتری متفرقه" },
                    customerPhone = customer?.phone.orEmpty(),
                    goldPricePerGram18k = s.goldPricePerGram18k,
                    goldValue = sum.goldValue,
                    wage = sum.wage,
                    profit = sum.profit,
                    tax = sum.tax,
                    discount = discount,
                    total = (sum.total - discount).coerceAtLeast(0),
                )
            )
            db.invoiceDao().insertItems(
                lines.map { l ->
                    InvoiceItem(
                        invoiceId = invoiceId,
                        productId = l.product?.id,
                        name = l.name,
                        karat = l.karat,
                        weightGrams = l.weightGrams,
                        quantity = l.quantity,
                        wagePercent = l.wagePercent,
                        goldValue = l.breakdown.goldValue,
                        wage = l.breakdown.wage,
                        profit = l.breakdown.profit,
                        tax = l.breakdown.tax,
                        lineTotal = l.breakdown.total,
                    )
                }
            )
            lines.forEach { l ->
                l.product?.let { db.productDao().decrementStock(it.id, l.quantity) }
            }
            onSaved(invoiceId)
        }
    }

    fun setInvoicePaid(id: Long, paid: Boolean) {
        viewModelScope.launch { db.invoiceDao().setPaid(id, paid) }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch { db.invoiceDao().deleteById(id) }
    }
}
