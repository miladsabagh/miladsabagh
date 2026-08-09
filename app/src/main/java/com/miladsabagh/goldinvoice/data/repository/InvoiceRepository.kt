package com.miladsabagh.goldinvoice.data.repository

import com.miladsabagh.goldinvoice.data.dao.InvoiceDao
import com.miladsabagh.goldinvoice.data.dao.InvoiceWithItems
import com.miladsabagh.goldinvoice.data.entity.Invoice
import com.miladsabagh.goldinvoice.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class InvoiceRepository(private val dao: InvoiceDao) {
    fun observeAll(): Flow<List<Invoice>> = dao.observeAll()
    fun observeWithItems(id: Long): Flow<InvoiceWithItems?> = dao.observeWithItems(id)

    fun observeTodayCount(): Flow<Int> = dao.observeCountSince(startOfTodayMillis())
    fun observeTodaySalesTotal(): Flow<Double> = dao.observeTotalSalesSince(startOfTodayMillis())

    suspend fun nextInvoiceNumber(): String {
        val count = dao.countAll()
        return (1000 + count + 1).toString()
    }

    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long =
        dao.insertInvoiceWithItems(invoice, items)

    suspend fun update(invoice: Invoice) = dao.update(invoice)
    suspend fun delete(invoice: Invoice) = dao.delete(invoice)

    private fun startOfTodayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
