package ir.zarrin.goldshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.Invoice
import ir.zarrin.goldshop.data.settings.SettingsRepository
import ir.zarrin.goldshop.data.settings.ShopSettings
import ir.zarrin.goldshop.domain.model.InvoiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class ReportRange(val label: String) {
    TODAY("امروز"),
    MONTH("این ماه"),
    YEAR("امسال"),
    ALL("کل")
}

data class ReportsState(
    val range: ReportRange = ReportRange.MONTH,
    val settings: ShopSettings = ShopSettings(),
    val invoiceCount: Int = 0,
    val salesTotal: Long = 0L,
    val purchaseTotal: Long = 0L,
    val receivedTotal: Long = 0L,
    val outstanding: Long = 0L,
    val topCustomers: List<Pair<String, Long>> = emptyList(),
    val invoices: List<Invoice> = emptyList()
)

class ReportsViewModel(
    repository: ShopRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val range = MutableStateFlow(ReportRange.MONTH)

    val state: StateFlow<ReportsState> = combine(
        repository.observeInvoices(),
        settingsRepository.settings,
        range
    ) { invoices, settings, selectedRange ->
        val now = System.currentTimeMillis()
        val from = when (selectedRange) {
            ReportRange.TODAY -> PersianCalendar.startOfDay(now)
            ReportRange.MONTH -> PersianCalendar.startOfJalaliMonth(now)
            ReportRange.YEAR -> PersianCalendar.startOfJalaliYear(now)
            ReportRange.ALL -> Long.MIN_VALUE
        }
        val scoped = invoices.filter { it.dateMillis >= from }
        val sales = scoped.filter { it.type == InvoiceType.SALE }
        val purchases = scoped.filter { it.type == InvoiceType.PURCHASE }
        ReportsState(
            range = selectedRange,
            settings = settings,
            invoiceCount = scoped.size,
            salesTotal = sales.sumOf { it.payable },
            purchaseTotal = purchases.sumOf { it.payable },
            receivedTotal = sales.sumOf { it.paid },
            outstanding = sales.sumOf { (it.payable - it.paid).coerceAtLeast(0L) },
            topCustomers = sales
                .groupBy { it.customerName.ifBlank { "مشتری متفرقه" } }
                .map { (name, group) -> name to group.sumOf { it.payable } }
                .sortedByDescending { it.second }
                .take(5),
            invoices = scoped
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsState())

    fun onRangeChange(value: ReportRange) = range.update { value }
}
