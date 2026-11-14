package com.pplantreman.app

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    // Channel Group
    private const val GROUP_VEXA = "vexa_notification_group"

    // Channel IDs
    const val CHANNEL_MOTIVATION = "vexa_motivation_channel"
    const val CHANNEL_WATER = "vexa_water_channel"
    const val CHANNEL_WORKOUT = "vexa_workout_channel"
    const val CHANNEL_STEPS = "vexa_steps_channel"

    // Notification IDs
    const val NOTIF_ID_MOTIVATION = 2001
    const val NOTIF_ID_WATER = 2002
    const val NOTIF_ID_WORKOUT = 2003

    /**
     * Creates all notification channels organized under a group.
     * Call this once at app startup.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create channel group
            val group = NotificationChannelGroup(GROUP_VEXA, "VEXA Bildirimleri")
            notificationManager.createNotificationChannelGroup(group)

            // Motivation channel
            val motivationChannel = NotificationChannel(
                CHANNEL_MOTIVATION,
                "Günlük Motivasyon",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Günlük motivasyon sözleri ile enerjini yüksek tut!"
                setGroup(GROUP_VEXA)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setShowBadge(true)
            }

            // Water reminder channel
            val waterChannel = NotificationChannel(
                CHANNEL_WATER,
                "Su Hatırlatıcı",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Düzenli su içmeni hatırlatır."
                setGroup(GROUP_VEXA)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                setShowBadge(true)
            }

            // Workout reminder channel
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Antrenman Hatırlatıcı",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Günlük antrenmanını kaçırma!"
                setGroup(GROUP_VEXA)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }

            // Steps / Activity channel (low importance — ongoing foreground)
            val stepsChannel = NotificationChannel(
                CHANNEL_STEPS,
                "Aktivite Takibi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Adım sayacı ve aktivite takibi"
                setGroup(GROUP_VEXA)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(motivationChannel, waterChannel, workoutChannel, stepsChannel)
            )
        }
    }

    /**
     * Backward-compatible single-channel creation.
     * Delegates to createNotificationChannels.
     */
    fun createNotificationChannel(context: Context) {
        createNotificationChannels(context)
    }

    // ─── Motivation Notification ─────────────────────────────────────

    fun sendMotivationalNotification(context: Context, quote: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val accentColor = ContextCompat.getColor(context, R.color.notif_color_motivation)
        val largeIcon = createGradientIcon(context, "⚡", R.color.notif_color_motivation)

        val builder = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_notif_motivation)
            .setLargeIcon(largeIcon)
            .setContentTitle("🔥 VEXA — Günün Motivasyonu")
            .setContentText(quote)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("🔥 VEXA — Günün Motivasyonu")
                    .bigText(quote)
                    .setSummaryText("Motivasyon")
            )
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_MOTIVATION, builder.build())
    }

    // ─── Water Notification ──────────────────────────────────────────

    private val waterMessages = listOf(
        "Bir bardak su iç ve vücuduna enerji ver! 💧",
        "Hidrasyon = Performans. Hadi bir yudum! 🥤",
        "Kasların su istiyor! Hedefine bir bardak daha yaklaş. 💪",
        "Su içmeyi unutma! Vücudunun %60'ı sudan oluşuyor. 🌊",
        "Bir bardak su = Bir adım daha sağlıklı sen. ✨",
        "Metabolizmanı hızlandır, bir bardak su iç! 🚀",
        "Su molası zamanı! Bugün kaç bardak içtin? 📊"
    )

    fun sendWaterNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val message = waterMessages.random()
        val accentColor = ContextCompat.getColor(context, R.color.notif_color_water)
        val largeIcon = createGradientIcon(context, "💧", R.color.notif_color_water)

        val builder = NotificationCompat.Builder(context, CHANNEL_WATER)
            .setSmallIcon(R.drawable.ic_notif_water)
            .setLargeIcon(largeIcon)
            .setContentTitle("💧 Su İçme Vakti!")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("💧 Su İçme Vakti!")
                    .bigText(message)
                    .setSummaryText("Hidrasyon")
            )
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_WATER, builder.build())
    }

    // ─── Workout Notification ────────────────────────────────────────

    private val workoutTitles = listOf(
        "💪 Antrenman Zamanı!",
        "🏋️ Bugün Hangi Kas Grubu?",
        "⚡ Güne Enerjik Başla!",
        "🔥 Demir Vakti Geldi!"
    )

    private val workoutMessages = listOf(
        "Günün antrenmanını tamamla ve serini bozma! Hedefine odaklan.",
        "Bugün çalışırsan, yarın teşekkür edersin. Hadi başla!",
        "Son tekrar en önemlisi! Spor salonuna gitme zamanı.",
        "Kaslarını çalıştır, sınırlarını zorla. Büyüme konforsuz alanda başlar!",
        "Push, Pull, Legs — hangisi olursa olsun, bugün en iyisini ver!",
        "Tutarlılık anahtar. Bugün de bir gün daha ekle serine! 📈"
    )

    fun sendWorkoutNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val title = workoutTitles.random()
        val message = workoutMessages.random()
        val accentColor = ContextCompat.getColor(context, R.color.notif_color_workout)
        val largeIcon = createGradientIcon(context, "💪", R.color.notif_color_workout)

        val builder = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_notif_workout)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(message)
                    .setSummaryText("Antrenman")
            )
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_WORKOUT, builder.build())
    }

    // ─── Gradient Large Icon Generator ───────────────────────────────

    /**
     * Creates a beautiful circular gradient bitmap to use as the notification large icon.
     * This gives notifications a premium, branded look.
     */
    private fun createGradientIcon(context: Context, emoji: String, colorRes: Int): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = ContextCompat.getColor(context, colorRes)

        // Draw circular gradient background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                baseColor,
                adjustAlpha(baseColor, 0.7f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, size.toFloat(), size.toFloat()),
            size / 2f, size / 2f, bgPaint
        )

        // Draw emoji/text centered
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.5f
            textAlign = Paint.Align.CENTER
        }
        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(emoji, size / 2f, textY, textPaint)

        return bitmap
    }

    /**
     * Adjusts alpha of a color.
     */
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }
}
