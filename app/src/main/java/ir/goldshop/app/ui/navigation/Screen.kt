package ir.goldshop.app.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Invoices : Screen("invoices")
    data object Products : Screen("products")
    data object Customers : Screen("customers")
    data object Settings : Screen("settings")

    data object NewInvoice : Screen("new_invoice")
    data object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }

    data object ProductEdit : Screen("product_edit?productId={productId}") {
        fun createRoute(productId: Long? = null) = "product_edit?productId=${productId ?: -1}"
    }

    data object CustomerEdit : Screen("customer_edit?customerId={customerId}") {
        fun createRoute(customerId: Long? = null) = "customer_edit?customerId=${customerId ?: -1}"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard to "داشبورد",
    Screen.Invoices to "فاکتورها",
    Screen.Products to "کالاها",
    Screen.Customers to "مشتریان",
    Screen.Settings to "تنظیمات"
)
