package com.zarnegar.gold.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.PaymentMethod
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ProductCategory
import com.zarnegar.gold.domain.model.WageMode

@Entity(tableName = "products", indices = [Index(value = ["code"], unique = true)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String,
    val pricingMode: String,
    val karat: Int,
    val weightGrams: Double,
    val stoneWeightGrams: Double,
    val stoneValue: Long,
    val wageMode: String,
    val wagePercent: Double,
    val wagePerGram: Long,
    val profitPercent: Double,
    val fixedPrice: Long,
    val vatExempt: Boolean,
    val stock: Int,
    val note: String,
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val nationalCode: String,
    val address: String,
    val note: String,
)

@Entity(tableName = "invoices", indices = [Index(value = ["number"], unique = true)])
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val createdAt: Long,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String,
    val customerNationalCode: String,
    val goldRateSnapshot: Long,
    val vatPercentSnapshot: Double,
    val grossBeforeTax: Long,
    val itemDiscountTotal: Long,
    val invoiceDiscount: Long,
    val vatTotal: Long,
    val roundingAdjustment: Long,
    val payable: Long,
    val paidAmount: Long,
    val paymentMethod: String,
    val status: String,
    val note: String,
)

@Entity(
    tableName = "invoice_lines",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("invoiceId")],
)
data class InvoiceLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long?,
    val title: String,
    val description: String,
    val karat: Int,
    val weightGrams: Double,
    val quantity: Int,
    val ratePerGram: Long,
    val goldValue: Long,
    val wage: Long,
    val profit: Long,
    val stoneValue: Long,
    val discount: Long,
    val vat: Long,
    val total: Long,
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    code = code,
    name = name,
    category = ProductCategory.fromName(category),
    pricingMode = PricingMode.fromName(pricingMode),
    karat = karat,
    weightGrams = weightGrams,
    stoneWeightGrams = stoneWeightGrams,
    stoneValue = stoneValue,
    wageMode = WageMode.fromName(wageMode),
    wagePercent = wagePercent,
    wagePerGram = wagePerGram,
    profitPercent = profitPercent,
    fixedPrice = fixedPrice,
    vatExempt = vatExempt,
    stock = stock,
    note = note,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    code = code,
    name = name,
    category = category.name,
    pricingMode = pricingMode.name,
    karat = karat,
    weightGrams = weightGrams,
    stoneWeightGrams = stoneWeightGrams,
    stoneValue = stoneValue,
    wageMode = wageMode.name,
    wagePercent = wagePercent,
    wagePerGram = wagePerGram,
    profitPercent = profitPercent,
    fixedPrice = fixedPrice,
    vatExempt = vatExempt,
    stock = stock,
    note = note,
)

fun CustomerEntity.toDomain(): Customer = Customer(id, fullName, phone, nationalCode, address, note)

fun Customer.toEntity(): CustomerEntity =
    CustomerEntity(id, fullName, phone, nationalCode, address, note)

fun InvoiceEntity.toDomain(lines: List<InvoiceLine> = emptyList()): Invoice = Invoice(
    id = id,
    number = number,
    createdAt = createdAt,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    customerNationalCode = customerNationalCode,
    goldRateSnapshot = goldRateSnapshot,
    vatPercentSnapshot = vatPercentSnapshot,
    grossBeforeTax = grossBeforeTax,
    itemDiscountTotal = itemDiscountTotal,
    invoiceDiscount = invoiceDiscount,
    vatTotal = vatTotal,
    roundingAdjustment = roundingAdjustment,
    payable = payable,
    paidAmount = paidAmount,
    paymentMethod = PaymentMethod.fromName(paymentMethod),
    status = InvoiceStatus.fromName(status),
    note = note,
    lines = lines,
)

fun Invoice.toEntity(): InvoiceEntity = InvoiceEntity(
    id = id,
    number = number,
    createdAt = createdAt,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    customerNationalCode = customerNationalCode,
    goldRateSnapshot = goldRateSnapshot,
    vatPercentSnapshot = vatPercentSnapshot,
    grossBeforeTax = grossBeforeTax,
    itemDiscountTotal = itemDiscountTotal,
    invoiceDiscount = invoiceDiscount,
    vatTotal = vatTotal,
    roundingAdjustment = roundingAdjustment,
    payable = payable,
    paidAmount = paidAmount,
    paymentMethod = paymentMethod.name,
    status = status.name,
    note = note,
)

fun InvoiceLineEntity.toDomain(): InvoiceLine = InvoiceLine(
    id = id,
    invoiceId = invoiceId,
    productId = productId,
    title = title,
    description = description,
    karat = karat,
    weightGrams = weightGrams,
    quantity = quantity,
    ratePerGram = ratePerGram,
    goldValue = goldValue,
    wage = wage,
    profit = profit,
    stoneValue = stoneValue,
    discount = discount,
    vat = vat,
    total = total,
)

fun InvoiceLine.toEntity(invoiceId: Long): InvoiceLineEntity = InvoiceLineEntity(
    id = id,
    invoiceId = invoiceId,
    productId = productId,
    title = title,
    description = description,
    karat = karat,
    weightGrams = weightGrams,
    quantity = quantity,
    ratePerGram = ratePerGram,
    goldValue = goldValue,
    wage = wage,
    profit = profit,
    stoneValue = stoneValue,
    discount = discount,
    vat = vat,
    total = total,
)
