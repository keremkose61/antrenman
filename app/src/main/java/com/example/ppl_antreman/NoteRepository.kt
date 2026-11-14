package com.pplantreman.app

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Notu Room'a yazar. (API entegrasyonu kaldırıldı)
     */
    suspend fun insert(note: Note) {
        // Sadece yerel (offline-first)
        noteDao.insertNote(note)
    }


    suspend fun getNoteByDate(date: Long): Note? = noteDao.getNoteByDate(date)

    fun getNotesBetweenDates(startDate: Long, endDate: Long): Flow<List<Note>> =
        noteDao.getNotesBetweenDates(startDate, endDate)

    /**
     * Sunucudaki notları Room'a senkronize eder. (API entegrasyonu kaldırıldı)
     */
    suspend fun syncFromServer(userId: Int) {
        // Backend kaldırıldığı için hiçbir işlem yapmaz
    }
}
