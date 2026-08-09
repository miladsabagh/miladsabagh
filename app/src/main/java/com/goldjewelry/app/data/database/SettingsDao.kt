package com.goldjewelry.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.goldjewelry.app.data.model.ShopSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = 1")
    fun getSettings(): Flow<ShopSettings?>

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun getSettingsOnce(): ShopSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: ShopSettings)

    @Update
    suspend fun update(settings: ShopSettings)
}
