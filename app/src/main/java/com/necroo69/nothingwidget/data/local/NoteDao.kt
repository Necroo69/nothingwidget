package com.necroo69.nothingwidget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM saved_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<SavedNoteEntity>>

    @Query("SELECT * FROM saved_notes WHERE isPinnedToWidget = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getPinnedNote(): Flow<SavedNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SavedNoteEntity)

    @Query("DELETE FROM saved_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
