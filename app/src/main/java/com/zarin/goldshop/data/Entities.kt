package com.zarin.goldshop.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,      // طلا / جواهر / سکه / آبشده
    val weight: Double,        // grams
    val karat: Int,            // عیار (e.g. 18, 21, 24)
    val wagePercent: Double,   // اجرت %
    val stonePrice: Long,      // قیمت نگین/سنگ (Toman)
    val stock: Int,            // موجودی
    val code: String = "",     // کد محصول
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val dateMillis: Long,
    val goldPricePerGram: Long,
    val goldValueTotal: Long,
    val wageTotal: Long,
    val stoneTotal: Long,
    val profitTotal: Long,
    val taxTotal: Long,
    val discount: Long,
    val grandTotal: Long,
    val note: String = "",
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val name: String,
    val weight: Double,
    val karat: Int,
    val wagePercent: Double,
    val stonePrice: Long,
    val quantity: Int,
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val tax: Long,
    val unitTotal: Long,
    val lineTotal: Long,
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItemEntity>,
)

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "طلا و جواهر زرین",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val goldPricePerGram: Long = 3_500_000, // Toman per gram, 18k
    val profitPercent: Double = 7.0,
    val taxPercent: Double = 9.0,
    val lastInvoiceSeq: Int = 0,
)
