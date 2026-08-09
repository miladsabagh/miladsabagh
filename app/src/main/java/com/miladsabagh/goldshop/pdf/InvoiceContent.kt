package com.miladsabagh.goldshop.pdf

import com.miladsabagh.goldshop.data.local.entity.Invoice
import com.miladsabagh.goldshop.data.local.entity.InvoiceItem
import com.miladsabagh.goldshop.data.settings.StoreSettings

/** Plain data snapshot used to render an invoice into a PDF, independent of Compose/DB. */
data class InvoiceContent(
    val store: StoreSettings,
    val invoice: Invoice,
    val items: List<InvoiceItem>
)
