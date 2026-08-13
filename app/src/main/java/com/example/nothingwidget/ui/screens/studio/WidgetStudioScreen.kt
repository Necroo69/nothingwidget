package com.example.nothingwidget.ui.screens.studio

import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.WidgetSize
import com.example.nothingwidget.domain.model.WidgetType
import com.example.nothingwidget.ui.components.InteractiveWidgetView
import com.example.nothingwidget.ui.components.NothingHeader
import com.example.nothingwidget.ui.theme.NothingDotFontFamily
import com.example.nothingwidget.ui.theme.NothingRed

@Composable
fun WidgetStudioScreen(
    viewModel: WidgetStudioViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.builderState.collectAsStateWithLifecycle()

    val colors = listOf("#D71921", "#FFFFFF", "#FFD600", "#00E676")

    val tempPreviewConfig = NothingWidgetConfig(
        id = "preview_temp",
        type = state.selectedType,
        size = state.selectedSize,
        title = state.customTitle,
        accentColorHex = state.accentHex,
        cornerRadiusDp = state.cornerRadius,
        showDotMatrixBackground = true,
        showGlyphBorder = true,
        customSubtitle = "CUSTOM CARD BUILDER"
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "CARD BUILDER STUDIO",
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
                .testTag("studio_builder_scroll"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card
            Text(
                text = "BUILDER CANVAS PREVIEW",
                color = NothingRed,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            InteractiveWidgetView(
                config = tempPreviewConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Select Base Widget Engine
            Text(
                text = "SELECT BASE ENGINE",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WidgetType.entries.toTypedArray()) { type ->
                    val isSelected = type == state.selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NothingRed else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) NothingRed else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setWidgetType(type) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = type.displayName.uppercase(),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontFamily = NothingDotFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Select Size Span
            Text(
                text = "SELECT WIDGET SPAN",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WidgetSize.entries.forEach { size ->
                    val isSelected = size == state.selectedSize
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NothingRed else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) NothingRed else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setWidgetSize(size) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = size.label.split(" ")[0],
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontFamily = NothingDotFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Custom Title Field
            Text(
                text = "CUSTOM CARD TITLE",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = state.customTitle,
                onValueChange = { viewModel.setCustomTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NothingRed,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Accent Color Selector
            Text(
                text = "ACCENT COLOR",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEach { hex ->
                    val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { NothingRed }
                    val isSelected = hex.equals(state.accentHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .minimumInteractiveComponentSize()
                            .semantics { contentDescription = "Select Color $hex" }
                            .clip(CircleShape)
                            .background(parsed)
                            .border(2.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                            .clickable(onClickLabel = "Select Color") { viewModel.setAccentHex(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = if (hex == "#FFFFFF") Color.Black else Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create & Deploy Button
            Button(
                onClick = {
                    viewModel.buildAndSaveWidget {
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_studio_card_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Build Widget")
                    Text("GENERATE & DEPLOY WIDGET", fontSize = 14.sp, fontFamily = NothingDotFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
