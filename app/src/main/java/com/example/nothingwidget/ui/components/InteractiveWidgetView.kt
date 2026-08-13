package com.example.nothingwidget.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nothingwidget.domain.model.AudioTrackState
import com.example.nothingwidget.domain.model.BatteryInfo
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.QuickToggleState
import com.example.nothingwidget.domain.model.WeatherInfo
import com.example.nothingwidget.domain.model.WidgetType
import com.example.nothingwidget.ui.theme.NothingDotFontFamily
import com.example.nothingwidget.ui.theme.NothingRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveWidgetView(
    config: NothingWidgetConfig,
    weatherInfo: WeatherInfo = WeatherInfo(),
    batteryInfo: BatteryInfo = BatteryInfo(),
    toggleState: QuickToggleState = QuickToggleState(),
    audioState: AudioTrackState = AudioTrackState(),
    stepsCount: Int = 8420,
    noteText: String = "Meeting at 3:00 PM in Studio B",
    onToggleClick: ((String) -> Unit)? = null,
    onAudioPlayPause: (() -> Unit)? = null,
    onAudioNext: (() -> Unit)? = null,
    onAudioPrev: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val parseAccent = try {
        Color(android.graphics.Color.parseColor(config.accentColorHex))
    } catch (_: Exception) {
        NothingRed
    }

    NothingGlassCard(
        modifier = modifier,
        cornerRadius = config.cornerRadiusDp.dp,
        showRedDot = config.showGlyphBorder,
        showDotGrid = config.showDotMatrixBackground,
        borderColor = if (config.showGlyphBorder) parseAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            when (config.type) {
                WidgetType.DIGITAL_CLOCK -> DigitalClockWidget(accentColor = parseAccent, subtitle = config.customSubtitle)
                WidgetType.ANALOG_CLOCK -> AnalogClockWidget(accentColor = parseAccent)
                WidgetType.WEATHER -> WeatherWidgetView(weatherInfo = weatherInfo, accentColor = parseAccent)
                WidgetType.BATTERY_CIRCLE -> BatteryWidgetView(batteryInfo = batteryInfo, accentColor = parseAccent)
                WidgetType.STEP_TRACKER -> StepTrackerWidgetView(steps = stepsCount, accentColor = parseAccent)
                WidgetType.QUICK_TOGGLES -> QuickTogglesWidgetView(state = toggleState, accentColor = parseAccent, onToggleClick = onToggleClick)
                WidgetType.AUDIO_PLAYER -> AudioPlayerWidgetView(audioState = audioState, accentColor = parseAccent, onPlayPause = onAudioPlayPause, onNext = onAudioNext, onPrev = onAudioPrev)
                WidgetType.WORLD_CLOCK -> WorldClockWidgetView(accentColor = parseAccent)
                WidgetType.QUICK_NOTE -> QuickNoteWidgetView(noteText = noteText, accentColor = parseAccent)
                WidgetType.DATE -> DateWidgetView(accentColor = parseAccent, subtitle = config.customSubtitle)
            }
        }
    }
}

