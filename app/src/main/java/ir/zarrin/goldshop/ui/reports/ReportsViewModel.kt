package ir.zarrin.goldshop.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.util.JalaliDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DayBucket(val label: String, val total: Long, val count: Int)

data class TopProduct(val name: String, val quantity: Int, val total: Long)

data class ReportsState(
    val settings: AppSettings = AppSettings(),
    val monthlyBuckets: List<DayBucket> = emptyList(),
    val lastDays: List<DayBucket> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val totalSales: Long = 0L,
    val totalTax: Long = 0L,
    val totalWage: Long = 0L,
    val totalProfit: Long = 0L,
    val totalWeight: Double = 0.0,
    val invoiceCount: Int = 0,
    val paymentBreakdown: List<Pair<String, Long>> = emptyList()
)

class ReportsViewModel(
    invoiceRepository: InvoiceRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<ReportsState> = combine(
        invoiceRepository.observeAll(),
        settingsRepository.settings
    ) { invoices, settings ->
        val currentYear = JalaliDate.now().year

        val monthly = (1..12).map { month ->
            val ofMonth = invoices.filter {
                val date = JalaliDate.fromEpochMillis(it.invoice.dateMillis)
                date.year == currentYear && date.month == month
            }
            DayBucket(
                label = JalaliDate.monthName(month),
                total = ofMonth.sumOf { it.invoice.grandTotal },
                count = ofMonth.size
            )
        }

        val today = JalaliDate.now()
        val lastDays = (0..6).map { offset ->
            val dayStart = today.toEpochMillis() - offset * DAY_MILLIS
            val date = JalaliDate.fromEpochMillis(dayStart)
            val ofDay = invoices.filter {
                val invoiceDate = JalaliDate.fromEpochMillis(it.invoice.dateMillis)
                invoiceDate == date
            }
            DayBucket(
                label = "${date.day} ${JalaliDate.monthName(date.month)}",
                total = ofDay.sumOf { it.invoice.grandTotal },
                count = ofDay.size
            )
        }.reversed()

        val topProducts = invoices
            .flatMap { it.items }
            .groupBy { it.name }
            .map { (name, items) ->
                TopProduct(
                    name = name,
                    quantity = items.sumOf { it.quantity },
                    total = items.sumOf { it.lineTotal }
                )
            }
            .sortedByDescending { it.total }
            .take(5)

        val payments = invoices
            .groupBy { PaymentMethod.fromName(it.invoice.paymentMethod) }
            .map { (method, list) -> method.label to list.sumOf { it.invoice.grandTotal } }
            .sortedByDescending { it.second }

        ReportsState(
            settings = settings,
            monthlyBuckets = monthly,
            lastDays = lastDays,
            topProducts = topProducts,
            totalSales = invoices.sumOf { it.invoice.grandTotal },
            totalTax = invoices.sumOf { it.invoice.totalTax },
            totalWage = invoices.sumOf { it.invoice.totalWage },
            totalProfit = invoices.sumOf { it.invoice.totalProfit },
            totalWeight = invoices.sumOf { it.invoice.totalWeight },
            invoiceCount = invoices.size,
            paymentBreakdown = payments
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsState())

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
