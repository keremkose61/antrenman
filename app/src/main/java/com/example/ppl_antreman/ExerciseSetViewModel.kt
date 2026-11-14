package com.pplantreman.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExerciseSetViewModel(application: Application) : AndroidViewModel(application) {
    private val exerciseSetDao = AppDatabase.getDatabase(application).exerciseSetDao()

    fun insertSet(exerciseName: String, weight: Float, reps: Int, date: Long) {
        viewModelScope.launch {
            val set = ExerciseSet(
                exerciseName = exerciseName,
                weight = weight,
                reps = reps,
                date = date
            )
            exerciseSetDao.insert(set)
        }
    }

    suspend fun getLastSetForExercise(exerciseName: String): ExerciseSet? {
        return exerciseSetDao.getLastSetForExercise(exerciseName)
    }
}
