package com.pplantreman.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PplAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // LATER: Ideally from Room DB via a Worker or Broadcast, for now we pull from SharedPreferences
        val prefs = context.getSharedPreferences("FitnessStats", Context.MODE_PRIVATE)
        val steps = prefs.getInt("total_steps", 0)
        val calories = prefs.getInt("calories_burned", 0)

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_ppl_stats)
        views.setTextViewText(R.id.tvWidgetSteps, String.format("%,d", steps))
        views.setTextViewText(R.id.tvWidgetCalories, String.format("%,d", calories))

        // Create an Intent to launch MainActivity when clicked
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
