package com.zarin.gold.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlin.math.roundToLong

enum class ProductCategory(val labelFa: String) {
    RING("انگشتر"),
    NECKLACE("گردنبند"),
    BRACELET("دستبند"),
    EARRING("گوشواره"),
    COIN("سکه"),
    BULLION("شمش"),
    SET("سرویس")
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: ProductCategory,
    val carat: Int,
    val weightGram: Double,
    val makingFeePercent: Double,
    val stonePrice: Long = 0,
    val imageHint: String = "",
    val inStock: Boolean = true
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val nationalId: String = ""
)

data class InvoiceLine(
    val productId: Long,
    val productName: String,
    val carat: Int,
    val weightGram: Double,
    val unitGoldPrice: Long,
    val makingFeePercent: Double,
    val stonePrice: Long,
    val quantity: Int = 1
) {
    val goldValue: Long
        get() = (weightGram * unitGoldPrice * quantity).roundToLong()

    val makingFee: Long
        get() = (goldValue * makingFeePercent / 100.0).roundToLong()

    val lineTotal: Long
        get() = goldValue + makingFee + (stonePrice * quantity)
}

@Entity(tableName = "invoices")
@TypeConverters(Converters::class)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val goldPricePerGram18: Long,
    val lines: List<InvoiceLine>,
    val discount: Long = 0,
    val note: String = ""
) {
    val subtotal: Long get() = lines.sumOf { it.lineTotal }
    val tax: Long get() = ((subtotal - discount) * 0.09).roundToLong()
    val total: Long get() = subtotal - discount + tax
}

class Converters {
    @TypeConverter
    fun fromLines(value: List<InvoiceLine>): String =
        value.joinToString("||") {
            listOf(
                it.productId,
                it.productName.replace("|", "/"),
                it.carat,
                it.weightGram,
                it.unitGoldPrice,
                it.makingFeePercent,
                it.stonePrice,
                it.quantity
            ).joinToString("|")
        }

    @TypeConverter
    fun toLines(value: String): List<InvoiceLine> {
        if (value.isBlank()) return emptyList()
        return value.split("||").mapNotNull { part ->
            val p = part.split("|")
            if (p.size < 8) return@mapNotNull null
            InvoiceLine(
                productId = p[0].toLong(),
                productName = p[1],
                carat = p[2].toInt(),
                weightGram = p[3].toDouble(),
                unitGoldPrice = p[4].toLong(),
                makingFeePercent = p[5].toDouble(),
                stonePrice = p[6].toLong(),
                quantity = p[7].toInt()
            )
        }
    }

    @TypeConverter
    fun fromCategory(value: ProductCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ProductCategory = ProductCategory.valueOf(value)
}
