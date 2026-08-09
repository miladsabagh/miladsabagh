package ir.zarin.faktor

import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.core.JalaliDate
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.data.model.ProductCategory
import ir.zarin.faktor.data.settings.AppSettings
import ir.zarin.faktor.domain.GoldPricing
import ir.zarin.faktor.ui.sale.SaleLine

/** داده نمونه مشترک بین تست‌های واحد، تست PDF و تست‌های تصویری. */
object TestSamples {

    val settings = AppSettings(
        shopName = "گالری طلا و جواهر زرین",
        shopPhone = "021-88776655",
        shopAddress = "تهران، خیابان ولیعصر، نبش کوچه گلستان، پلاک ۱۲",
        goldRatePerGramRial = 92_000_000,
        profitPercent = 7.0,
        vatPercent = 10.0,
        currencyUnit = CurrencyUnit.TOMAN,
        invoicePrefix = "",
        persianDigits = true,
    )

    val customer = Customer(
        id = 1,
        name = "مریم احمدی",
        phone = "09121234567",
        nationalCode = "0060123456",
        address = "تهران، سعادت‌آباد",
    )

    val customers = listOf(
        customer,
        Customer(id = 2, name = "رضا کریمی", phone = "09351112233"),
        Customer(id = 3, name = "سمیرا موسوی", phone = "09197654321"),
    )

    val products = listOf(
        Product(
            id = 1,
            code = "R-101",
            name = "انگشتر طلا ۱۸ عیار",
            category = ProductCategory.GOLD,
            karat = 18,
            weightGrams = 4.25,
            wagePercent = 12.0,
            stockQty = 3,
        ),
        Product(
            id = 2,
            code = "S-204",
            name = "سرویس طلا نیم‌ست",
            category = ProductCategory.GOLD,
            karat = 18,
            weightGrams = 12.8,
            wagePercent = 15.0,
            stonePriceRial = 5_000_000,
            stockQty = 1,
        ),
        Product(
            id = 3,
            code = "C-001",
            name = "سکه تمام بهار آزادی",
            category = ProductCategory.COIN,
            pricingMode = PricingMode.FIXED_PRICE,
            fixedPriceRial = 950_000_000,
            applyVat = false,
            stockQty = 5,
        ),
        Product(
            id = 4,
            code = "J-330",
            name = "گردنبند جواهر با نگین برلیان",
            category = ProductCategory.JEWELRY,
            karat = 18,
            weightGrams = 7.6,
            wagePercent = 22.0,
            stonePriceRial = 180_000_000,
            stockQty = 0,
        ),
    )

    val saleLines = listOf(
        SaleLine.fromProduct(products[0]).copy(key = "line-1"),
        SaleLine.fromProduct(products[1]).copy(key = "line-2"),
        SaleLine.fromProduct(products[2], quantity = 2).copy(key = "line-3"),
    )

    val invoiceDateMillis: Long = JalaliCalendar.toEpochMillis(JalaliDate(1405, 5, 18)) + 14 * 3_600_000L

    /** فاکتور نمونه که مبالغ آن با همان موتور قیمت‌گذاری برنامه محاسبه شده است. */
    fun invoice(paidRatio: Double = 1.0): InvoiceWithItems {
        val context = settings.pricingContext
        val breakdowns = saleLines.map { it.breakdown(context) }
        val totals = GoldPricing.totals(breakdowns, invoiceDiscountRial = 2_000_000)
        val items = saleLines.zip(breakdowns) { line, breakdown -> line.toInvoiceItem(breakdown) }
            .mapIndexed { index, item -> item.copy(id = index + 1L, invoiceId = 1) }

        return InvoiceWithItems(
            invoice = Invoice(
                id = 1,
                number = "140505-001",
                dateMillis = invoiceDateMillis,
                customerId = customer.id,
                customerName = customer.name,
                customerPhone = customer.phone,
                goldRatePerGramRial = settings.goldRatePerGramRial,
                profitPercent = settings.profitPercent,
                vatPercent = settings.vatPercent,
                goldTotalRial = totals.goldTotalRial,
                wageTotalRial = totals.wageTotalRial,
                profitTotalRial = totals.profitTotalRial,
                stoneTotalRial = totals.stoneTotalRial,
                vatTotalRial = totals.vatTotalRial,
                itemsDiscountRial = totals.itemsDiscountRial,
                invoiceDiscountRial = totals.invoiceDiscountRial,
                grandTotalRial = totals.grandTotalRial,
                paidAmountRial = (totals.grandTotalRial * paidRatio).toLong(),
                paymentMethod = PaymentMethod.CARD,
                currencyUnit = settings.currencyUnit,
                note = "ضمانت‌نامه یک‌ساله تعویض رایگان قفل و بند به همراه فاکتور ارائه شد.",
            ),
            items = items,
        )
    }

    val recentInvoices: List<Invoice> = listOf(
        invoice().invoice,
        invoice().invoice.copy(
            id = 2,
            number = "140505-002",
            customerName = "رضا کریمی",
            customerPhone = "09351112233",
            grandTotalRial = 1_480_000_000,
            paidAmountRial = 500_000_000,
            dateMillis = invoiceDateMillis - 26 * 3_600_000L,
        ),
        invoice().invoice.copy(
            id = 3,
            number = "140505-003",
            customerName = "سمیرا موسوی",
            customerPhone = "09197654321",
            grandTotalRial = 720_500_000,
            paidAmountRial = 0,
            dateMillis = invoiceDateMillis - 50 * 3_600_000L,
        ),
    )
}
