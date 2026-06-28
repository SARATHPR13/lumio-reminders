package com.lumio.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
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

class LargeWidgetProvider : AppWidgetProvider() {

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
        val views    = RemoteViews(context.packageName, R.layout.widget_large)
        val reminders = WidgetDataProvider.getTodayReminders(context)

        // Date header
        val dateStr = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.tv_large_widget_date, dateStr)

        // Count badge
        val countText = when (reminders.size) {
            0    -> "Nothing today"
            1    -> "1 reminder today"
            else -> "${reminders.size} reminders today"
        }
        views.setTextViewText(R.id.tv_large_widget_count, countText)

        if (reminders.isEmpty()) {
            // Show empty state
            views.setViewVisibility(R.id.list_widget_reminders, View.GONE)
            views.setViewVisibility(R.id.layout_widget_empty,   View.VISIBLE)
        } else {
            // Show list
            views.setViewVisibility(R.id.list_widget_reminders, View.VISIBLE)
            views.setViewVisibility(R.id.layout_widget_empty,   View.GONE)

            // Set up RemoteViews adapter
            val listIntent = Intent(context, LargeWidgetService::class.java)
            views.setRemoteAdapter(R.id.list_widget_reminders, listIntent)
            views.setEmptyView(R.id.list_widget_reminders, R.id.layout_widget_empty)
        }

        // Open app on tap
        val openIntent  = Intent(context, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context, widgetId + 1000, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tv_large_widget_date, openPending)

        // Add reminder button
        val addIntent  = Intent(context, MainActivity::class.java).apply {
            putExtra("open_add", true)
        }
        val addPending = PendingIntent.getActivity(
            context, widgetId + 2000, addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_add, addPending)

        manager.updateAppWidget(widgetId, views)
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.list_widget_reminders)
    }

    override fun onEnabled(context: Context)  { super.onEnabled(context) }
    override fun onDisabled(context: Context) { super.onDisabled(context) }
}
