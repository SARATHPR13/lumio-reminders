package com.lumio.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdater {

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        updateSmall(context, manager)
        updateLarge(context, manager)
        updateTransparent(context, manager)
    }

    private fun updateSmall(context: Context, manager: AppWidgetManager) {
        val ids = manager.getAppWidgetIds(
            ComponentName(context, SmallWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            SmallWidgetProvider().onUpdate(context, manager, ids)
        }
    }

    private fun updateLarge(context: Context, manager: AppWidgetManager) {
        val ids = manager.getAppWidgetIds(
            ComponentName(context, LargeWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            LargeWidgetProvider().onUpdate(context, manager, ids)
            ids.forEach { id ->
                manager.notifyAppWidgetViewDataChanged(id, android.R.id.list)
            }
        }
    }

    private fun updateTransparent(context: Context, manager: AppWidgetManager) {
        val ids = manager.getAppWidgetIds(
            ComponentName(context, TransparentWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            TransparentWidgetProvider().onUpdate(context, manager, ids)
        }
    }
}
