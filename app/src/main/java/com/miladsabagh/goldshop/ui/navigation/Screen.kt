package com.miladsabagh.goldshop.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Invoices : Screen("invoices")
    object NewInvoice : Screen("new_invoice")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
    object Products : Screen("products")
    object ProductEdit : Screen("product_edit?productId={productId}") {
        fun createRoute(productId: Long? = null) = "product_edit?productId=${productId ?: -1L}"
    }
    object Customers : Screen("customers")
    object CustomerEdit : Screen("customer_edit?customerId={customerId}") {
        fun createRoute(customerId: Long? = null) = "customer_edit?customerId=${customerId ?: -1L}"
    }
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
