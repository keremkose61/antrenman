package com.pplantreman.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyMotivationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // App prefs for checking notification settings
        val appPrefs = applicationContext.getSharedPreferences("VexaAppPrefs", Context.MODE_PRIVATE)
        val notifPref = appPrefs.getInt("notification_pref", 0)

        // If notifPref is 2 (Sessiz), we don't send notification
        if (notifPref == 2) {
            return Result.success()
        }

        // Rich motivational quotes collection
        val quotes = listOf(
            // Güç & Dayanıklılık
            "Zayıflar pes eder. Sen devam et. 🔥",
            "Bugün dünden daha güçlü ol. Her tekrar seni zirveye taşır. 💪",
            "Ağrı geçici. Pes etmek sonsuzdur. Son tekrarı yap!",
            "Vücudunu geliştir, zihnini güçlendir. İkisi birlikte yükselir. 🧠",
            "Son tekrarı yap! Limitlerini zorla ve sınırlarını yık. ⚡",
            "Senin rakibin sadece dünkü sensin. Bugün onu geç!",

            // Disiplin & Tutarlılık
            "Motivasyon seni başlatır, disiplin seni bitiş çizgisine taşır. 🏁",
            "Tutarlılık yeteneği yener. Her gün bir adım daha at. 📈",
            "Başarı bir gün değil, her gün yapılan küçük şeylerin toplamıdır. 💎",
            "Konforsuz alanda büyüme başlar. Bugün rahatsız ol! 🌱",

            // Hedefe Odaklanma
            "Hedefini gözünün önünden ayırma. Her antrenman seni oraya yaklaştırır. 🎯",
            "Excuses don't burn calories. Bahaneler kalori yakmaz! 🏋️",
            "Bedel öde ya da bedel ödeme — iki yol da bedel. Hangisini seçiyorsun?",
            "Hayal et, planla, uygula. VEXA seninle. 🚀"
        )
        val quote = quotes.random()

        try {
            NotificationHelper.createNotificationChannels(applicationContext)
            NotificationHelper.sendMotivationalNotification(applicationContext, quote)
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
