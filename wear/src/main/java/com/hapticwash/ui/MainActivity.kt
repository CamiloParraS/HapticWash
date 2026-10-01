package com.hapticwash.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.wear.compose.material3.Icon
import com.hapticwash.session.StepCoverage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.LayoutDirection
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
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
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
import com.hapticwash.session.Haptics
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
import kotlin.math.ceil

/** Progress ring target. A UI constant: nothing in Coverage reads it. */
private const val RING_TARGET_S = Haptics.TARGET_MS / 1000f

/** Summary entrance: each row fades and rises over [ROW_MS], [STAGGER_MS] after the one above. */
private const val ROW_MS = 200f
private const val STAGGER_MS = 40f
private const val ENTRANCE_MS = 360 // ROW_MS + 4 × STAGGER_MS, for five rows

/** Half a list button's 52 dp, so [ListScreen] can centre the last row. */
private val ROW_HALF_HEIGHT = 26.dp

/** Summary ring: diameter, stroke, and the gap between Step arcs. */
private val RING_SIZE = 124.dp
private val RING_STROKE = 8.dp
private const val RING_GAP_DEG = 14f

/** Strong ease-out: moves at once, settles gently. */
private val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

class MainActivity : ComponentActivity() {
    /** Counts Tile and complication taps; each one is answered once, as a tap on Start wash. */
    private var startRequests by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) takeStart(intent)
        addOnNewIntentListener(::takeStart)
        setContent { HapticWashTheme { AppScaffold { App { startRequests } } } }
    }

    private fun takeStart(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_START, false)) startRequests++
    }

    companion object {
        const val EXTRA_START = "start"

        /** Opens the app and starts a Wash, from outside it (the complication; the Tile builds its own launch action). */
        fun startIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_START, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}

/**
 * Max depth 2 (§4.1.5): Start → In-progress → Summary, Start → History → Summary, Start → Settings.
 * [startRequests] is a provider, not an Int: the nav graph keeps its destination lambdas, so a captured
 * Int would stay at its first value and a Tile tap on an already-open Start would be missed.
 */
@Composable
fun App(startRequests: () -> Int = { 0 }) {
    val nav = rememberSwipeDismissableNavController()
    var answered by rememberSaveable { mutableIntStateOf(0) }
    // A Tile or complication tap: back to Start, which then starts (or rejoins) the Wash.
    LaunchedEffect(startRequests()) { if (startRequests() > answered) nav.popBackStack("start", inclusive = false) }
    SwipeDismissableNavHost(nav, startDestination = "start") {
        composable("start") {
            StartScreen(
                onWash = { nav.navigate("wash") },
                onHistory = { nav.navigate("history") },
                onSettings = { nav.navigate("settings") },
                autoStart = startRequests() > answered,
                onAutoStarted = { answered = startRequests() },
            )
        }
        composable("wash") { WashScreen(onEnd = { id -> nav.navigate("summary/$id") { popUpTo("start") } }) }
        composable("history") { HistoryScreen(onOpen = { nav.navigate("summary/$it") }) }
        composable("summary/{id}") { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull()) }
        composable("settings") { SettingsScreen() }
    }
}

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. Items shrink and fade at the round edge via [spec]. */
@Composable
private fun ListScreen(endAtCentre: Boolean = false, content: TransformingLazyColumnScope.(spec: TransformationSpec) -> Unit) {
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // Like the watch's own Settings: the last row can scroll up to the middle of the screen, with black below it.
    val centre = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() / 2 } - ROW_HALF_HEIGHT
    ScreenScaffold(state) { padding ->
        TransformingLazyColumn(
            state = state,
            contentPadding = if (endAtCentre) PaddingValues(
                start = padding.calculateStartPadding(LayoutDirection.Ltr),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = centre,
            ) else padding,
        ) { content(spec) }
    }
}

/** Full width, with the item's height following the edge transformation. Surfaces also take `SurfaceTransformation(spec)`. */
private fun TransformingLazyColumnItemScope.morph(spec: TransformationSpec) =
    Modifier.fillMaxWidth().transformedHeight(this, spec)

