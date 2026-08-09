package com.zarin.goldshop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zarin.goldshop.data.AppDatabase
import com.zarin.goldshop.data.AppSettings
import com.zarin.goldshop.data.Customer
import com.zarin.goldshop.data.InvoiceEntity
import com.zarin.goldshop.data.InvoiceItemEntity
import com.zarin.goldshop.data.InvoiceWithItems
import com.zarin.goldshop.data.Product
import com.zarin.goldshop.data.Repository
import com.zarin.goldshop.util.GoldCalc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DraftItem(
    val productId: Long? = null,
    val name: String,
    val weight: Double,
    val karat: Int,
    val wagePercent: Double,
    val stonePrice: Long,
    val quantity: Int,
)

data class InvoiceTotals(
    val goldValueTotal: Long = 0,
    val wageTotal: Long = 0,
    val stoneTotal: Long = 0,
    val profitTotal: Long = 0,
    val taxTotal: Long = 0,
    val discount: Long = 0,
    val grandTotal: Long = 0,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(AppDatabase.get(app))

    val products: StateFlow<List<Product>> =
        repo.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers: StateFlow<List<Customer>> =
        repo.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices: StateFlow<List<InvoiceWithItems>> =
        repo.invoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings: StateFlow<AppSettings> =
        repo.settings.map { it ?: AppSettings() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun observeInvoice(id: Long) = repo.observeInvoice(id)

    // ----- Draft invoice state -----
    private val _draftItems = MutableStateFlow<List<DraftItem>>(emptyList())
    val draftItems: StateFlow<List<DraftItem>> = _draftItems.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _discount = MutableStateFlow(0L)
    val discount: StateFlow<Long> = _discount.asStateFlow()

    fun setCustomerName(v: String) { _customerName.value = v }
    fun setCustomerPhone(v: String) { _customerPhone.value = v }
    fun setDiscount(v: Long) { _discount.value = v }

    fun addDraftItem(item: DraftItem) { _draftItems.value = _draftItems.value + item }
    fun removeDraftItem(index: Int) {
        _draftItems.value = _draftItems.value.toMutableList().also { if (index in it.indices) it.removeAt(index) }
    }
    fun clearDraft() {
        _draftItems.value = emptyList()
        _customerName.value = ""
        _customerPhone.value = ""
        _discount.value = 0
    }

    fun computeTotals(items: List<DraftItem>, s: AppSettings, discount: Long): InvoiceTotals {
        var gold = 0L; var wage = 0L; var stone = 0L; var profit = 0L; var tax = 0L; var grand = 0L
        items.forEach { d ->
            val r = GoldCalc.computeLine(
                GoldCalc.LineInput(d.weight, d.karat, d.wagePercent, d.stonePrice, d.quantity),
                s.goldPricePerGram, s.profitPercent, s.taxPercent
            )
            val q = if (d.quantity <= 0) 1 else d.quantity
            gold += r.goldValue * q
            wage += r.wage * q
            stone += r.stone * q
            profit += r.profit * q
            tax += r.tax * q
            grand += r.lineTotal
        }
        val total = (grand - discount).coerceAtLeast(0)
        return InvoiceTotals(gold, wage, stone, profit, tax, discount, total)
    }

    // ----- Product actions -----
    fun saveProduct(product: Product) = viewModelScope.launch { repo.upsertProduct(product) }
    fun deleteProduct(product: Product) = viewModelScope.launch { repo.deleteProduct(product) }

    // ----- Customer actions -----
    fun saveCustomer(customer: Customer) = viewModelScope.launch { repo.upsertCustomer(customer) }
    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repo.deleteCustomer(customer) }

    // ----- Settings -----
    fun saveSettings(s: AppSettings) = viewModelScope.launch { repo.saveSettings(s) }

    // ----- Invoice -----
    fun saveInvoice(onDone: (Long) -> Unit) = viewModelScope.launch {
        val s = repo.getSettings()
        val items = _draftItems.value
        if (items.isEmpty()) return@launch
        val totals = computeTotals(items, s, _discount.value)
        val seq = s.lastInvoiceSeq + 1
        val number = "${seq.toString().padStart(4, '0')}"
        val date = System.currentTimeMillis()

        val invoice = InvoiceEntity(
            invoiceNumber = number,
            customerName = _customerName.value.ifBlank { "مشتری نقدی" },
            customerPhone = _customerPhone.value,
            dateMillis = date,
            goldPricePerGram = s.goldPricePerGram,
            goldValueTotal = totals.goldValueTotal,
            wageTotal = totals.wageTotal,
            stoneTotal = totals.stoneTotal,
            profitTotal = totals.profitTotal,
            taxTotal = totals.taxTotal,
            discount = totals.discount,
            grandTotal = totals.grandTotal,
        )

        val itemEntities = items.map { d ->
            val r = GoldCalc.computeLine(
                GoldCalc.LineInput(d.weight, d.karat, d.wagePercent, d.stonePrice, d.quantity),
                s.goldPricePerGram, s.profitPercent, s.taxPercent
            )
            InvoiceItemEntity(
                invoiceId = 0,
                name = d.name,
                weight = d.weight,
                karat = d.karat,
                wagePercent = d.wagePercent,
                stonePrice = d.stonePrice,
                quantity = if (d.quantity <= 0) 1 else d.quantity,
                goldValue = r.goldValue,
                wage = r.wage,
                profit = r.profit,
                tax = r.tax,
                unitTotal = r.unitTotal,
                lineTotal = r.lineTotal,
            )
        }

        val stockUpdates = items.mapNotNull { d -> d.productId?.let { it to (if (d.quantity <= 0) 1 else d.quantity) } }

        val id = repo.saveInvoice(invoice, itemEntities, stockUpdates, seq)
        clearDraft()
        onDone(id)
    }

    fun deleteInvoice(id: Long) = viewModelScope.launch { repo.deleteInvoice(id) }
}
