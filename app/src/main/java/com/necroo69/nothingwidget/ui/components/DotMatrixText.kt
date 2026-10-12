package com.necroo69.nothingwidget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroo69.nothingwidget.ui.theme.NothingDotFontFamily

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 28.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    letterSpacing: TextUnit = 2.sp
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = NothingDotFontFamily,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing
    )
}

@Composable
fun DotGridBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.08f),
    dotRadius: Dp = 1.dp,
    spacing: Dp = 12.dp
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val radiusPx = dotRadius.toPx()
        val spacingPx = spacing.toPx()
        val columns = (size.width / spacingPx).toInt()
        val rows = (size.height / spacingPx).toInt()

        for (x in 0..columns) {
            for (y in 0..rows) {
                drawCircle(
                    color = dotColor,
                    radius = radiusPx,
                    center = Offset(x * spacingPx + spacingPx / 2, y * spacingPx + spacingPx / 2)
                )
            }
        }
    }
}
