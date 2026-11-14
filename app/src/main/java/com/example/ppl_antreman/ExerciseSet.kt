package com.pplantreman.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_sets")
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseName: String,
    val date: Long, // Epoch millis
    val weight: Float,
    val reps: Int
)
