package com.necroo69.nothingwidget.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun NothingWidgetsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorSchemeColors else LightColorSchemeColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography,
        content = content
    )
}

