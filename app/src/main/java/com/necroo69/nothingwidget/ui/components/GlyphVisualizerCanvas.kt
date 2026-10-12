package com.necroo69.nothingwidget.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GlyphVisualizerCanvas(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    topRingIntensity: Float = 1.0f,
    cameraRingIntensity: Float = 0.8f,
    diagonalStripIntensity: Float = 0.6f,
    bottomExclamationIntensity: Float = 1.0f,
    glyphColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glyph_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = 6.dp.toPx()

        val activePulse = if (isAnimating) pulseAnim else 1.0f

        // 1. Camera Module Ring (Top Left)
        val camCenter = Offset(w * 0.3f, h * 0.22f)
        val camRadius = w * 0.16f
        val camColor = glyphColor.copy(alpha = (cameraRingIntensity * activePulse).coerceIn(0.1f, 1f))

        drawCircle(
            color = camColor,
            center = camCenter,
            radius = camRadius,
            style = Stroke(width = strokeWidthPx)
        )

        // 2. Top Right Arc
        val topArcCenter = Offset(w * 0.68f, h * 0.22f)
        val topArcRadius = w * 0.14f
        val topArcColor = glyphColor.copy(alpha = (topRingIntensity * activePulse).coerceIn(0.1f, 1f))

        drawArc(
            color = topArcColor,
            startAngle = 220f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(topArcCenter.x - topArcRadius, topArcCenter.y - topArcRadius),
            size = Size(topArcRadius * 2, topArcRadius * 2),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // 3. Diagonal Slash Strip
        val slashPath = Path().apply {
            moveTo(w * 0.72f, h * 0.38f)
            lineTo(w * 0.28f, h * 0.62f)
        }
        val slashColor = glyphColor.copy(alpha = (diagonalStripIntensity * activePulse).coerceIn(0.1f, 1f))
        drawPath(
            path = slashPath,
            color = slashColor,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // 4. Bottom Central Exclamation Mark (Vertical Line + Dot)
        val bottomLineColor = glyphColor.copy(alpha = (bottomExclamationIntensity * activePulse).coerceIn(0.1f, 1f))
        val linePath = Path().apply {
            moveTo(w * 0.5f, h * 0.68f)
            lineTo(w * 0.5f, h * 0.82f)
        }
        drawPath(
            path = linePath,
            color = bottomLineColor,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Exclamation Dot
        drawCircle(
            color = bottomLineColor,
            center = Offset(w * 0.5f, h * 0.88f),
            radius = strokeWidthPx * 0.8f
        )
    }
}
