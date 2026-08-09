package ir.zarrin.goldshop.data.repo

import androidx.room.withTransaction
import ir.zarrin.goldshop.data.db.CustomerDao
import ir.zarrin.goldshop.data.db.CustomerEntity
import ir.zarrin.goldshop.data.db.InvoiceDao
import ir.zarrin.goldshop.data.db.InvoiceEntity
import ir.zarrin.goldshop.data.db.InvoiceItemEntity
import ir.zarrin.goldshop.data.db.InvoiceWithItems
import ir.zarrin.goldshop.data.db.ProductDao
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.data.db.ZarrinDatabase
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.DraftItem
import ir.zarrin.goldshop.domain.InvoiceStatus
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.domain.PriceBreakdown
import ir.zarrin.goldshop.domain.PricingEngine
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.summarize
import ir.zarrin.goldshop.util.JalaliDate
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {

    fun observeAll(): Flow<List<ProductEntity>> = dao.observeAll()

    fun search(query: String, category: String): Flow<List<ProductEntity>> = dao.search(query, category)

    fun observeCount(): Flow<Int> = dao.observeCount()

    fun observeTotalStock(): Flow<Int> = dao.observeTotalStock()

    fun observeTotalWeight(): Flow<Double> = dao.observeTotalWeight()

    suspend fun findById(id: Long): ProductEntity? = dao.findById(id)

    suspend fun save(product: ProductEntity): Long =
        if (product.id == 0L) dao.insert(product) else {
            dao.update(product)
            product.id
        }

    suspend fun delete(product: ProductEntity) = dao.delete(product)
}

class CustomerRepository(private val dao: CustomerDao) {

    fun observeAll(): Flow<List<CustomerEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<CustomerEntity>> = dao.search(query)

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun findById(id: Long): CustomerEntity? = dao.findById(id)

    suspend fun save(customer: CustomerEntity): Long =
        if (customer.id == 0L) dao.insert(customer) else {
            dao.update(customer)
            customer.id
        }

    suspend fun delete(customer: CustomerEntity) = dao.delete(customer)
}

/** ورودی ثبت یک فاکتور جدید */
data class NewInvoiceRequest(
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String,
    val items: List<DraftItem>,
    val goldRate18: Long,
    val taxPercent: Double,
    val invoiceDiscount: Long,
    val paidAmount: Long,
    val paymentMethod: PaymentMethod,
    val note: String
)

class InvoiceRepository(
    private val database: ZarrinDatabase,
    private val invoiceDao: InvoiceDao,
    private val productDao: ProductDao,
    private val settingsRepository: SettingsRepository
) {

    fun observeAll(): Flow<List<InvoiceWithItems>> = invoiceDao.observeAll()

    fun search(query: String, status: String): Flow<List<InvoiceWithItems>> =
        invoiceDao.search(query, status)

    fun observeById(id: Long): Flow<InvoiceWithItems?> = invoiceDao.observeById(id)

    fun observeByCustomer(customerId: Long): Flow<List<InvoiceWithItems>> =
        invoiceDao.observeByCustomer(customerId)

    fun observeRecent(limit: Int): Flow<List<InvoiceEntity>> = invoiceDao.observeRecent(limit)

    suspend fun updatePayment(id: Long, paid: Long, total: Long) {
        invoiceDao.updatePayment(id, paid, InvoiceStatus.of(total, paid).name)
    }

    suspend fun delete(id: Long) {
        database.withTransaction {
            val items = invoiceDao.itemsOf(id)
            items.forEach { item ->
                item.productId?.let { productDao.increaseStock(it, item.quantity) }
            }
            invoiceDao.deleteById(id)
        }
    }

    suspend fun create(request: NewInvoiceRequest): Long {
        val sequence = settingsRepository.nextInvoiceSequence()
        val jalaliYear = JalaliDate.now().year
        val number = "%d-%04d".format(jalaliYear, sequence)

        val breakdowns: List<Pair<DraftItem, PriceBreakdown>> = request.items.map { item ->
            item to PricingEngine.calculate(item.priceInput(request.goldRate18, request.taxPercent))
        }
        val totals = breakdowns.map { it.second }.summarize(
            invoiceDiscount = request.invoiceDiscount,
            totalWeight = breakdowns.sumOf { (item, _) ->
                if (item.pricingMode == PricingMode.BY_WEIGHT) item.weightGrams * item.quantity else 0.0
            }
        )

        return database.withTransaction {
            val invoiceId = invoiceDao.insertInvoice(
                InvoiceEntity(
                    number = number,
                    customerId = request.customerId,
                    customerName = request.customerName.ifBlank { "مشتری حضوری" },
                    customerPhone = request.customerPhone,
                    dateMillis = System.currentTimeMillis(),
                    goldRate18 = request.goldRate18,
                    taxPercent = request.taxPercent,
                    totalGoldValue = totals.goldValue,
                    totalWage = totals.wage,
                    totalProfit = totals.profit,
                    totalStone = totals.stone,
                    totalTax = totals.tax,
                    itemsDiscount = totals.itemsDiscount,
                    subtotal = totals.subtotal,
                    invoiceDiscount = totals.invoiceDiscount,
                    grandTotal = totals.grandTotal,
                    paidAmount = request.paidAmount,
                    totalWeight = totals.totalWeight,
                    paymentMethod = request.paymentMethod.name,
                    status = InvoiceStatus.of(totals.grandTotal, request.paidAmount).name,
                    note = request.note
                )
            )

            val rows = breakdowns.map { (item, price) ->
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = item.productId,
                    name = item.name,
                    category = item.category.name,
                    karat = item.karat,
                    weightGrams = item.weightGrams,
                    quantity = item.quantity,
                    pricingMode = item.pricingMode.name,
                    ratePerGram = PricingEngine.ratePerGram(request.goldRate18, item.karat),
                    wagePercent = item.wagePercent,
                    profitPercent = item.profitPercent,
                    stonePrice = item.stonePrice,
                    fixedPrice = item.fixedPrice,
                    taxable = item.taxable,
                    goldValue = price.goldValue,
                    wage = price.wage,
                    profit = price.profit,
                    tax = price.tax,
                    unitPrice = price.unitPrice,
                    discount = price.discount,
                    lineTotal = price.lineTotal
                )
            }
            invoiceDao.insertItems(rows)

            breakdowns.forEach { (item, _) ->
                item.productId?.let { productDao.decreaseStock(it, item.quantity) }
            }
            invoiceId
        }
    }
}
