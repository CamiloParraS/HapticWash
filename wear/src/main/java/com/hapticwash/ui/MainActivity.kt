package com.hapticwash.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.hapticwash.R
import com.hapticwash.inference.StepModel
import com.hapticwash.session.History
import com.hapticwash.session.Settings
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashService
import com.hapticwash.session.WashSession
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Progress ring target. A UI constant: nothing in Coverage reads it. */
private const val RING_TARGET_S = 40f

/** Summary entrance: each row fades and rises over [ROW_MS], [STAGGER_MS] after the one above. */
private const val ROW_MS = 200f
private const val STAGGER_MS = 40f
private const val ENTRANCE_MS = 360 // ROW_MS + 4 × STAGGER_MS, for five rows

/** Strong ease-out: moves at once, settles gently. */
private val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HapticWashTheme { AppScaffold { App() } } }
    }
}

/** Max depth 2 (§4.1.5): Start → In-progress → Summary, Start → History → Summary, Start → Settings. */
@Composable
fun App() {
    val nav = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(nav, startDestination = "start") {
        composable("start") {
            StartScreen(onWash = { nav.navigate("wash") }, onHistory = { nav.navigate("history") }, onSettings = { nav.navigate("settings") })
        }
        composable("wash") { WashScreen(onEnd = { id -> nav.navigate("summary/$id") { popUpTo("start") } }) }
        composable("history") { HistoryScreen(onOpen = { nav.navigate("summary/$it") }) }
        composable("summary/{id}") { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull()) }
        composable("settings") { SettingsScreen() }
    }
}

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. Items shrink and fade at the round edge via [spec]. */
@Composable
private fun ListScreen(content: TransformingLazyColumnScope.(spec: TransformationSpec) -> Unit) {
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    ScreenScaffold(state) { padding ->
        TransformingLazyColumn(state = state, contentPadding = padding) { content(spec) }
    }
}

/** Full width, with the item's height following the edge transformation. Surfaces also take `SurfaceTransformation(spec)`. */
private fun TransformingLazyColumnItemScope.morph(spec: TransformationSpec) =
    Modifier.fillMaxWidth().transformedHeight(this, spec)

/** [morph] for plain content that has no surface of its own. */
private fun TransformingLazyColumnItemScope.morphContent(spec: TransformationSpec) =
    morph(spec).graphicsLayer { with(spec) { applyContainerTransformation(scrollProgress) } }

@Composable
private fun StartScreen(onWash: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    var timerOnly by remember { mutableStateOf(false) }
    fun begin() {
        if (!status.running) WashService.start(context)
        onWash()
    }
    // Asked on the first Start, not at launch; the Wash begins whatever the answer.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { begin() }
    LaunchedEffect(Unit) {
        // Loads the model off the main thread; also the release smoke check's log line.
        timerOnly = withContext(Dispatchers.Default) { StepModel.bundled(context) == null }
    }
    val (press, pressScale) = rememberPressScale()
    ListScreen { spec ->
        item { ListHeader { Text(stringResource(R.string.app_name)) } }
        item {
            Button(
                onClick = {
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) begin()
                    else askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                modifier = morph(spec).then(pressScale),
                interactionSource = press,
                transformation = SurfaceTransformation(spec),
                label = { Text(stringResource(if (status.running) R.string.session_running else R.string.start_wash)) },
            )
        }
        if (timerOnly) item { Notice(stringResource(R.string.timer_only), morphContent(spec)) }
        item { FilledTonalButton(onHistory, morph(spec), transformation = SurfaceTransformation(spec), label = { Text(stringResource(R.string.history)) }) }
        item { FilledTonalButton(onSettings, morph(spec), transformation = SurfaceTransformation(spec), label = { Text(stringResource(R.string.settings)) }) }
    }
}

