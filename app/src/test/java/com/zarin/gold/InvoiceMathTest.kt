package com.zarin.gold

import com.zarin.gold.data.Invoice
import com.zarin.gold.data.InvoiceLine
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceMathTest {

    @Test
    fun lineTotal_includesGoldMakingAndStone() {
        val line = InvoiceLine(
            productId = 1,
            productName = "انگشتر",
            carat = 18,
            weightGram = 2.0,
            unitGoldPrice = 10_000_000,
            makingFeePercent = 10.0,
            stonePrice = 1_000_000,
            quantity = 1
        )
        // gold = 20_000_000, making = 2_000_000, stone = 1_000_000
        assertEquals(23_000_000L, line.lineTotal)
    }

    @Test
    fun invoice_appliesDiscountAndTax() {
        val invoice = Invoice(
            invoiceNumber = "ZR-TEST-001",
            customerName = "آزمون",
            goldPricePerGram18 = 40_000_000,
            lines = listOf(
                InvoiceLine(
                    productId = 1,
                    productName = "شمش",
                    carat = 24,
                    weightGram = 1.0,
                    unitGoldPrice = 50_000_000,
                    makingFeePercent = 0.0,
                    stonePrice = 0,
                    quantity = 1
                )
            ),
            discount = 5_000_000
        )
        assertEquals(50_000_000L, invoice.subtotal)
        // tax = (50M - 5M) * 0.09 = 4_050_000
        assertEquals(4_050_000L, invoice.tax)
        assertEquals(49_050_000L, invoice.total)
    }
}
