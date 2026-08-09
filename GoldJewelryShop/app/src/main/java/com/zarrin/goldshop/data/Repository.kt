package com.zarrin.goldshop.data

import com.zarrin.goldshop.domain.GoldPricing
import com.zarrin.goldshop.domain.startOfMonthMillis
import com.zarrin.goldshop.domain.startOfTodayMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DraftInvoiceItem(
    val product: ProductEntity,
    val quantity: Int = 1
)

class ShopRepository(private val db: AppDatabase) {
    private val products = db.productDao()
    private val customers = db.customerDao()
    private val settings = db.settingsDao()
    private val invoices = db.invoiceDao()

    fun observeProducts(): Flow<List<ProductEntity>> = products.observeAll()
    fun observeCustomers(): Flow<List<CustomerEntity>> = customers.observeAll()
    fun observeInvoices(): Flow<List<InvoiceEntity>> = invoices.observeAll()
    fun observeSettings(): Flow<ShopSettingsEntity> =
        settings.observe().map { it ?: ShopSettingsEntity() }

    suspend fun ensureSeeded() {
        if (settings.get() == null) {
            settings.upsert(ShopSettingsEntity())
        }
        if (products.count() == 0) {
            sampleProducts().forEach { products.insert(it) }
        }
        if (customers.observeAll().first().isEmpty()) {
            customers.insert(
                CustomerEntity(
                    fullName = "علی رضایی",
                    phone = "09121234567",
                    address = "تهران، ولیعصر"
                )
            )
        }
    }

    suspend fun saveProduct(product: ProductEntity) {
        if (product.id == 0L) products.insert(product) else products.update(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = products.delete(product)

    suspend fun saveCustomer(customer: CustomerEntity) {
        if (customer.id == 0L) customers.insert(customer) else customers.update(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = customers.delete(customer)

    suspend fun saveSettings(shopSettings: ShopSettingsEntity) =
        settings.upsert(shopSettings.copy(id = 1))

    suspend fun getSettings(): ShopSettingsEntity =
        settings.get() ?: ShopSettingsEntity()

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? {
        val invoice = invoices.getById(id) ?: return null
        return InvoiceWithItems(invoice, invoices.getItems(id))
    }

    suspend fun deleteInvoice(id: Long) = invoices.deleteInvoice(id)

    suspend fun dashboardStats(): DashboardStats {
        return DashboardStats(
            todaySales = invoices.sumFrom(startOfTodayMillis()),
            monthSales = invoices.sumFrom(startOfMonthMillis()),
            invoiceCount = invoices.count(),
            productCount = products.count(),
            lowStockCount = products.lowStockCount()
        )
    }

    suspend fun createInvoice(
        customer: CustomerEntity?,
        customerName: String,
        customerPhone: String,
        draftItems: List<DraftInvoiceItem>,
        paidAmount: Long,
        notes: String
    ): Long {
        require(draftItems.isNotEmpty()) { "فاکتور باید حداقل یک قلم داشته باشد" }
        val shop = getSettings()
        val lineItems = mutableListOf<InvoiceItemEntity>()
        val lineTotals = mutableListOf<Long>()

        draftItems.forEach { draft ->
            repeat(draft.quantity) {
                val line = GoldPricing.lineTotal(
                    weightGrams = draft.product.weightGrams,
                    karat = draft.product.purityKarat,
                    makingFeePercent = draft.product.makingFeePercent,
                    goldPrice18PerGram = shop.goldPrice18PerGram
                )
                lineTotals += line
                lineItems += InvoiceItemEntity(
                    invoiceId = 0,
                    productId = draft.product.id,
                    productName = draft.product.name,
                    productCode = draft.product.code,
                    weightGrams = draft.product.weightGrams,
                    purityKarat = draft.product.purityKarat,
                    makingFeePercent = draft.product.makingFeePercent,
                    unitGoldPrice = shop.goldPrice18PerGram,
                    lineTotal = line
                )
            }
        }

        val totals = GoldPricing.invoiceTotals(
            itemLineTotals = lineTotals,
            profitPercent = shop.profitPercent,
            vatPercent = shop.vatPercent
        )

        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val invoice = InvoiceEntity(
            invoiceNumber = "${shop.invoicePrefix}-$stamp",
            customerId = customer?.id,
            customerName = customerName.ifBlank { customer?.fullName ?: "مشتری حضوری" },
            customerPhone = customerPhone.ifBlank { customer?.phone.orEmpty() },
            goldPrice18PerGram = shop.goldPrice18PerGram,
            profitPercent = shop.profitPercent,
            vatPercent = shop.vatPercent,
            subtotal = totals.subtotal,
            profitAmount = totals.profitAmount,
            vatAmount = totals.vatAmount,
            totalAmount = totals.totalAmount,
            paidAmount = paidAmount.coerceAtMost(totals.totalAmount),
            notes = notes
        )

        val invoiceId = invoices.createInvoice(invoice, lineItems)
        draftItems.forEach { draft ->
            if (draft.product.id != 0L) {
                products.decreaseStock(draft.product.id, draft.quantity)
            }
        }
        return invoiceId
    }

    private fun sampleProducts(): List<ProductEntity> = listOf(
        ProductEntity(
            name = "انگشتر کلاسیک عیار ۱۸",
            category = ProductCategory.RING,
            weightGrams = 4.2,
            purityKarat = 18,
            makingFeePercent = 12.0,
            stockCount = 8,
            code = "RNG-1801"
        ),
        ProductEntity(
            name = "گردنبند ظریف ونکلیف",
            category = ProductCategory.NECKLACE,
            weightGrams = 7.5,
            purityKarat = 18,
            makingFeePercent = 18.0,
            stockCount = 4,
            code = "NCK-1807"
        ),
        ProductEntity(
            name = "دستبند کارتیر",
            category = ProductCategory.BRACELET,
            weightGrams = 11.3,
            purityKarat = 18,
            makingFeePercent = 15.0,
            stockCount = 3,
            code = "BRC-1811"
        ),
        ProductEntity(
            name = "گوشواره مروارید طلا",
            category = ProductCategory.EARRING,
            weightGrams = 3.1,
            purityKarat = 18,
            makingFeePercent = 20.0,
            stockCount = 6,
            code = "EAR-1803"
        ),
        ProductEntity(
            name = "سکه بهار آزادی طرح جدید",
            category = ProductCategory.COIN,
            weightGrams = 8.13,
            purityKarat = 22,
            makingFeePercent = 0.0,
            stockCount = 20,
            code = "COIN-2201"
        ),
        ProductEntity(
            name = "شمش طلا ۱۰ گرمی",
            category = ProductCategory.BULLION,
            weightGrams = 10.0,
            purityKarat = 24,
            makingFeePercent = 2.0,
            stockCount = 12,
            code = "BLN-2410"
        )
    )
}
