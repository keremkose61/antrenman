@file:Suppress("PackageDirectoryMismatch")

package com.pplantreman.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.Locale

class IstatistikFragment : Fragment(R.layout.fragment_istatistik) {

    private lateinit var stepsText: TextView
    private lateinit var workoutHoursText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var runningHoursText: TextView
    private lateinit var btnToggleTracking: Button
    private lateinit var serviceStatusText: TextView

    private var isServiceRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null) return
            loadStatistics()
            handler.postDelayed(this, 2000)
        }
    }

    private val requestActivityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) requestNotificationAndStart()
        else showPermissionDeniedDialog()
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        startStepCounterService()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepsText        = view.findViewById(R.id.stepsValue)
        workoutHoursText = view.findViewById(R.id.workoutHoursValue)
        caloriesText     = view.findViewById(R.id.caloriesValue)
        runningHoursText = view.findViewById(R.id.runningHoursValue)
        btnToggleTracking = view.findViewById(R.id.btnToggleTracking)
        serviceStatusText = view.findViewById(R.id.serviceStatus)

        loadStatistics()
        
        val btnResetStats: Button = view.findViewById(R.id.btnResetStats)
        btnResetStats.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            AlertDialog.Builder(ctx)
                .setTitle("Gelişim Verilerini Sıfırla")
                .setMessage("Tüm gelişim verileriniz sıfırlanacak. Emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    val act = activity ?: return@setPositiveButton
                    val prefs = act.getSharedPreferences("FitnessStats", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    // Reset Texts
                    stepsText.text = "0"
                    workoutHoursText.text = "0"
                    caloriesText.text = "0"
                    runningHoursText.text = "0"

                    val resetIntent = Intent(act, StepCounterService::class.java).apply {
                        action = StepCounterService.ACTION_RESET
                    }
                    act.startService(resetIntent)

                    try {
                        val appWidgetManager = AppWidgetManager.getInstance(act)
                        val widgetIds = appWidgetManager.getAppWidgetIds(
                            ComponentName(act, PplAppWidgetProvider::class.java)
                        )
                        if (widgetIds.isNotEmpty()) {
                            val widgetIntent = Intent(act, PplAppWidgetProvider::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                            }
                            act.sendBroadcast(widgetIntent)
                        }
                    } catch (_: Exception) {}

                    loadStatistics()
                    Toast.makeText(act, "Gelişim verileri sıfırlandı", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        btnToggleTracking.setOnClickListener {
            if (isServiceRunning) stopStepCounterService()
            else checkPermissionAndStartService()
        }

        updateServiceStatus()
    }

    private fun loadStatistics() {
        val act = activity ?: return
        if (!isAdded) return
        val prefs = act.getSharedPreferences("FitnessStats", Context.MODE_PRIVATE)

        val steps          = prefs.getInt("total_steps", 0)
        val workoutMinutes = prefs.getFloat("workout_minutes", 0f)
        val calories       = prefs.getInt("calories_burned", 0)
        val runningMinutes = prefs.getFloat("running_minutes", 0f)

        stepsText.text        = String.format(Locale.getDefault(), "%,d", steps)
        workoutHoursText.text = String.format(Locale.getDefault(), "%.0f", workoutMinutes)
        caloriesText.text     = String.format(Locale.getDefault(), "%,d", calories)
        runningHoursText.text = String.format(Locale.getDefault(), "%.0f", runningMinutes)
    }

    override fun onResume() {
        super.onResume()
        isServiceRunning = StepCounterService.isRunning || isMyServiceRunning(StepCounterService::class.java)
        updateServiceStatus()
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val ctx = context ?: return false
        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager ?: return false
        return try {
            @Suppress("DEPRECATION")
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun checkPermissionAndStartService() {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> requestNotificationAndStart()
                else -> requestActivityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            requestNotificationAndStart()
        }
    }

    private fun requestNotificationAndStart() {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> startStepCounterService()
                else -> requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startStepCounterService()
        }
    }

    private fun startStepCounterService() {
        val ctx = context ?: return
        val intent = Intent(ctx, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
        isServiceRunning = true
        updateServiceStatus()
    }

    private fun stopStepCounterService() {
        val ctx = context ?: return
        val intent = Intent(ctx, StepCounterService::class.java)
        ctx.stopService(intent)
        isServiceRunning = false
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        val ctx = context ?: return
        if (isServiceRunning) {
            btnToggleTracking.text = "Takibi Durdur"
            btnToggleTracking.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, android.R.color.holo_red_dark)
            )
            serviceStatusText.text = "🟢 Otomatik takip aktif"
            serviceStatusText.setTextColor(
                ContextCompat.getColor(ctx, R.color.success)
            )
        } else {
            btnToggleTracking.text = "Takibi Başlat"
            btnToggleTracking.backgroundTintList = null
            serviceStatusText.text = "⚪ Otomatik takip kapalı"
            serviceStatusText.setTextColor(
                ContextCompat.getColor(ctx, R.color.text_secondary_light)
            )
        }
    }

    private fun showPermissionDeniedDialog() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle("İzin Gerekli")
            .setMessage("Adım sayımı için aktivite tanıma izni gereklidir.")
            .setPositiveButton("Tamam", null)
            .show()
    }
}
