package com.necroo69.nothingwidget.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroo69.nothingwidget.ui.components.DotGridBackground
import com.necroo69.nothingwidget.ui.screens.settings.SettingsViewModel
import com.necroo69.nothingwidget.ui.screens.settings.WeatherLocationPicker
import com.necroo69.nothingwidget.ui.theme.NothingDotFontFamily
import com.necroo69.nothingwidget.ui.theme.NothingRed
import kotlinx.coroutines.launch

private data class OnboardingPage(val step: String, val title: String, val body: List<String>)

private val pages = listOf(
    OnboardingPage(
        "01", "WIDGETS, NOTHING-STYLE",
        listOf(
            "Dot-matrix clock, date, battery and weather widgets for any Android phone.",
            "No ads, and no background work for the clocks: your launcher ticks them."
        )
    ),
    OnboardingPage(
        "02", "MAKE IT YOURS",
        listOf(
            "In the gallery, tap EDIT on a widget to change its accent color, corners, dot grid and label.",
            "Already placed one? Tap it on your home screen to edit just that widget."
        )
    ),
    OnboardingPage(
        "03", "ADD TO YOUR HOME SCREEN",
        listOf(
            "1. Tap ADD TO HOME on a widget in the gallery, then confirm.",
            "2. Or long-press an empty spot on your home screen, open Widgets, find Nothing Widgets and drag one into place.",
            "Widgets marked IN-APP ONLY can't be placed yet."
        )
    ),
    OnboardingPage(
        "04", "WEATHER",
        listOf(
            "Use your approximate location, or pick a city. Your location is only read while the app is open.",
            "You can skip this and set it later in Settings."
        )
    )
)

/**
 * First-run introduction, also reachable from Settings ("HOW TO ADD WIDGETS"). The last page
 * asks for a weather location using the same picker as Settings.
 */
@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel,
    onFinished: () -> Unit
) {
    val state by settingsViewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun finish() {
        settingsViewModel.completeOnboarding()
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DotGridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isLastPage) {
                    TextButton(onClick = ::finish, modifier = Modifier.testTag("onboarding_skip")) {
                        Text("SKIP", fontFamily = NothingDotFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_pager")
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(page.step, color = NothingRed, fontSize = 14.sp, fontFamily = NothingDotFontFamily, fontWeight = FontWeight.Bold)
                    Text(
                        text = page.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontFamily = NothingDotFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    page.body.forEach { line ->
                        Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp, fontFamily = NothingDotFontFamily)
                    }
                    if (index == pages.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                        WeatherLocationPicker(state = state, viewModel = settingsViewModel)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.semantics { contentDescription = "Page ${pagerState.currentPage + 1} of ${pages.size}" }
                ) {
                    pages.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == pagerState.currentPage) 10.dp else 6.dp)
                                .background(
                                    if (i == pagerState.currentPage) NothingRed else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )
                    }
                }
                Button(
                    onClick = {
                        if (isLastPage) finish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("onboarding_next"),
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isLastPage) "GET STARTED" else "NEXT",
                        fontSize = 14.sp,
                        fontFamily = NothingDotFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
