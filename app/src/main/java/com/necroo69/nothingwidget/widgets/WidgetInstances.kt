package com.necroo69.nothingwidget.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.necroo69.nothingwidget.MainActivity
import com.necroo69.nothingwidget.R
import com.necroo69.nothingwidget.data.local.AppDatabase
import com.necroo69.nothingwidget.data.repository.WidgetRepository
import com.necroo69.nothingwidget.domain.model.NothingWidgetConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Gallery template id, carried by the pin callback and the tap-to-edit intent. */
const val EXTRA_PRESET_ID = "com.necroo69.nothingwidget.extra.PRESET_ID"

/** MainActivity action: open the customizer for one placed widget (EXTRA_APPWIDGET_ID). */
const val ACTION_EDIT_WIDGET = "com.necroo69.nothingwidget.action.EDIT_WIDGET"

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

/** MainActivity action: open Settings at the weather location picker. */
const val ACTION_SET_WEATHER_LOCATION = "com.necroo69.nothingwidget.action.SET_WEATHER_LOCATION"

/** Tapping a weather widget that has no location opens the location picker. */
fun weatherLocationPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = ACTION_SET_WEATHER_LOCATION
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    return PendingIntent.getActivity(
        context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

/**
 * Applies what every widget shares: the card drawn from [config] (corner radius, dot grid, glyph
 * border) and tap-to-edit. [defaultSizeDp] is the provider's minimum size, used until the
 * launcher reports the real one.
 */
fun RemoteViews.applyCard(
    context: Context,
    manager: AppWidgetManager,
    appWidgetId: Int,
    config: NothingWidgetConfig?,
    rootId: Int,
    presetId: String,
    defaultSizeDp: Pair<Int, Int>
) {
    val (width, height) = widgetSizeDp(manager, appWidgetId, defaultSizeDp.first, defaultSizeDp.second)
    setImageViewBitmap(R.id.widget_background, renderWidgetBackground(context, config, width, height))
    setOnClickPendingIntent(rootId, editWidgetPendingIntent(context, appWidgetId, presetId))
}

/** Shows [text] in [viewId], or hides the view when it is blank. */
fun RemoteViews.setOptionalText(viewId: Int, text: String?) {
    setViewVisibility(viewId, if (text.isNullOrBlank()) View.GONE else View.VISIBLE)
    setTextViewText(viewId, text.orEmpty())
}

/** Base for every home-screen provider: keeps the widget_instances table in step with the launcher. */
abstract class NothingWidgetProvider : AppWidgetProvider() {
    // Resizing changes the card bitmap's size, so redraw.
    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

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
