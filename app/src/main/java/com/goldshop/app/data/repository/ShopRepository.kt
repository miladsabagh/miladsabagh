package com.goldshop.app.data.repository

import com.goldshop.app.data.local.AppDatabase
import com.goldshop.app.data.model.CartItem
import com.goldshop.app.data.model.Customer
import com.goldshop.app.data.model.DashboardStats
import com.goldshop.app.data.model.Invoice
import com.goldshop.app.data.model.InvoiceItem
import com.goldshop.app.data.model.InvoiceWithItems
import com.goldshop.app.data.model.Product
import com.goldshop.app.data.model.ShopSettings
import com.goldshop.app.util.PriceCalculator
import com.goldshop.app.util.startOfMonthMillis
import com.goldshop.app.util.startOfTodayMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ShopRepository(private val db: AppDatabase) {

    private val products = db.productDao()
    private val customers = db.customerDao()
    private val invoices = db.invoiceDao()
    private val settings = db.settingsDao()

    fun observeProducts(): Flow<List<Product>> = products.observeAll()
    fun observeCustomers(): Flow<List<Customer>> = customers.observeAll()
    fun observeInvoices(): Flow<List<Invoice>> = invoices.observeAll()
    fun observeSettings(): Flow<ShopSettings> =
        settings.observe().map { it ?: ShopSettings() }

    fun observeDashboard(): Flow<DashboardStats> = combine(
        products.observeCount(),
        customers.observeCount(),
        invoices.observeCount(),
        invoices.observeSalesSince(startOfTodayMillis()),
        invoices.observeSalesSince(startOfMonthMillis())
    ) { productCount, customerCount, invoiceCount, today, month ->
        DashboardStats(
            productCount = productCount,
            customerCount = customerCount,
            invoiceCount = invoiceCount,
            todaySales = today,
            monthSales = month
        )
    }

    suspend fun saveProduct(product: Product): Long = products.upsert(product)
    suspend fun deleteProduct(product: Product) = products.delete(product)
    suspend fun getProduct(id: Long) = products.getById(id)

    suspend fun saveCustomer(customer: Customer): Long = customers.upsert(customer)
    suspend fun deleteCustomer(customer: Customer) = customers.delete(customer)

    suspend fun saveSettings(shopSettings: ShopSettings) = settings.upsert(shopSettings)

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? {
        val invoice = invoices.getById(id) ?: return null
        return InvoiceWithItems(invoice, invoices.getItems(id))
    }

    suspend fun createInvoice(
        cart: List<CartItem>,
        customer: Customer?,
        customerNameOverride: String?,
        customerPhoneOverride: String?,
        discount: Long,
        notes: String,
        paidAmount: Long
    ): Long {
        require(cart.isNotEmpty()) { "سبد خرید خالی است" }
        val shop = settings.get() ?: ShopSettings()
        val goldPrice = shop.goldPricePerGram18
        val subtotal = PriceCalculator.cartSubtotal(cart, goldPrice)
        val tax = PriceCalculator.taxAmount(subtotal, discount, shop.taxPercent)
        val total = PriceCalculator.grandTotal(subtotal, discount, tax)
        val count = invoices.count() + 1
        val number = "${shop.invoicePrefix}-${count.toString().padStart(5, '0')}"

        val invoice = Invoice(
            invoiceNumber = number,
            customerId = customer?.id,
            customerName = customerNameOverride?.ifBlank { null }
                ?: customer?.fullName
                ?: "مشتری حضوری",
            customerPhone = customerPhoneOverride?.ifBlank { null }
                ?: customer?.phone
                ?: "",
            goldPricePerGram18 = goldPrice,
            subtotal = subtotal,
            discount = discount,
            taxAmount = tax,
            total = total,
            paidAmount = if (paidAmount > 0) paidAmount else total,
            notes = notes
        )

        val items = cart.map { item ->
            val labor = item.customLaborPercent ?: item.product.laborPercent
            val profit = item.customProfitPercent ?: item.product.profitPercent
            val unit = PriceCalculator.unitPrice(
                weightGrams = item.product.weightGrams,
                purity = item.product.purity,
                laborPercent = labor,
                profitPercent = profit,
                goldPricePerGram18 = goldPrice
            )
            InvoiceItem(
                invoiceId = 0,
                productId = item.product.id.takeIf { it > 0 },
                productName = item.product.name,
                categoryLabel = item.product.category.labelFa,
                weightGrams = item.product.weightGrams,
                purity = item.product.purity,
                laborPercent = labor,
                profitPercent = profit,
                unitPrice = unit,
                lineTotal = unit * item.quantity,
                quantity = item.quantity
            )
        }

        val invoiceId = invoices.createInvoice(invoice, items)
        cart.forEach { item ->
            if (item.product.id > 0) {
                products.decreaseStock(item.product.id, item.quantity)
            }
        }
        return invoiceId
    }
}
