package com.example.nothingwidget.ui.screens.gallery

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nothingwidget.R
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.WidgetType
import com.example.nothingwidget.ui.components.InteractiveWidgetView
import com.example.nothingwidget.ui.components.NothingGlassCard
import com.example.nothingwidget.ui.components.NothingHeader
import com.example.nothingwidget.ui.theme.NothingDotFontFamily
import com.example.nothingwidget.ui.theme.NothingRed

@Composable
fun WidgetGalleryScreen(
    viewModel: WidgetGalleryViewModel,
    onNavigateToCustomizer: (String) -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToGlyph: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val categories = listOf("All", "Clock", "Weather", "System", "Fitness", "Media", "Productivity")

    Scaffold(
        topBar = {
            NothingHeader(
                title = "NOTHING WIDGETS",
                subtitle = "v2.5 OS ENGINE",
                trailingIcon = Icons.Default.Tune,
                onTrailingClick = onNavigateToSettings
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .testTag("widget_gallery_column"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = R.drawable.nothing_hero_banner_1785844485499,
                        contentDescription = "Nothing Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(NothingRed, CircleShape))
                            Text(
                                text = "MONOCHROME SUITE",
                                color = NothingRed,
                                fontSize = 11.sp,
                                fontFamily = NothingDotFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "CUSTOMIZE YOUR HOME SCREEN",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 18.sp,
                            fontFamily = NothingDotFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToStudio,
                        modifier = Modifier.weight(1f).testTag("studio_builder_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Card Builder", modifier = Modifier.size(16.dp))
                            Text("CARD BUILDER", fontSize = 12.sp, fontFamily = NothingDotFontFamily)
                        }
                    }

                    Button(
                        onClick = onNavigateToGlyph,
                        modifier = Modifier.weight(1f).testTag("glyph_studio_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(NothingRed, CircleShape))
                            Text("GLYPH STUDIO", fontSize = 12.sp, fontFamily = NothingDotFontFamily)
                        }
                    }
                }
            }

            // Category Filter Pills
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == state.selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) NothingRed else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) NothingRed else MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category.uppercase(),
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontFamily = NothingDotFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Widget Cards
            items(state.widgets, key = { it.id }) { widget ->
                GalleryWidgetCardItem(
                    widget = widget,
                    state = state,
                    onCustomizeClick = { onNavigateToCustomizer(widget.id) },
                    onPinClick = {
                        requestPinWidget(context, widget)
                    },
                    onToggleClick = { viewModel.toggleQuickToggle(it) },
                    onAudioPlayPause = { viewModel.toggleAudioPlay() },
                    onAudioNext = { viewModel.nextAudio() },
                    onAudioPrev = { viewModel.prevAudio() }
                )
            }
        }
    }
}

@Composable
fun GalleryWidgetCardItem(
    widget: NothingWidgetConfig,
    state: GalleryUiState,
    onCustomizeClick: () -> Unit,
    onPinClick: () -> Unit,
    onToggleClick: (String) -> Unit,
    onAudioPlayPause: () -> Unit,
    onAudioNext: () -> Unit,
    onAudioPrev: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = widget.title.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${widget.size.label} • ${widget.type.category}".uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = NothingDotFontFamily
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable(onClickLabel = "Edit Widget") { onCustomizeClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Text("EDIT", fontSize = 10.sp, fontFamily = NothingDotFontFamily, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(NothingRed)
                        .clickable(onClickLabel = "Add to Home Screen") { onPinClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = "Pin", modifier = Modifier.size(12.dp), tint = androidx.compose.ui.graphics.Color.White)
                        Text("ADD TO HOME", fontSize = 10.sp, fontFamily = NothingDotFontFamily, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        InteractiveWidgetView(
            config = widget,
            weatherInfo = state.weatherInfo,
            batteryInfo = state.batteryInfo,
            toggleState = state.toggleState,
            audioState = state.audioState,
            stepsCount = state.stepCount,
            onToggleClick = onToggleClick,
            onAudioPlayPause = onAudioPlayPause,
            onAudioNext = onAudioNext,
            onAudioPrev = onAudioPrev,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

fun requestPinWidget(context: Context, config: NothingWidgetConfig) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val providerClass = when (config.type) {
            WidgetType.DIGITAL_CLOCK, WidgetType.ANALOG_CLOCK, WidgetType.WORLD_CLOCK -> Class.forName("com.example.nothingwidget.widgets.ClockWidget")
            WidgetType.WEATHER -> Class.forName("com.example.nothingwidget.widgets.WeatherWidget")
            WidgetType.BATTERY_CIRCLE -> Class.forName("com.example.nothingwidget.widgets.BatteryWidget")
            WidgetType.QUICK_TOGGLES -> Class.forName("com.example.nothingwidget.widget.NothingQuickSettingsWidgetProvider")
            WidgetType.STEP_TRACKER -> Class.forName("com.example.nothingwidget.widget.NothingStepWidgetProvider")
            WidgetType.AUDIO_PLAYER -> Class.forName("com.example.nothingwidget.widget.NothingAudioWidgetProvider")
            WidgetType.QUICK_NOTE -> Class.forName("com.example.nothingwidget.widgets.ClockWidget") // Fallback
            WidgetType.DATE -> Class.forName("com.example.nothingwidget.widgets.DateWidget")
        }

        val myProvider = ComponentName(context, providerClass)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
        } else {
            Toast.makeText(context, "Auto-pin not supported on this launcher", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Auto-pin not supported on this launcher", Toast.LENGTH_SHORT).show()
    }
}
