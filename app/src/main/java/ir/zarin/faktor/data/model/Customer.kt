package ir.zarin.faktor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val nationalCode: String = "",
    val address: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
