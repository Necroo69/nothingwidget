package com.necroo69.nothingwidget.ui.theme

import androidx.compose.ui.graphics.Color

// Nothing OS Signature Palette
val PitchBlack = Color(0xFF000000)
val CharcoalDark = Color(0xFF0F0F0F)
val GlassSurface = Color(0xFF18181B)
val GlassSurfaceElevated = Color(0xFF242428)
val GlassBorder = Color(0xFF333338)

val StarkWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFE4E4E7)
val MutedGray = Color(0xFF909096)
val SubtleDarkGray = Color(0xFF4A4A50)

val NothingRed = Color(0xFFD71921)
val NothingRedGlow = Color(0x33D71921)
val CyberYellow = Color(0xFFFFD600)
val MatrixGreen = Color(0xFF00E676)

val DarkColorSchemeColors = androidx.compose.material3.darkColorScheme(
    primary = StarkWhite,
    onPrimary = PitchBlack,
    primaryContainer = GlassSurfaceElevated,
    onPrimaryContainer = StarkWhite,
    secondary = NothingRed,
    onSecondary = StarkWhite,
    tertiary = MutedGray,
    background = PitchBlack,
    onBackground = StarkWhite,
    surface = GlassSurface,
    onSurface = StarkWhite,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = MutedGray,
    outline = GlassBorder,
    outlineVariant = SubtleDarkGray
)

val LightColorSchemeColors = androidx.compose.material3.lightColorScheme(
    primary = PitchBlack,
    onPrimary = StarkWhite,
    primaryContainer = OffWhite,
    onPrimaryContainer = PitchBlack,
    secondary = NothingRed,
    onSecondary = StarkWhite,
    tertiary = SubtleDarkGray,
    background = OffWhite,
    onBackground = PitchBlack,
    surface = StarkWhite,
    onSurface = PitchBlack,
    surfaceVariant = OffWhite,
    onSurfaceVariant = SubtleDarkGray,
    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFD4D4D8)
)