/** [morph] for plain content that has no surface of its own. */
private fun TransformingLazyColumnItemScope.morphContent(spec: TransformationSpec) =
    morph(spec).graphicsLayer { with(spec) { applyContainerTransformation(scrollProgress) } }

@Composable
private fun StartScreen(onWash: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit, autoStart: Boolean, onAutoStarted: () -> Unit) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    val washes by remember { History.get(context).washes }.collectAsState()
    var timerOnly by remember { mutableStateOf(false) }
    fun begin() {
        if (!status.running) WashService.start(context)
        onWash()
    }
    // Asked on the first Start, not at launch; the Wash begins whatever the answer.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { begin() }
    fun start() {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) begin()
        else askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(autoStart) {
        if (autoStart) {
            onAutoStarted()
            start()
        }
    }
    LaunchedEffect(Unit) {
        // Loads the model off the main thread; also the release smoke check's log line.
        timerOnly = withContext(Dispatchers.Default) { StepModel.bundled(context) == null }
    }
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // The one primary action hugs the bottom curve, where the Tile puts it too, so app and Tile read as one (§4.1.3).
    ScreenScaffold(state, edgeButton = {
        EdgeButton(::start, buttonSize = EdgeButtonSize.Medium) {
            Text(stringResource(if (status.running) R.string.session_running else R.string.start_wash))
        }
    }) { padding ->
        // Short enough to fit above the full-size EdgeButton: a longer list would shrink it until scrolled (§4.1.4).
        TransformingLazyColumn(state = state, contentPadding = padding, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
            // The last takeaway in the Tile's words, so the screen says something at a glance. Weight and size make the hierarchy, not a container.
            item {
                val last = washes.firstOrNull()
                Column(morphContent(spec).semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (last != null) Text(
                        lastWashLabel(context, last),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        last?.let { hint(context.resources, it) } ?: stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    if (timerOnly) Text(stringResource(R.string.timer_only_short), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
            // Rarely needed, so small and secondary to the EdgeButton; 48 dp targets (§4.1.3).
            item {
                Row(morphContent(spec).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    FilledTonalIconButton(onHistory, Modifier.size(48.dp), shapes = IconButtonDefaults.animatedShapes()) { Icon(painterResource(R.drawable.ic_history), stringResource(R.string.history)) }
                    FilledTonalIconButton(onSettings, Modifier.size(48.dp), shapes = IconButtonDefaults.animatedShapes()) { Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings)) }
                }
            }
        }
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
                // Counts down to the target, then shows the total. Tabular digits, so the time doesn't shift sideways as it ticks.
                Text(
                    duration(if (full) elapsedS else ceil(RING_TARGET_S - elapsedS)),
                    style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                )
                Text(
                    if (full) stringResource(R.string.target_reached, RING_TARGET_S.toInt()) else stringResource(R.string.time_left),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
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
        if (shown) item { CoverageRing(wash.coverage, { entrance.value }, morphContent(spec)) }
        else item { ListHeader { Text(duration(wash.durationS)) } }
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
                            val p = staggered(entrance.value, i)
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
    ListScreen(endAtCentre = true) { spec ->
        item { ListHeader { Text(stringResource(R.string.history)) } }
        if (washes.isEmpty()) item { Notice(stringResource(R.string.history_empty), morphContent(spec)) }
        items(washes) { w ->
            FilledTonalButton(
                onClick = { onOpen(w.id) },
                modifier = morph(spec),
                transformation = SurfaceTransformation(spec),
                label = { Text(DateUtils.formatDateTime(context, w.startedAtEpochMs, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH)) },
                secondaryLabel = { Text("${duration(w.durationS)} · ${hint(LocalContext.current.resources, w)}") },
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    val status by WashService.status.collectAsState()
    var wrist by remember { mutableStateOf(settings.wrist) }
    var haptics by remember { mutableStateOf(settings.haptics) }
    var ring by remember { mutableStateOf(settings.progressRing) }
    // Three settings, three rows, one screen: each row names the setting, and its current value, with an icon to scan by.
    ListScreen(endAtCentre = true) { spec ->
        item { ListHeader { Text(stringResource(R.string.settings)) } }
        // Two values, so a tap flips it and the row shows the new one: no dialog, and a second tap undoes it (§4.1.5).
        item {
            FilledTonalButton(
                onClick = { wrist = if (wrist == Wrist.LEFT) Wrist.RIGHT else Wrist.LEFT; settings.wrist = wrist },
                modifier = morph(spec),
                transformation = SurfaceTransformation(spec),
                icon = { Icon(painterResource(R.drawable.ic_watch), contentDescription = null) },
                label = { Text(stringResource(R.string.wrist_side)) },
                secondaryLabel = { Text(stringResource(if (wrist == Wrist.LEFT) R.string.wrist_left else R.string.wrist_right)) },
            )
        }
        item {
            SwitchButton(haptics, { haptics = it; settings.haptics = it }, morph(spec), transformation = SurfaceTransformation(spec),
                icon = { Icon(painterResource(R.drawable.ic_vibration), contentDescription = null) }, label = { Text(stringResource(R.string.haptics)) })
        }
        item {
            SwitchButton(ring, { ring = it; settings.progressRing = it }, morph(spec), transformation = SurfaceTransformation(spec),
                icon = { Icon(painterResource(R.drawable.ic_ring), contentDescription = null) }, label = { Text(stringResource(R.string.progress_ring)) })
        }
        // Only said when it matters: Wrist side and the ring are read at Wash start.
        if (status.running) item { Notice(stringResource(R.string.applies_next_wash), morphContent(spec)) }
    }
}

/** Press feedback for Stop: a slight, critically damped shrink while the finger is down. */
@Composable
private fun rememberPressScale(): Pair<MutableInteractionSource, Modifier> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium))
    return source to Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Row [i]'s share of the Summary entrance, eased: 0 before its turn, 1 once it has settled. */
private fun staggered(entrance: Float, i: Int) =
    EaseOutStrong.transform(((entrance * ENTRANCE_MS - i * STAGGER_MS) / ROW_MS).coerceIn(0f, 1f))

/**
 * The five Steps as arcs around the takeaway, clockwise from 12 o'clock in list order. Shape
 * carries the Status, not colour (§4.1.10): Looks covered fills the arc, May have been brief fills
 * half, Not detected leaves only the track, like the ✓ ◐ ○ glyphs below. The list says it in words.
 */
@Composable
private fun CoverageRing(coverage: List<StepCoverage>, entrance: () -> Float, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val ok = coverage.count { it.status == StepStatus.OK }
    val takeaway = stringResource(R.string.history_hint, ok)
    Box(modifier.height(RING_SIZE).clearAndSetSemantics { contentDescription = takeaway }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(RING_SIZE)) {
            val stroke = RING_STROKE.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(size.width - stroke, size.height - stroke)
            val style = Stroke(stroke, cap = StrokeCap.Round)
            val slot = 360f / coverage.size
            coverage.forEachIndexed { i, c ->
                val start = -90f + i * slot + RING_GAP_DEG / 2
                val sweep = slot - RING_GAP_DEG
                drawArc(colors.surfaceContainer, start, sweep, false, topLeft, arc, style = style)
                val share = when (c.status) {
                    StepStatus.OK -> 1f
                    StepStatus.BRIEF -> 0.5f
                    StepStatus.NOT_DETECTED -> 0f
                }
                if (share > 0f) drawArc(colors.primary, start, sweep * share * staggered(entrance(), i), false, topLeft, arc, style = style)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.ring_count, ok), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.ring_caption), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Notice(text: String, modifier: Modifier = Modifier.fillMaxWidth()) =
    Text(text, modifier, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

/** A Wash in a few words, for History rows and the Tile. */
internal fun hint(res: Resources, w: WashSession): String = when {
    w.modelVersion == null -> res.getString(R.string.timer_only_short)
    !w.coverageShown -> res.getString(R.string.too_short_short)
    else -> res.getString(R.string.history_hint, w.coverage.count { it.status == StepStatus.OK })
}

/** "Last wash · 7:15 AM", or the date once it is not today. Start and the Tile. */
internal fun lastWashLabel(context: Context, w: WashSession) =
    "${context.getString(R.string.last_wash_title)} · ${DateUtils.getRelativeTimeSpanString(context, w.startedAtEpochMs)}"

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
