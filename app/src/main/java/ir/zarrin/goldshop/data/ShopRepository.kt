package ir.zarrin.goldshop.data

import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.data.local.Customer
import ir.zarrin.goldshop.data.local.CustomerDao
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.data.local.InvoiceDao
import ir.zarrin.goldshop.data.local.InvoiceItem
import ir.zarrin.goldshop.data.local.InvoiceWithItems
import ir.zarrin.goldshop.data.local.Product
import ir.zarrin.goldshop.data.local.ProductDao
import ir.zarrin.goldshop.data.local.toLineInput
import ir.zarrin.goldshop.domain.GoldCalculator
import ir.zarrin.goldshop.domain.InvoiceTotals
import ir.zarrin.goldshop.domain.model.InvoiceType
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.Karat
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode
import kotlinx.coroutines.flow.Flow

class ShopRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao
) {

    fun observeProducts(): Flow<List<Product>> = productDao.observeAll()

    fun observeProductCount(): Flow<Int> = productDao.observeCount()

    suspend fun findProduct(id: Long): Product? = productDao.findById(id)

    suspend fun saveProduct(product: Product): Long =
        if (product.id == 0L) productDao.insert(product) else {
            productDao.update(product)
            product.id
        }

    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    fun observeCustomers(): Flow<List<Customer>> = customerDao.observeAll()

    fun observeCustomerCount(): Flow<Int> = customerDao.observeCount()

    suspend fun findCustomer(id: Long): Customer? = customerDao.findById(id)

    suspend fun saveCustomer(customer: Customer): Long =
        if (customer.id == 0L) customerDao.insert(customer) else {
            customerDao.update(customer)
            customer.id
        }

    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    fun observeInvoices(): Flow<List<Invoice>> = invoiceDao.observeAll()

    fun observeInvoice(id: Long): Flow<InvoiceWithItems?> = invoiceDao.observeWithItems(id)

    suspend fun findInvoice(id: Long): InvoiceWithItems? = invoiceDao.findWithItems(id)

    suspend fun deleteInvoice(id: Long) = invoiceDao.deleteById(id)

    /** Sequential number scoped to the Jalali year, for example `1403-0007`. */
    suspend fun nextInvoiceNumber(): String {
        val year = PersianCalendar.today().year
        val previous = invoiceDao.lastNumber()
        val previousSequence = previous
            ?.substringAfterLast('-', "")
            ?.toIntOrNull()
            ?.takeIf { previous.startsWith("$year-") }
            ?: 0
        val sequence = if (previousSequence > 0) previousSequence + 1 else invoiceDao.count() + 1
        return "%d-%04d".format(year, sequence)
    }

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val totals = totalsOf(invoice, items)
        val stamped = invoice.copy(payable = totals.payable, updatedAt = System.currentTimeMillis())
        val invoiceId = invoiceDao.saveInvoice(stamped, items)
        if (invoice.id == 0L && invoice.type == InvoiceType.SALE) {
            items.filter { it.productId != null }.forEach { item ->
                productDao.decreaseStock(item.productId!!, item.quantity)
            }
        }
        return invoiceId
    }

    fun totalsOf(invoice: Invoice, items: List<InvoiceItem>): InvoiceTotals {
        val lines = items.map { item ->
            val input = item.toLineInput()
            input to GoldCalculator.calculateLine(input)
        }
        return GoldCalculator.summarize(lines, invoice.discount, invoice.paid)
    }

    fun totalsOf(invoiceWithItems: InvoiceWithItems): InvoiceTotals =
        totalsOf(invoiceWithItems.invoice, invoiceWithItems.orderedItems)

    /** Fills an empty database with believable demo data so the app can be explored right away. */
    suspend fun seedSampleData(goldRate18: Long, taxPercent: Double) {
        val customers = listOf(
            Customer(name = "مریم رضایی", phone = "09121234567", nationalId = "0079123456", address = "تهران، خیابان ولیعصر، پلاک ۱۲"),
            Customer(name = "علی محمدی", phone = "09353334455", nationalId = "0064332211", address = "اصفهان، چهارباغ بالا"),
            Customer(name = "زهرا کریمی", phone = "09197778899", address = "شیراز، بلوار زند")
        )
        val customerIds = customers.map { customerDao.insert(it) }

        val products = listOf(
            Product(code = "R-1001", name = "انگشتر طرح ونکلیف", kind = ItemKind.MANUFACTURED, karat = Karat.K18, weightGrams = 3.250, wageValue = 9.0, profitPercent = 7.0, stock = 4),
            Product(code = "N-2004", name = "گردنبند زنجیر ایتالیایی", kind = ItemKind.MANUFACTURED, karat = Karat.K18, weightGrams = 7.480, wageValue = 12.0, profitPercent = 7.0, stock = 2),
            Product(code = "B-3010", name = "دستبند النگویی رولباز", kind = ItemKind.MANUFACTURED, karat = Karat.K18, weightGrams = 11.900, wageValue = 14.5, profitPercent = 7.0, stock = 3),
            Product(code = "E-4007", name = "گوشواره حلقه‌ای نگین‌دار", kind = ItemKind.MANUFACTURED, karat = Karat.K18, weightGrams = 2.140, wageValue = 15.0, profitPercent = 7.0, stonePrice = 850_000L, stock = 6),
            Product(code = "C-5001", name = "ربع سکه بهار آزادی", kind = ItemKind.COIN, karat = Karat.MELTED, weightGrams = 2.032, wageValue = 0.0, profitPercent = 0.0, unitPriceOverride = 18_500_000L, stock = 5)
        )
        val productIds = products.map { productDao.insert(it) }

        val rate18 = GoldCalculator.rateForKarat(goldRate18, Karat.K18)
        val invoice = Invoice(
            number = nextInvoiceNumber(),
            type = InvoiceType.SALE,
            customerId = customerIds.first(),
            customerName = customers.first().name,
            customerPhone = customers.first().phone,
            customerNationalId = customers.first().nationalId,
            customerAddress = customers.first().address,
            baseGoldRate = goldRate18,
            taxPercent = taxPercent,
            note = "سرویس عروس – تحویل حضوری"
        )
        val items = listOf(
            InvoiceItem(
                productId = productIds[0],
                title = products[0].name,
                kind = products[0].kind,
                karat = products[0].karat,
                weightGrams = products[0].weightGrams,
                quantity = 1,
                goldRatePerGram = rate18,
                wageMode = WageMode.PERCENT,
                wageValue = products[0].wageValue,
                profitPercent = products[0].profitPercent,
                taxBasis = TaxBasis.WAGE_AND_PROFIT,
                taxPercent = taxPercent
            ),
            InvoiceItem(
                productId = productIds[3],
                title = products[3].name,
                kind = products[3].kind,
                karat = products[3].karat,
                weightGrams = products[3].weightGrams,
                quantity = 1,
                goldRatePerGram = rate18,
                wageMode = WageMode.PERCENT,
                wageValue = products[3].wageValue,
                profitPercent = products[3].profitPercent,
                stonePrice = products[3].stonePrice,
                taxBasis = TaxBasis.WAGE_AND_PROFIT,
                taxPercent = taxPercent
            )
        )
        saveInvoice(invoice, items)
    }

    suspend fun isEmpty(): Boolean = invoiceDao.count() == 0
}
