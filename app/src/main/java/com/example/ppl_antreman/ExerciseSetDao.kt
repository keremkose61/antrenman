package com.pplantreman.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseSetDao {
    @Insert
    suspend fun insert(set: ExerciseSet)

    @Query("SELECT * FROM exercise_sets WHERE exerciseName = :name ORDER BY date DESC LIMIT 1")
    suspend fun getLastSetForExercise(name: String): ExerciseSet?
}
