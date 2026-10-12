package com.necroo69.nothingwidget.ui.screens.glyph

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroo69.nothingwidget.ui.components.GlyphVisualizerCanvas
import com.necroo69.nothingwidget.ui.components.NothingGlassCard
import com.necroo69.nothingwidget.ui.theme.NothingDotFontFamily
import com.necroo69.nothingwidget.ui.theme.NothingRed

@Composable
fun GlyphStudioScreen(
    viewModel: GlyphStudioViewModel,
    onBackClick: () -> Unit
) {
    val pattern by viewModel.selectedPattern.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "GLYPH MATRIX STUDIO",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("glyph_studio_scroll"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glyph Simulator Card
            NothingGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                cornerRadius = 24.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlyphVisualizerCanvas(
                        isAnimating = true,
                        topRingIntensity = pattern.topRingIntensity,
                        cameraRingIntensity = pattern.cameraRingIntensity,
                        diagonalStripIntensity = pattern.diagonalStripIntensity,
                        bottomExclamationIntensity = pattern.bottomExclamationIntensity,
                        glyphColor = Color.White
                    )
                }
            }

            // Pattern Selector Pills
            Text(
                text = "GLYPH LIGHTING PATTERNS",
                color = NothingRed,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availablePatterns) { item ->
                    val isSelected = item.id == pattern.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NothingRed else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) NothingRed else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectPattern(item) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontFamily = NothingDotFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.description,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                fontFamily = NothingDotFontFamily
                            )
                        }
                    }
                }
            }

            // Individual LED Strip Sliders
            Text(
                text = "LED STRIP INTENSITY CONTROLS",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IntensityControlItem("TOP RING ARC", pattern.topRingIntensity) { viewModel.updateTopRing(it) }
            IntensityControlItem("CAMERA MODULE RING", pattern.cameraRingIntensity) { viewModel.updateCameraRing(it) }
            IntensityControlItem("DIAGONAL STRIP", pattern.diagonalStripIntensity) { viewModel.updateDiagonalStrip(it) }
            IntensityControlItem("BOTTOM EXCLAMATION DOT", pattern.bottomExclamationIntensity) { viewModel.updateBottomExclamation(it) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IntensityControlItem(label: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily
            )
            Text(
                text = "${(value * 100).toInt()}%",
                color = NothingRed,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = NothingRed,
                activeTrackColor = NothingRed,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
