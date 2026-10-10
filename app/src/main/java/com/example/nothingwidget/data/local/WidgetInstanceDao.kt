package com.example.nothingwidget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WidgetInstanceDao {
    @Query("SELECT * FROM widget_instances WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getInstance(appWidgetId: Int): WidgetInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInstance(instance: WidgetInstanceEntity)

    @Query("DELETE FROM widget_instances WHERE appWidgetId IN (:appWidgetIds)")
    suspend fun deleteInstances(appWidgetIds: List<Int>)

    @Query("UPDATE widget_instances SET appWidgetId = :newId WHERE appWidgetId = :oldId")
    suspend fun remapInstance(oldId: Int, newId: Int)
}
