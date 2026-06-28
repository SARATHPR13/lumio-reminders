package com.lumio.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lumio.app.MainActivity
import com.lumio.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransparentWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            scope.launch {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private suspend fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        val views  = RemoteViews(context.packageName, R.layout.widget_transparent)
        val next   = WidgetDataProvider.getNextReminder(context)

        if (next != null) {
            val timeStr = SimpleDateFormat("hh:mm a · MMM dd", Locale.getDefault())
                .format(Date(next.dateTimeMillis))
            views.setTextViewText(R.id.tv_transparent_title, next.title)
            views.setTextViewText(R.id.tv_transparent_time,  "⏰ $timeStr")
        } else {
            views.setTextViewText(R.id.tv_transparent_title, "No upcoming reminders")
            views.setTextViewText(R.id.tv_transparent_time,  "Tap to add one")
        }

        val openIntent  = Intent(context, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context, widgetId + 3000, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tv_transparent_title, openPending)

        manager.updateAppWidget(widgetId, views)
    }
}
