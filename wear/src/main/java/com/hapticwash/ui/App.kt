package com.hapticwash.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.hapticwash.R
import com.hapticwash.ui.theme.Five

/** True while ambient mode has the screen and a Wash is running, for the Wash screen beneath to stand still. */
internal val LocalAmbient = compositionLocalOf { false }

/**
 * Max depth 2 (§4.1.5): Start → In-progress, which shows its own Summary when the Wash ends, Start → History → Summary, Start → Settings.
 * [startRequests] is a provider, not an Int: the nav graph keeps its destination lambdas, so a captured
 * Int would stay at its first value and a Tile tap on an already-open Start would be missed.
 */
@Composable
fun App(startRequests: () -> Int = { 0 }, openRequest: () -> Long? = { null }, onOpened: () -> Unit = {}, ambient: () -> Ambient? = { null }) {
    val nav = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val running by LocalWashControls.current.status.collectAsState()
    val ambientNow = ambient().takeIf { running.running }
    var answered by rememberSaveable { mutableIntStateOf(0) }
    // Shown on Start after auto-end cancels a Wash, until the user acts.
    var cancelledNotice by rememberSaveable { mutableStateOf(false) }
    // A Tile or complication tap: back to Start, which then starts (or rejoins) the Wash.
    LaunchedEffect(startRequests()) { if (startRequests() > answered) nav.popBackStack(Routes.START, inclusive = false) }
    // A Wash that began without a tap on Start (a Twist): show it, from whatever screen the app was left on, over Start.
    // Each Wash screen claims the Wash it shows. A tap has already opened one, which claims this Wash (a Step guide
    // opened from it stays open); one still showing an earlier Wash's Summary is stale and gives way to a fresh one.
    LaunchedEffect(running.startedAtEpochMs) {
        if (!running.running) return@LaunchedEffect
        val started = running.startedAtEpochMs
        val wash = runCatching { nav.getBackStackEntry(Routes.WASH) }.getOrNull()
        val claimed = wash?.savedStateHandle?.get<Long>(WASH_CLAIM)
        if (wash != null && (claimed == null || claimed == started)) {
            wash.savedStateHandle[WASH_CLAIM] = started
            return@LaunchedEffect
        }
        nav.popBackStack(Routes.START, inclusive = false)
        nav.navigate(Routes.WASH)
        nav.currentBackStackEntry?.savedStateHandle?.set(WASH_CLAIM, started)
    }
    // A tap on the Tile's ring or words: that Wash's Summary, over Start, as if opened from History.
    LaunchedEffect(openRequest()) {
        val id = openRequest() ?: return@LaunchedEffect
        nav.popBackStack(Routes.START, inclusive = false)
        nav.navigate(Routes.summary(id))
        onOpened()
    }
    // The Wash screen stays composed under ambient mode, so waking returns to it as it was; ambient only covers it.
    Box {
        CompositionLocalProvider(LocalAmbient provides (ambientNow != null), LocalStartOrigin provides remember { StartOrigin() }) {
            SwipeDismissableNavHost(nav, startDestination = Routes.START) {
                composable(Routes.START) {
                    StartScreen(
                        onWash = { cancelledNotice = false; nav.navigate(Routes.WASH) },
                        onHistory = { cancelledNotice = false; nav.navigate(Routes.HISTORY) },
                        onSettings = { cancelledNotice = false; nav.navigate(Routes.SETTINGS) },
                        cancelledNotice = cancelledNotice,
                        autoStart = startRequests() > answered,
                        onAutoStarted = { answered = startRequests() },
                    )
                }
                composable(Routes.WASH) { WashScreen(onGuide = { nav.navigate(Routes.guide(it)) }, onCancelled = {
                    nav.popBackStack(Routes.START, inclusive = false)
                    cancelledNotice = true
                }) }
                composable(Routes.HISTORY) { HistoryScreen(onOpen = { nav.navigate(Routes.summary(it)) }) }
                composable(Routes.SUMMARY) { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull(), onGuide = { l -> nav.navigate(Routes.guide(l)) }) }
                composable(Routes.GUIDE) { GuideScreen(it.arguments?.getString("label")?.toIntOrNull()) }
                composable(Routes.SETTINGS) { SettingsScreen(onWash = { nav.navigate(Routes.SETTINGS_WASH) }, onStart = { nav.navigate(Routes.SETTINGS_START) }, onCredits = { nav.navigate(Routes.CREDITS) }) }
                composable(Routes.SETTINGS_WASH) { WashSettingsScreen() }
                composable(Routes.SETTINGS_START) { StartSettingsScreen() }
                composable(Routes.CREDITS) { CreditsScreen() }
            }
        }
        ambientNow?.let { AmbientWash(shift = if (it.burnIn) (it.updates % 3 - 1).dp * 2 else 0.dp) }
    }
}

/**
 * What ambient mode shows during a Wash: outlines on black, nothing that moves or counts. The system redraws
 * ambient once a minute, so a time here would be wrong; a Wash is about a minute long. [shift] moves the outline
 * about on displays that need burn-in protection.
 */
@Composable
private fun AmbientWash(shift: Dp) {
    val line = with(LocalDensity.current) { 1.5.dp.toPx() }
    val drop = remember { PathParser().parsePathString(DROP_PATH).toPath() }
    ScreenScaffold {
        Box(
            Modifier.fillMaxSize().background(Color.Black).offset(shift, shift).drawBehind {
                // The live ring's circle and its 60° gap at the top, as a thin outline.
                edgeArc(SolidColor(Five.AmbientLine), 1f, line, LIVE_STROKE.toPx())
            },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // The drop as an outline: nothing filled, so nothing burns in.
                Canvas(Modifier.size(22.dp)) {
                    scale(size.width / 24f, size.height / 24f, Offset.Zero) { drawPath(drop, Five.AmbientLine, style = Stroke(1.4.dp.toPx() * 24f / size.width)) }
                }
                Text(stringResource(R.string.ambient_washing), color = Five.AmbientText, style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium))
            }
        }
    }
}

/** The app's drop glyph (`ic_wash`), as path data on a 24-unit grid, for the outlines ambient mode draws. */
/** Saved-state key on a Wash screen: the start time of the Wash it shows. */
private const val WASH_CLAIM = "wash_claim"

private const val DROP_PATH = "M12,2C6.67,6.55 4,10.48 4,13.8C4,18.78 7.8,22 12,22s8,-3.22 8,-8.2C20,10.48 17.33,6.55 12,2z"

@WearPreviewDevices
@Composable
private fun AmbientPreview() = PreviewFrame { AmbientWash(shift = 0.dp) }