@Composable
fun DigitalClockWidget(accentColor: Color, subtitle: String) {
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val seconds = SimpleDateFormat(":ss", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()).uppercase()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentDate,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentTime,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 42.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = seconds,
                color = accentColor,
                fontSize = 18.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (subtitle.isNotBlank()) subtitle.uppercase() else "NOTHING OS TIME ENGINE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontFamily = NothingDotFontFamily,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun DateWidgetView(accentColor: Color, subtitle: String) {
    val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    val currentMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()).uppercase()
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).uppercase()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayOfWeek,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = currentDay,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 42.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = currentMonth,
                color = accentColor,
                fontSize = 18.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (subtitle.isNotBlank()) subtitle.uppercase() else "NOTHING OS DATE ENGINE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontFamily = NothingDotFontFamily,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun AnalogClockWidget(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "clock_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 4.dp.toPx()

                // Outer Glyph Ring Dots
                for (i in 0 until 12) {
                    val angleRad = Math.toRadians((i * 30).toDouble())
                    val dotX = center.x + (radius - 6.dp.toPx()) * cos(angleRad).toFloat()
                    val dotY = center.y + (radius - 6.dp.toPx()) * sin(angleRad).toFloat()
                    drawCircle(
                        color = if (i % 3 == 0) accentColor else Color.White.copy(alpha = 0.4f),
                        radius = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // Clock Hands
                val minuteAngleRad = Math.toRadians((sweepAngle - 90).toDouble())
                val hourAngleRad = Math.toRadians(((sweepAngle / 12) - 90).toDouble())

                // Hour Hand
                val hourX = center.x + (radius * 0.45f) * cos(hourAngleRad).toFloat()
                val hourY = center.y + (radius * 0.45f) * sin(hourAngleRad).toFloat()
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(hourX, hourY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minute Hand
                val minX = center.x + (radius * 0.75f) * cos(minuteAngleRad).toFloat()
                val minY = center.y + (radius * 0.75f) * sin(minuteAngleRad).toFloat()
                drawLine(
                    color = accentColor,
                    start = center,
                    end = Offset(minX, minY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center Pin
                drawCircle(color = accentColor, radius = 4.dp.toPx(), center = center)
            }
        }
    }
}

@Composable
fun WeatherWidgetView(weatherInfo: WeatherInfo, accentColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weatherInfo.cityName.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = weatherInfo.condition.label.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = NothingDotFontFamily
                )
            }
            Text(
                text = "${weatherInfo.temperatureC?.toString() ?: "--"}°",
                color = accentColor,
                fontSize = 32.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weatherInfo.forecastHourly.take(4).forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.timeLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = NothingDotFontFamily
                    )
                    Text(
                        text = item.condition.dotSymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${item.tempC?.toString() ?: "--"}°",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontFamily = NothingDotFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryWidgetView(batteryInfo: BatteryInfo, accentColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BATTERY",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily,
                letterSpacing = 1.sp
            )
            Text(
                text = "${batteryInfo.percentage}%",
                color = accentColor,
                fontSize = 28.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { batteryInfo.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EAR (2): 95%",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily
            )
            Text(
                text = "${batteryInfo.estimatedHoursRemaining}h REMAINING",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily
            )
        }
    }
}

@Composable
fun StepTrackerWidgetView(steps: Int, accentColor: Color) {
    val progress = (steps / 10000f).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DAILY STEPS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily,
                letterSpacing = 1.sp
            )
            Text(
                text = "$steps",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "GOAL: 10,000",
                color = accentColor,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily
            )
        }

        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 6.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(strokePx)
                )
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickTogglesWidgetView(
    state: QuickToggleState,
    accentColor: Color,
    onToggleClick: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleItem("WIFI", state.isWifiOn, Icons.Default.Wifi, accentColor) { onToggleClick?.invoke("wifi") }
        ToggleItem("BT", state.isBluetoothOn, Icons.Default.Bluetooth, accentColor) { onToggleClick?.invoke("bluetooth") }
        ToggleItem("FLASH", state.isFlashlightOn, Icons.Default.FlashlightOn, accentColor) { onToggleClick?.invoke("flashlight") }
        ToggleItem("AIR", state.isAirplaneModeOn, Icons.Default.Air, accentColor) { onToggleClick?.invoke("airplane") }
    }
}

@Composable
fun ToggleItem(
    label: String,
    isOn: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClickLabel = "Toggle $label") { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isOn) accentColor else MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, if (isOn) accentColor else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isOn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontFamily = NothingDotFontFamily
        )
    }
}

@Composable
fun AudioPlayerWidgetView(
    audioState: AudioTrackState,
    accentColor: Color,
    onPlayPause: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onPrev: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioState.title.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = audioState.artist.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = NothingDotFontFamily,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music",
                tint = accentColor
            )
        }

        // Spectrum Waveform Simulation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            for (freq in audioState.spectrumFrequencies) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(if (audioState.isPlaying) freq else 0.2f)
                        .background(if (audioState.isPlaying) accentColor else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onPrev?.invoke() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(
                onClick = { onPlayPause?.invoke() },
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor, CircleShape)
            ) {
                Icon(
                    imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "PlayPause",
                    tint = Color.White
                )
            }
            IconButton(onClick = { onNext?.invoke() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun WorldClockWidgetView(accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TOKYO",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily
            )
            Text(
                text = "21:45",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(MaterialTheme.colorScheme.outline))
        Column {
            Text(
                text = "LONDON",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily
            )
            Text(
                text = "12:45",
                color = accentColor,
                fontSize = 18.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(MaterialTheme.colorScheme.outline))
        Column {
            Text(
                text = "NEW YORK",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily
            )
            Text(
                text = "07:45",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickNoteWidgetView(noteText: String, accentColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STICKY NOTE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = NothingDotFontFamily,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
        }

        Text(
            text = noteText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontFamily = NothingDotFontFamily,
            lineHeight = 20.sp,
            maxLines = 3
        )

        Text(
            text = "TAP TO EDIT NOTE",
            color = accentColor,
            fontSize = 9.sp,
            fontFamily = NothingDotFontFamily
        )
    }
}