/** Elapsed time, optional progress ring, Stop. Never names a Step. */
@Composable
private fun WashScreen(onEnd: (Long) -> Unit) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    val last by WashService.lastWash.collectAsState()
    val before = remember { last?.id }
    LaunchedEffect(last) { last?.takeIf { it.id != before }?.let { onEnd(it.id) } }
    val ring = remember { Settings(context).progressRing }
    var stopping by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(250)
        }
    }
    // Derived state, so the ring's progress lambda reads the live value at draw time.
    val elapsedS by remember { derivedStateOf { if (status.running) ((now - status.startedAtEpochMs) / 1000f).coerceAtLeast(0f) else 0f } }
    // The ring changes colour once it fills, so reaching the target is visible at a glance.
    val full by remember { derivedStateOf { elapsedS >= RING_TARGET_S } }
    val ringColor by animateColorAsState(if (full) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary, tween(300))
    val (press, pressScale) = rememberPressScale()
    ScreenScaffold { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // A 60° gap centred on 12 o'clock leaves TimeText clear.
            if (ring) {
                CircularProgressIndicator(
                    progress = { (elapsedS / RING_TARGET_S).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding),
                    startAngle = 300f,
                    endAngle = 240f,
                    colors = ProgressIndicatorDefaults.colors(indicatorColor = ringColor),
                )
            }
            Column(
                Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Tabular digits, so the time doesn't shift sideways as it ticks.
                Text(duration(elapsedS), style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"))
                if (status.timerOnly) Notice(stringResource(R.string.timer_only))
                // Wide: wet, soapy fingers miss small targets. Answers the tap at once, before the Wash has finished saving.
                Button(
                    onClick = { stopping = true; WashService.stop(context) },
                    modifier = Modifier.fillMaxWidth(0.8f).then(pressScale),
                    enabled = !stopping,
                    interactionSource = press,
                    label = { Text(stringResource(if (stopping) R.string.stopping else R.string.stop_wash), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                )
            }
        }
    }
}

/** A Wash's Summary: the one just ended, or one from History. */
@Composable
private fun SummaryScreen(id: Long?) {
    val context = LocalContext.current
    val wash = remember(id) {
        WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(context).read().find { it.id == id }
    }
    val steps = stringArrayResource(R.array.steps)
    val colors = MaterialTheme.colorScheme
    // Rows arrive in a short cascade as the end pulses play. Decorative: scrolling is never blocked.
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing)) }
    ListScreen { spec ->
        if (wash == null) return@ListScreen
        val shown = wash.modelVersion != null && wash.coverageShown
        // The takeaway first, so the answer is on screen before any scrolling.
        item {
            ListHeader {
                Text(if (shown) stringResource(R.string.history_hint, wash.coverage.count { it.status == StepStatus.OK }) else duration(wash.durationS))
            }
        }
        when {
            wash.modelVersion == null -> item { Notice(stringResource(R.string.timer_only), morphContent(spec)) }
            !wash.coverageShown -> item { Notice(stringResource(R.string.too_short), morphContent(spec)) }
            else -> {
                item { Notice(stringResource(R.string.washed_for, duration(wash.durationS)), morphContent(spec)) }
                items(wash.coverage.size) { i ->
                    val c = wash.coverage[i]
                    val rise = with(LocalDensity.current) { 6.dp.toPx() }
                    // Icon and text, never colour alone (§4.1.10); the glyph is read as the text. Colour only adds emphasis.
                    Row(
                        morphContent(spec).graphicsLayer {
                            val t = ((entrance.value * ENTRANCE_MS - i * STAGGER_MS) / ROW_MS).coerceIn(0f, 1f)
                            val p = EaseOutStrong.transform(t)
                            alpha = p
                            translationY = (1 - p) * rise
                        }.padding(horizontal = 16.dp).semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            glyph(c.status),
                            Modifier.width(20.dp).clearAndSetSemantics {},
                            color = if (c.status == StepStatus.OK) colors.primary else colors.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                        Column {
                            Text(steps[c.label - 1], style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(statusText(c.status)), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val washes by remember { History.get(context).washes }.collectAsState()
    ListScreen { spec ->
        item { ListHeader { Text(stringResource(R.string.history)) } }
        if (washes.isEmpty()) item { Notice(stringResource(R.string.history_empty), morphContent(spec)) }
        items(washes) { w ->
            FilledTonalButton(
                onClick = { onOpen(w.id) },
                modifier = morph(spec),
                transformation = SurfaceTransformation(spec),
                label = { Text(DateUtils.formatDateTime(context, w.startedAtEpochMs, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH)) },
                secondaryLabel = { Text("${duration(w.durationS)} · ${hint(w)}") },
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    var wrist by remember { mutableStateOf(settings.wrist) }
    var haptics by remember { mutableStateOf(settings.haptics) }
    var ring by remember { mutableStateOf(settings.progressRing) }
    ListScreen { spec ->
        item { ListHeader { Text(stringResource(R.string.settings)) } }
        // Two visible options, no dialog (§4.1.5). Read at Wash start, so a change applies from the next Wash.
        item { ListSubHeader(morph(spec), transformation = SurfaceTransformation(spec)) { Text(stringResource(R.string.wrist_side)) } }
        for ((side, name) in listOf(Wrist.LEFT to R.string.wrist_left, Wrist.RIGHT to R.string.wrist_right)) {
            item {
                RadioButton(
                    selected = wrist == side,
                    onSelect = { wrist = side; settings.wrist = side },
                    modifier = morph(spec),
                    transformation = SurfaceTransformation(spec),
                    label = { Text(stringResource(name)) },
                )
            }
        }
        item { Notice(stringResource(R.string.applies_next_wash), morphContent(spec)) }
        item {
            SwitchButton(haptics, { haptics = it; settings.haptics = it }, morph(spec), transformation = SurfaceTransformation(spec), label = { Text(stringResource(R.string.haptics)) })
        }
        item {
            SwitchButton(ring, { ring = it; settings.progressRing = it }, morph(spec), transformation = SurfaceTransformation(spec), label = { Text(stringResource(R.string.progress_ring)) })
        }
    }
}

/** Press feedback for Start and Stop: a slight, critically damped shrink while the finger is down. */
@Composable
private fun rememberPressScale(): Pair<MutableInteractionSource, Modifier> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium))
    return source to Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
private fun Notice(text: String, modifier: Modifier = Modifier.fillMaxWidth()) =
    Text(text, modifier, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

@Composable
private fun hint(w: WashSession): String = when {
    w.modelVersion == null -> stringResource(R.string.timer_only_short)
    !w.coverageShown -> stringResource(R.string.too_short_short)
    else -> stringResource(R.string.history_hint, w.coverage.count { it.status == StepStatus.OK })
}

private fun duration(s: Float): String {
    val t = s.toInt()
    return "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
}

private fun glyph(s: StepStatus) = when (s) {
    StepStatus.OK -> "✓"
    StepStatus.BRIEF -> "◐"
    StepStatus.NOT_DETECTED -> "○"
}

private fun statusText(s: StepStatus) = when (s) {
    StepStatus.OK -> R.string.status_ok
    StepStatus.BRIEF -> R.string.status_brief
    StepStatus.NOT_DETECTED -> R.string.status_not_detected
}
