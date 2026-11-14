package com.pplantreman.app

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        // Ensure channels are created
        NotificationHelper.createNotificationChannels(ctx)

        val type = inputData.getString("NOTIFICATION_TYPE") ?: "MOTIVATION"

        when (type) {
            "WATER" -> NotificationHelper.sendWaterNotification(ctx)
            "WORKOUT" -> NotificationHelper.sendWorkoutNotification(ctx)
            else -> {
                // Fallback — send a generic motivation notification
                val quotes = listOf(
                    "Kendini geliştirmeye devam et! 🚀",
                    "Her gün bir adım daha ileri. ✨",
                    "Başarı alışkanlıkların toplamıdır. 💎"
                )
                NotificationHelper.sendMotivationalNotification(ctx, quotes.random())
            }
        }

        return Result.success()
    }
}
