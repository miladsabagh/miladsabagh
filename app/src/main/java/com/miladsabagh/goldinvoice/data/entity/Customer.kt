package com.miladsabagh.goldinvoice.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val address: String = "",
    val nationalId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
