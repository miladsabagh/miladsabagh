package com.miladsabagh.goldinvoice.data

import androidx.room.TypeConverter
import com.miladsabagh.goldinvoice.data.entity.PaymentStatus

class Converters {
    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus =
        runCatching { PaymentStatus.valueOf(value) }.getOrDefault(PaymentStatus.UNPAID)
}
