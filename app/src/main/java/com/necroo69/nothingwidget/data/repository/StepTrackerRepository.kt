package com.necroo69.nothingwidget.data.repository

import com.necroo69.nothingwidget.data.local.StepDao
import com.necroo69.nothingwidget.data.local.StepEntryEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackerRepository(private val stepDao: StepDao) {

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todaySteps: Flow<StepEntryEntity?> = stepDao.getStepsForDate(todayDateString)
    val weeklyHistory: Flow<List<StepEntryEntity>> = stepDao.getWeeklyStepHistory()

    suspend fun incrementSteps(delta: Int = 250) {
        val currentEntry = stepDao.getStepsForDateSync(todayDateString)
        val existingCount = currentEntry?.stepCount ?: 7420 // Default base steps
        val newCount = existingCount + delta
        val calories = (newCount * 0.04f).toInt()
        val distance = newCount * 0.75f
        val activeMins = (newCount / 120)

        stepDao.insertOrUpdateStepEntry(
            StepEntryEntity(
                dateString = todayDateString,
                stepCount = newCount,
                targetGoal = 10000,
                caloriesBurned = calories,
                distanceMeters = distance,
                activeMinutes = activeMins
            )
        )
    }
}
