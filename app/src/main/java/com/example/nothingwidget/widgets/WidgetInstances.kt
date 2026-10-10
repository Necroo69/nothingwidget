package com.example.nothingwidget.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.example.nothingwidget.MainActivity
import com.example.nothingwidget.data.local.AppDatabase
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Gallery template id, carried by the pin callback and the tap-to-edit intent. */
const val EXTRA_PRESET_ID = "com.example.nothingwidget.extra.PRESET_ID"

/** MainActivity action: open the customizer for one placed widget (EXTRA_APPWIDGET_ID). */
const val ACTION_EDIT_WIDGET = "com.example.nothingwidget.action.EDIT_WIDGET"

fun widgetRepository(context: Context): WidgetRepository {
    val db = AppDatabase.getInstance(context)
    return WidgetRepository(db.widgetConfigDao(), db.widgetInstanceDao())
}

/** The widget's accent color, or white if there is no config or its hex is invalid. */
fun accentColorOf(config: NothingWidgetConfig?): Int =
    try { Color.parseColor(config?.accentColorHex ?: "#FFFFFF") } catch (e: IllegalArgumentException) { Color.WHITE }

/** Runs [block] off the main thread for the duration of the broadcast being delivered. */
fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

/** Tapping a placed widget opens the customizer for that widget only. */
fun editWidgetPendingIntent(context: Context, appWidgetId: Int, fallbackPresetId: String): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = ACTION_EDIT_WIDGET
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        putExtra(EXTRA_PRESET_ID, fallbackPresetId)
        // Start fresh so the customizer opens even if the app is already running.
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    // The request code keeps one PendingIntent per widget; extras alone don't make them distinct.
    return PendingIntent.getActivity(
        context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

/** Base for every home-screen provider: keeps the widget_instances table in step with the launcher. */
abstract class NothingWidgetProvider : AppWidgetProvider() {
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        runAsync { widgetRepository(context).deleteWidgets(appWidgetIds) }
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        runAsync { widgetRepository(context).remapWidgets(oldWidgetIds, newWidgetIds) }
    }
}

/**
 * Receives requestPinAppWidget's success callback, which the system fills in with the new
 * widget's EXTRA_APPWIDGET_ID, and links that widget to the gallery template it was pinned from.
 */
class WidgetPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || presetId == null) return

        runAsync {
            val repo = widgetRepository(context)
            repo.linkWidgetToPreset(appWidgetId, presetId)
            // The widget's first onUpdate may already have drawn the provider's default template.
            repo.getConfigById(presetId)?.let { requestWidgetUpdate(context, it.type) }
        }
    }

    companion object {
        fun successCallback(context: Context, presetId: String): PendingIntent {
            val intent = Intent(context, WidgetPinnedReceiver::class.java).putExtra(EXTRA_PRESET_ID, presetId)
            // Mutable so the system can add EXTRA_APPWIDGET_ID. The intent is explicit, so this is safe.
            val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            return PendingIntent.getBroadcast(
                context, presetId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or mutable
            )
        }
    }
}
