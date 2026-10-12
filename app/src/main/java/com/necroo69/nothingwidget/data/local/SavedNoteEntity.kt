package com.necroo69.nothingwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_notes")
data class SavedNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinnedToWidget: Boolean = false
)
