package com.miladsabagh.goldshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val firstName: String,
    val lastName: String = "",
    val phone: String = "",
    val address: String = "",
    val nationalCode: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}
