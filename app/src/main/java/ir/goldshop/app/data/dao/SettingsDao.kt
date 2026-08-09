package ir.goldshop.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.goldshop.app.data.entity.ShopSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = ${ShopSettings.SINGLE_ROW_ID}")
    fun observe(): Flow<ShopSettings?>

    @Query("SELECT * FROM shop_settings WHERE id = ${ShopSettings.SINGLE_ROW_ID}")
    suspend fun get(): ShopSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ShopSettings)

    @Query("UPDATE shop_settings SET nextInvoiceNumber = nextInvoiceNumber + 1 WHERE id = ${ShopSettings.SINGLE_ROW_ID}")
    suspend fun incrementInvoiceNumber()
}
