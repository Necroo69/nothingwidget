package com.necroo69.nothingwidget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.necroo69.nothingwidget.ui.theme.NothingRed

@Composable
fun NothingGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showRedDot: Boolean = true,
    showDotGrid: Boolean = true,
    onClick: (() -> Unit)? = null,
    testTag: String = "nothing_glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .testTag(testTag)
        .clip(shape)
        .background(backgroundColor)
        .border(borderWidth, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(modifier = cardModifier) {
        if (showDotGrid) {
            DotGridBackground(
                dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                spacing = 10.dp
            )
        }

        if (showRedDot) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, end = 12.dp)
                    .size(6.dp)
                    .background(NothingRed, CircleShape)
                    .align(Alignment.TopEnd)
            )
        }

        content()
    }
}
