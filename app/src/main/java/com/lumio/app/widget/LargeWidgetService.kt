package com.lumio.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lumio.app.R
import com.lumio.app.data.local.entity.ReminderEntity
import com.lumio.app.domain.model.Priority
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LargeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return LargeWidgetFactory(applicationContext)
    }
}

class LargeWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var reminders: List<ReminderEntity> = emptyList()

    override fun onCreate() { loadData() }

    override fun onDataSetChanged() { loadData() }

    private fun loadData() {
        reminders = runBlocking { WidgetDataProvider.getTodayReminders(context) }
    }

    override fun getCount(): Int = reminders.size

    override fun getViewAt(position: Int): RemoteViews {
        val views    = RemoteViews(context.packageName, R.layout.widget_list_item)
        val reminder = reminders[position]

        views.setTextViewText(R.id.tv_item_title, reminder.title)

        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date(reminder.dateTimeMillis))
        views.setTextViewText(R.id.tv_item_time, timeStr)

        // Priority dot color
        val dotColor = when (reminder.priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH   -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW    -> Color.parseColor("#FF4CAF50")
            Priority.NONE   -> Color.parseColor("#FF1A73E8")
        }
        views.setInt(R.id.view_priority_dot, "setBackgroundColor", dotColor)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = reminders[position].id
    override fun hasStableIds(): Boolean = true
    override fun onDestroy() {}
}
