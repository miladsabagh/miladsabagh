package ir.goldshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fullName: String,
    val phoneNumber: String = "",
    val address: String = "",
    val nationalId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
