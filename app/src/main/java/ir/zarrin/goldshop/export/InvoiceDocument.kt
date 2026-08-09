package ir.zarrin.goldshop.export

import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.data.local.InvoiceItem
import ir.zarrin.goldshop.data.settings.ShopSettings
import ir.zarrin.goldshop.domain.InvoiceTotals
import ir.zarrin.goldshop.domain.LineTotals

/** Everything a renderer needs to lay an invoice out on paper or in a message. */
data class InvoiceDocument(
    val invoice: Invoice,
    val items: List<InvoiceItem>,
    val lineTotals: List<LineTotals>,
    val totals: InvoiceTotals,
    val settings: ShopSettings
) {
    val currencyLabel: String get() = settings.currency.shortLabel

    val dateLabel: String get() = PersianCalendar.fromEpochMillis(invoice.dateMillis).formatNumeric()

    val timeLabel: String get() = PersianCalendar.formatClock(invoice.createdAt)

    val numberLabel: String get() = PersianNumbers.toPersianDigits(invoice.number)

    val title: String get() = "فاکتور ${invoice.type.label} طلا و جواهر"

    fun money(value: Long): String = "${PersianNumbers.formatAmount(value)} $currencyLabel"

    fun amount(value: Long): String = PersianNumbers.formatAmount(value)

    fun amountInWords(): String = "${PersianNumbers.toWords(totals.payable)} $currencyLabel"

    /** Plain text version used for sharing through messengers or SMS. */
    fun toPlainText(): String = buildString {
        appendLine("«${settings.shopName}»")
        if (settings.phone.isNotBlank()) appendLine("تلفن: ${PersianNumbers.toPersianDigits(settings.phone)}")
        appendLine("──────────────")
        appendLine("$title شماره $numberLabel")
        appendLine("تاریخ: $dateLabel")
        appendLine("مشتری: ${invoice.customerName.ifBlank { "مشتری متفرقه" }}")
        if (invoice.customerPhone.isNotBlank()) {
            appendLine("تلفن مشتری: ${PersianNumbers.toPersianDigits(invoice.customerPhone)}")
        }
        appendLine("نرخ هر گرم طلای ۱۸ عیار: ${money(invoice.baseGoldRate)}")
        appendLine("──────────────")
        items.forEachIndexed { index, item ->
            val line = lineTotals.getOrNull(index)
            val row = PersianNumbers.toPersianDigits((index + 1).toString())
            appendLine("$row) ${item.title.ifBlank { item.kind.label }}")
            if (item.weightGrams > 0.0) {
                appendLine("   وزن: ${PersianNumbers.formatWeight(item.weightGrams)} گرم — ${ir.zarrin.goldshop.domain.model.Karat.label(item.karat)}")
            }
            appendLine("   تعداد: ${PersianNumbers.toPersianDigits(item.quantity.toString())}")
            if (line != null) appendLine("   مبلغ: ${money(line.total)}")
        }
        appendLine("──────────────")
        appendLine("ارزش طلا: ${money(totals.goldValue)}")
        appendLine("اجرت ساخت: ${money(totals.wage)}")
        appendLine("سود فروشنده: ${money(totals.profit)}")
        if (totals.stone > 0L) appendLine("قیمت نگین: ${money(totals.stone)}")
        appendLine("مالیات بر ارزش افزوده: ${money(totals.tax)}")
        if (totals.discount > 0L) appendLine("تخفیف: ${money(totals.discount)}")
        appendLine("مبلغ قابل پرداخت: ${money(totals.payable)}")
        appendLine("پرداخت‌شده: ${money(totals.paid)}")
        if (totals.remaining != 0L) appendLine("مانده: ${money(totals.remaining)}")
        appendLine("به حروف: ${amountInWords()}")
        if (settings.invoiceFooter.isNotBlank()) {
            appendLine("──────────────")
            append(settings.invoiceFooter)
        }
    }

    companion object {
        fun fileName(invoice: Invoice): String = "factor-${invoice.number.ifBlank { invoice.id.toString() }}.pdf"
    }
}
