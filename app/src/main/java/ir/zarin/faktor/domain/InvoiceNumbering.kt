package ir.zarin.faktor.domain

import ir.zarin.faktor.core.JalaliDate
import java.util.Locale

/**
 * شماره‌گذاری فاکتور بر اساس سال و ماه شمسی، مثال: `140505-001`.
 * شماره‌ها در هر ماه از یک شروع می‌شوند و به‌صورت الفبایی هم مرتب می‌مانند.
 */
object InvoiceNumbering {

    fun monthlyPrefix(userPrefix: String, date: JalaliDate): String =
        userPrefix.trim() + String.format(Locale.US, "%04d%02d-", date.year, date.month)

    fun next(userPrefix: String, date: JalaliDate, lastNumber: String?): String {
        val prefix = monthlyPrefix(userPrefix, date)
        val lastSequence = lastNumber
            ?.takeIf { it.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?.toIntOrNull()
            ?: 0
        return prefix + String.format(Locale.US, "%03d", lastSequence + 1)
    }
}
