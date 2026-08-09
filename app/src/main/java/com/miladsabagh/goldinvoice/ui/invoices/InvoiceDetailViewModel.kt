package com.miladsabagh.goldinvoice.ui.invoices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miladsabagh.goldinvoice.data.dao.InvoiceWithItems
import com.miladsabagh.goldinvoice.data.repository.InvoiceRepository
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository
import com.miladsabagh.goldinvoice.pdf.InvoicePdfGenerator
import com.miladsabagh.goldinvoice.pdf.sharePdfFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceDetailViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
    invoiceId: Long
) : ViewModel() {

    val invoiceWithItems: StateFlow<InvoiceWithItems?> = invoiceRepository.observeWithItems(invoiceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun shareAsPdf(context: Context) {
        val data = invoiceWithItems.value ?: return
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val file = InvoicePdfGenerator.generate(context.applicationContext, data, settings)
            sharePdfFile(context, file)
        }
    }
}
