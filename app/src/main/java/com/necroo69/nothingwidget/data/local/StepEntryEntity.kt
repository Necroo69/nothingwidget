package com.necroo69.nothingwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_entries")
data class StepEntryEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val stepCount: Int,
    val targetGoal: Int = 10000,
    val caloriesBurned: Int,
    val distanceMeters: Float,
    val activeMinutes: Int
)
