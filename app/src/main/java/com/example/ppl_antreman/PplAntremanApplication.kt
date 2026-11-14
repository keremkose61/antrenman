package com.pplantreman.app

import android.app.Application
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Data
import java.util.concurrent.TimeUnit

class PplAntremanApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: NoteRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("PplAntremanApp", "Application started")
        
        database = AppDatabase.getDatabase(this)
        repository = NoteRepository(database.noteDao())
        
        // Create all notification channels at app startup
        NotificationHelper.createNotificationChannels(this)
        
        setupDailyNotifications()
    }

    private fun setupDailyNotifications() {
        // Water reminder every 2 hours
        val waterData = Data.Builder().putString("NOTIFICATION_TYPE", "WATER").build()
        val workReqWater = PeriodicWorkRequestBuilder<NotificationWorker>(2, TimeUnit.HOURS)
            .setInputData(waterData)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WaterReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workReqWater
        )
        
        // Workout reminder every 24 hours
        val workoutData = Data.Builder().putString("NOTIFICATION_TYPE", "WORKOUT").build()
        val workReqWorkout = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInputData(workoutData)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WorkoutReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workReqWorkout
        )
    }
}
