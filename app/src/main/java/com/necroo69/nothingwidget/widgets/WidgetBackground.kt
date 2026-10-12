package com.necroo69.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import androidx.core.graphics.ColorUtils
import com.necroo69.nothingwidget.domain.model.NothingWidgetConfig
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Colors of the in-app preview card (NothingGlassCard in the dark theme), so a placed widget
// looks like the preview the user customized.
private const val CARD_COLOR = 0xFF18181B.toInt()
private const val OUTLINE_COLOR = 0xFF333338.toInt()
private const val GLYPH_DOT_COLOR = 0xFFD71921.toInt()

/** Bitmaps above this are scaled down; the launcher stretches them back to the widget's bounds. */
private const val MAX_BITMAP_BYTES = 2_500_000f

/**
 * The widget's size in dp as the launcher last reported it (portrait width x portrait height),
 * or [fallbackWidthDp] x [fallbackHeightDp] before the launcher has reported one.
 */
fun widgetSizeDp(manager: AppWidgetManager, appWidgetId: Int, fallbackWidthDp: Int, fallbackHeightDp: Int): Pair<Int, Int> {
    val options = manager.getAppWidgetOptions(appWidgetId)
    val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    return if (width > 0 && height > 0) width to height else fallbackWidthDp to fallbackHeightDp
}

/**
 * Draws the card behind a home-screen widget with the config's corner radius, dot grid and glyph
 * border. RemoteViews can't change a drawable's corner radius or stroke color at runtime on
 * API < 31, so the card is a bitmap that matches NothingGlassCard in the preview.
 */
fun renderWidgetBackground(context: Context, config: NothingWidgetConfig?, widthDp: Int, heightDp: Int): Bitmap {
    val density = context.resources.displayMetrics.density
    val scale = min(density, sqrt(MAX_BITMAP_BYTES / (4f * widthDp * heightDp)))
    val width = (widthDp * scale).roundToInt().coerceAtLeast(1)
    val height = (heightDp * scale).roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Android 12+ launchers clip every widget to the system corner radius. A smaller radius
    // can't show, and drawing one would let the clip cut through the border, so never go below it.
    val systemRadiusDp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.resources.getDimension(android.R.dimen.system_app_widget_background_radius) / density
    } else 0f
    val radius = maxOf((config?.cornerRadiusDp ?: 24).toFloat(), systemRadiusDp) * scale
    val strokeWidth = 1f * scale
    val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val card = Path().apply { addRoundRect(bounds, radius, radius, Path.Direction.CW) }

    canvas.drawPath(card, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARD_COLOR })

    if (config?.showDotMatrixBackground != false) {
        canvas.save()
        canvas.clipPath(card)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ColorUtils.setAlphaComponent(Color.WHITE, 13) } // 5%
        val spacing = 10f * scale
        var x = spacing / 2
        while (x < width) {
            var y = spacing / 2
            while (y < height) {
                canvas.drawCircle(x, y, 1f * scale, dotPaint)
                y += spacing
            }
            x += spacing
        }
        canvas.restore()
    }

    val glyphBorder = config?.showGlyphBorder != false
    val borderColor = if (glyphBorder) ColorUtils.setAlphaComponent(accentColorOf(config), 128) else OUTLINE_COLOR
    val inset = strokeWidth / 2
    canvas.drawRoundRect(
        RectF(inset, inset, width - inset, height - inset), radius - inset, radius - inset,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = borderColor
        }
    )

    if (glyphBorder) {
        val dotRadius = 3f * scale
        canvas.drawCircle(
            width - (12f * scale) - dotRadius, (12f * scale) + dotRadius, dotRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GLYPH_DOT_COLOR }
        )
    }
    return bitmap
}
