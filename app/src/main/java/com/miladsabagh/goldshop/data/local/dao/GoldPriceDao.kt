package com.miladsabagh.goldshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miladsabagh.goldshop.data.local.entity.GoldPriceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldPriceDao {

    @Insert
    suspend fun insert(entry: GoldPriceEntry): Long

    @Query("SELECT * FROM gold_price_history ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<GoldPriceEntry?>

    @Query("SELECT * FROM gold_price_history ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatest(): GoldPriceEntry?

    @Query("SELECT * FROM gold_price_history ORDER BY recordedAt DESC LIMIT :limit")
    fun observeHistory(limit: Int = 30): Flow<List<GoldPriceEntry>>
}
