package com.pplantreman.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE date = :date LIMIT 1")
    suspend fun getNoteByDate(date: Long): Note?
    
    // Get notes between two dates (inclusive)
    @Query("SELECT * FROM notes WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getNotesBetweenDates(startDate: Long, endDate: Long): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)
}
