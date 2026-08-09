package com.zarfam.goldshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zarfam.goldshop.GoldShopApp
import com.zarfam.goldshop.data.db.Customer
import com.zarfam.goldshop.data.db.Invoice
import com.zarfam.goldshop.data.db.InvoiceItem
import com.zarfam.goldshop.data.db.Product
import com.zarfam.goldshop.data.db.ShopSettings
import com.zarfam.goldshop.domain.InvoiceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private fun startOfTodayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

class SettingsViewModel(private val app: GoldShopApp) : ViewModel() {
    private val dao = app.database.settingsDao()

    val settings = dao.observe()
        .map { it ?: ShopSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    fun save(settings: ShopSettings) {
        viewModelScope.launch { dao.upsert(settings.copy(id = 1)) }
    }

    fun saveGoldPrice(price: Long) {
        viewModelScope.launch {
            val current = dao.get() ?: ShopSettings()
            dao.upsert(current.copy(id = 1, goldPricePerGram18 = price))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(this[APPLICATION_KEY] as GoldShopApp) }
        }
    }
}

class ProductsViewModel(private val app: GoldShopApp) : ViewModel() {
    private val dao = app.database.productDao()

    val products = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsert(product: Product) {
        viewModelScope.launch { dao.upsert(product) }
    }

    fun delete(product: Product) {
        viewModelScope.launch { dao.delete(product) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ProductsViewModel(this[APPLICATION_KEY] as GoldShopApp) }
        }
    }
}

class CustomersViewModel(private val app: GoldShopApp) : ViewModel() {
    private val dao = app.database.customerDao()

    val customers = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsert(customer: Customer) {
        viewModelScope.launch { dao.upsert(customer) }
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { dao.delete(customer) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CustomersViewModel(this[APPLICATION_KEY] as GoldShopApp) }
        }
    }
}

class InvoicesViewModel(private val app: GoldShopApp) : ViewModel() {
    private val dao = app.database.invoiceDao()

    val invoices = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoiceCount = dao.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productCount = app.database.productDao().observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todaySales = dao.observeSalesSince(startOfTodayMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSales = dao.observeSalesSince(0)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun observeInvoice(id: Long) = dao.observeWithItems(id)

    fun delete(invoiceId: Long) {
        viewModelScope.launch { dao.deleteInvoice(invoiceId) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { InvoicesViewModel(this[APPLICATION_KEY] as GoldShopApp) }
        }
    }
}

/** A line of the invoice draft before it is persisted. */
data class DraftItem(
    val productId: Long? = null,
    val name: String,
    val weightGrams: Double,
    val karat: Int,
    val wagePercent: Double,
    val profitPercent: Double,
    val quantity: Int = 1,
)

class NewInvoiceViewModel(private val app: GoldShopApp) : ViewModel() {
    private val invoiceDao = app.database.invoiceDao()
    private val settingsDao = app.database.settingsDao()

    val products = app.database.productDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = app.database.customerDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = settingsDao.observe()
        .map { it ?: ShopSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    fun computeItems(
        items: List<DraftItem>,
        goldPrice: Long,
        taxPercent: Double,
    ): List<Pair<DraftItem, com.zarfam.goldshop.domain.LineAmounts>> = items.map { item ->
        item to InvoiceCalculator.calculateLine(
            pricePerGram18 = goldPrice,
            weightGrams = item.weightGrams,
            karat = item.karat,
            wagePercent = item.wagePercent,
            profitPercent = item.profitPercent,
            taxPercent = taxPercent,
            quantity = item.quantity,
        )
    }

    fun saveInvoice(
        customer: Customer?,
        guestName: String,
        guestPhone: String,
        items: List<DraftItem>,
        goldPrice: Long,
        taxPercent: Double,
        discount: Long,
        note: String,
        onSaved: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val computed = computeItems(items, goldPrice, taxPercent)
            val itemsTotal = computed.sumOf { it.second.lineTotal }
            val grandTotal = InvoiceCalculator.grandTotal(itemsTotal, discount)
            val number = invoiceDao.nextInvoiceNumber()
            val invoice = Invoice(
                invoiceNumber = number,
                customerId = customer?.id,
                customerName = customer?.name ?: guestName.ifBlank { "مشتری" },
                customerPhone = customer?.phone ?: guestPhone,
                goldPricePerGram18 = goldPrice,
                taxPercent = taxPercent,
                itemsTotal = itemsTotal,
                discount = discount,
                grandTotal = grandTotal,
                note = note,
            )
            val entities = computed.map { (draft, amounts) ->
                InvoiceItem(
                    productId = draft.productId,
                    name = draft.name,
                    weightGrams = draft.weightGrams,
                    karat = draft.karat,
                    wagePercent = draft.wagePercent,
                    profitPercent = draft.profitPercent,
                    quantity = draft.quantity,
                    rawGoldValue = amounts.rawGoldValue,
                    wageAmount = amounts.wageAmount,
                    profitAmount = amounts.profitAmount,
                    taxAmount = amounts.taxAmount,
                    lineTotal = amounts.lineTotal,
                )
            }
            val id = invoiceDao.createInvoice(invoice, entities)
            onSaved(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NewInvoiceViewModel(this[APPLICATION_KEY] as GoldShopApp) }
        }
    }
}
