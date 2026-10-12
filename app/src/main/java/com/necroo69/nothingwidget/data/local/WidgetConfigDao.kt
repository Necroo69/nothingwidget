package com.necroo69.nothingwidget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs ORDER BY lastUpdatedTimestamp DESC")
    fun getAllWidgetConfigs(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs WHERE id = :id LIMIT 1")
    suspend fun getWidgetConfigById(id: String): WidgetConfigEntity?

    @Query("SELECT * FROM widget_configs WHERE isPinned = 1")
    fun getPinnedWidgetConfigs(): Flow<List<WidgetConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWidgetConfig(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE id = :id")
    suspend fun deleteWidgetConfig(id: String)
}
