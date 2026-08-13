package com.example.nothingwidget.ui.screens.customizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.nothingwidget.ui.components.InteractiveWidgetView
import com.example.nothingwidget.ui.components.NothingHeader
import com.example.nothingwidget.ui.theme.NothingDotFontFamily
import com.example.nothingwidget.ui.theme.NothingRed

@Composable
fun WidgetCustomizerScreen(
    widgetId: String,
    viewModel: WidgetCustomizerViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(widgetId) {
        viewModel.loadWidget(widgetId)
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    val configState by viewModel.config.collectAsStateWithLifecycle()
    val config = configState

    val colorOptions = listOf(
        "#D71921" to "Nothing Red",
        "#FFFFFF" to "Stark White",
        "#FFD600" to "Cyber Yellow",
        "#00E676" to "Matrix Green",
        "#00E5FF" to "Cyan Glyph"
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
                    text = "CUSTOMIZER STUDIO",
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
        if (config != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("widget_customizer_scroll"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Interactive Preview Header
                Text(
                    text = "LIVE INTERACTIVE PREVIEW",
                    color = NothingRed,
                    fontSize = 11.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                InteractiveWidgetView(
                    config = config,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Customization Controls
                Text(
                    text = "ACCENT COLOR PALETTE",
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
                    colorOptions.forEach { (hex, name) ->
                        val parsed = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { NothingRed }
                        val isSelected = config.accentColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .minimumInteractiveComponentSize()
                                .semantics { contentDescription = "Select Color $name" }
                                .clip(CircleShape)
                                .background(parsed)
                                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                .clickable(onClickLabel = "Select Color") { viewModel.updateAccentColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = if (hex == "#FFFFFF") Color.Black else Color.White)
                            }
                        }
                    }
                }

                // Corner Radius Slider
                Text(
                    text = "CORNER RADIUS: ${config.cornerRadiusDp} DP",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Slider(
                    value = config.cornerRadiusDp.toFloat(),
                    onValueChange = { viewModel.updateCornerRadius(it.toInt()) },
                    valueRange = 8f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = NothingRed,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Switches Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOT MATRIX GRID TEXTURE",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontFamily = NothingDotFontFamily
                    )
                    Switch(
                        checked = config.showDotMatrixBackground,
                        onCheckedChange = { viewModel.toggleDotBackground() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NothingRed)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GLYPH RED ACCENT CORNER",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontFamily = NothingDotFontFamily
                    )
                    Switch(
                        checked = config.showGlyphBorder,
                        onCheckedChange = { viewModel.toggleGlyphBorder() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NothingRed)
                    )
                }

                // Custom Label
                Text(
                    text = "CUSTOM SUBTITLE LABEL",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = config.customSubtitle,
                    onValueChange = { viewModel.updateCustomSubtitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. STUDIO MATRIX", fontFamily = NothingDotFontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Action Button
                Button(
                    onClick = {
                        viewModel.saveConfig {
                            triggerWidgetUpdate(context, config.type)
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_customizer_btn"),
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
                        Icon(Icons.Default.Save, contentDescription = "Save Widget Preset")
                        Text("SAVE WIDGET PRESET", fontSize = 14.sp, fontFamily = NothingDotFontFamily, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

fun triggerWidgetUpdate(context: android.content.Context, type: com.example.nothingwidget.domain.model.WidgetType) {
    val providerClass = when (type) {
        com.example.nothingwidget.domain.model.WidgetType.DIGITAL_CLOCK, 
        com.example.nothingwidget.domain.model.WidgetType.ANALOG_CLOCK, 
        com.example.nothingwidget.domain.model.WidgetType.WORLD_CLOCK -> 
            try { Class.forName("com.example.nothingwidget.widgets.ClockWidget") } catch (e: Exception) { null }
        com.example.nothingwidget.domain.model.WidgetType.WEATHER -> 
            try { Class.forName("com.example.nothingwidget.widgets.WeatherWidget") } catch (e: Exception) { null }
        com.example.nothingwidget.domain.model.WidgetType.BATTERY_CIRCLE -> 
            try { Class.forName("com.example.nothingwidget.widgets.BatteryWidget") } catch (e: Exception) { null }
        com.example.nothingwidget.domain.model.WidgetType.DATE -> 
            try { Class.forName("com.example.nothingwidget.widgets.DateWidget") } catch (e: Exception) { null }
        else -> null
    }

    if (providerClass != null) {
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val componentName = android.content.ComponentName(context, providerClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, providerClass).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
