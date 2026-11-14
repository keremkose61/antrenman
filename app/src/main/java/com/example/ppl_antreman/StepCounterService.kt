package com.pplantreman.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialSteps = -1
    private var sessionSteps = 0
    private var baseSteps = 0
    private var baseCalories = 0
    private var baseRunningSteps = 0
    private var lastStepTime = 0L
    private var runningSteps = 0
    
    companion object {
        const val CHANNEL_ID = NotificationHelper.CHANNEL_STEPS
        const val NOTIFICATION_ID = 1
        const val ACTION_RESET = "com.pplantreman.app.ACTION_RESET"
        private const val RUNNING_THRESHOLD = 100 // steps per minute
        private const val DAILY_STEP_GOAL = 10000

        @Volatile
        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        // Load existing saved statistics as base
        val prefs = getSharedPreferences("FitnessStats", Context.MODE_PRIVATE)
        baseSteps = prefs.getInt("total_steps", 0)
        baseCalories = prefs.getInt("calories_burned", 0)
        baseRunningSteps = (prefs.getFloat("running_minutes", 0f) * 100).toInt()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Use centralized channel creation
        NotificationHelper.createNotificationChannels(this)
        val initialNotif = createNotification(baseSteps, baseCalories.toFloat())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotif,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotif)
        }
        
        if (stepCounterSensor == null) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Bu cihazda adım sayacı sensörü bulunamadı", Toast.LENGTH_LONG).show()
            }
            stopSelf()
            return
        }
        
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESET) {
            initialSteps = -1
            sessionSteps = 0
            baseSteps = 0
            baseCalories = 0
            baseRunningSteps = 0
            runningSteps = 0
            lastStepTime = 0L
            val notification = createNotification(0, 0f)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            updateWidget()
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalSteps = it.values[0].toInt()
                
                if (initialSteps == -1) {
                    initialSteps = totalSteps
                }
                
                sessionSteps = totalSteps - initialSteps
                if (sessionSteps < 0) {
                    // Device was rebooted while service was active
                    initialSteps = totalSteps
                    sessionSteps = 0
                }
                
                // Detect running based on step frequency
                val currentTime = System.currentTimeMillis()
                if (lastStepTime > 0) {
                    val timeDiff = currentTime - lastStepTime
                    if (timeDiff in 200..60000) {
                        val stepsPerMinute = (60000f / timeDiff)
                        if (stepsPerMinute in RUNNING_THRESHOLD.toFloat()..300f) {
                            runningSteps++
                        }
                    }
                }
                lastStepTime = currentTime
                
                val currentTotalSteps = baseSteps + sessionSteps
                val currentCalories = baseCalories + (sessionSteps * 0.04f).toInt()
                val currentTotalRunningSteps = baseRunningSteps + runningSteps
                
                // Save to SharedPreferences
                saveStatistics(currentTotalSteps, currentCalories, currentTotalRunningSteps)
                
                // Update notification
                val notification = createNotification(currentTotalSteps, currentCalories.toFloat())
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Update Home Screen Widget
                updateWidget()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    private fun saveStatistics(steps: Int, calories: Int, runningStepsCount: Int) {
        val prefs = getSharedPreferences("FitnessStats", Context.MODE_PRIVATE)
        
        // Calculate workout minutes (assuming average 100 steps per minute)
        val workoutMinutes = steps / 100f
        
        // Calculate running minutes (assuming 100 steps per minute when running)
        val runningMinutes = runningStepsCount / 100f
        
        prefs.edit().apply {
            putInt("total_steps", steps)
            putFloat("workout_minutes", workoutMinutes)
            putInt("calories_burned", calories)
            putFloat("running_minutes", runningMinutes)
            apply()
        }
    }

    private fun updateWidget() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(this, PplAppWidgetProvider::class.java)
            )
            if (widgetIds.isNotEmpty()) {
                val intent = Intent(this, PplAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                sendBroadcast(intent)
            }
        } catch (_: Exception) {
            // Widget update failed, safe to ignore
        }
    }

    private fun createNotification(steps: Int, calories: Float): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = ((steps.toFloat() / DAILY_STEP_GOAL) * 100).toInt().coerceIn(0, 100)
        val progressBar = buildProgressBar(progress)
        val accentColor = androidx.core.content.ContextCompat.getColor(this, R.color.notif_color_steps)

        val contentText = if (steps == 0) {
            "Adım sayacı başlatıldı — Yürümeye başla! 🚶"
        } else {
            "🚶 $steps adım • 🔥 ${calories.toInt()} kcal • %$progress"
        }

        val bigText = if (steps == 0) {
            "Adım sayacı aktif. Yürümeye başladığında\nistatistiklerin burada görünecek. 👟"
        } else {
            "$progressBar\n\n" +
            "🚶 Adım: $steps / $DAILY_STEP_GOAL\n" +
            "🔥 Kalori: ${calories.toInt()} kcal\n" +
            "📊 Hedefe: %$progress"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚡ Aktivite Takibi")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("⚡ Aktivite Takibi")
                    .bigText(bigText)
                    .setSummaryText("Günlük Hedef: $DAILY_STEP_GOAL adım")
            )
            .setSmallIcon(R.drawable.ic_notif_steps)
            .setColor(accentColor)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * Builds a visual text-based progress bar for the notification expanded view.
     */
    private fun buildProgressBar(percent: Int): String {
        val totalBlocks = 20
        val filled = (percent * totalBlocks / 100).coerceIn(0, totalBlocks)
        val empty = totalBlocks - filled
        return "▓".repeat(filled) + "░".repeat(empty) + " %$percent"
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        sensorManager.unregisterListener(this)
        
        // Save latest statistics
        val currentTotalSteps = baseSteps + sessionSteps
        val currentCalories = baseCalories + (sessionSteps * 0.04f).toInt()
        val currentTotalRunningSteps = baseRunningSteps + runningSteps
        saveStatistics(currentTotalSteps, currentCalories, currentTotalRunningSteps)
        updateWidget()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
