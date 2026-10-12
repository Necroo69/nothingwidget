package com.necroo69.nothingwidget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_entries ORDER BY dateString DESC LIMIT 7")
    fun getWeeklyStepHistory(): Flow<List<StepEntryEntity>>

    @Query("SELECT * FROM step_entries WHERE dateString = :dateString LIMIT 1")
    fun getStepsForDate(dateString: String): Flow<StepEntryEntity?>

    @Query("SELECT * FROM step_entries WHERE dateString = :dateString LIMIT 1")
    suspend fun getStepsForDateSync(dateString: String): StepEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStepEntry(entry: StepEntryEntity)
}
