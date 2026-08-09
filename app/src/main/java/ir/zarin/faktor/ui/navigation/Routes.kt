package ir.zarin.faktor.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val INVOICES = "invoices"
    const val SETTINGS = "settings"
    const val NEW_SALE = "new_sale"

    const val PRODUCT_ID_ARG = "productId"
    const val CUSTOMER_ID_ARG = "customerId"
    const val INVOICE_ID_ARG = "invoiceId"

    const val PRODUCT_EDIT = "product_edit/{$PRODUCT_ID_ARG}"
    const val CUSTOMER_EDIT = "customer_edit/{$CUSTOMER_ID_ARG}"
    const val INVOICE_DETAIL = "invoice_detail/{$INVOICE_ID_ARG}"

    fun productEdit(productId: Long): String = "product_edit/$productId"

    fun customerEdit(customerId: Long): String = "customer_edit/$customerId"

    fun invoiceDetail(invoiceId: Long): String = "invoice_detail/$invoiceId"
}
