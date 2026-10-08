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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.hapticwash.ui.theme.rounded
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.util.lerp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.drawCircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import kotlin.math.acos
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
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
import com.hapticwash.session.allCovered
import com.hapticwash.session.WashSession
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Progress ring target. A UI constant: nothing in Coverage reads it. */
private const val RING_TARGET_S = Haptics.TARGET_MS / 1000f
private const val GLOW_RAMP_S = 3f

/** Summary entrance: each row fades and rises over [ROW_MS], [STAGGER_MS] after the one above. */
private const val ROW_MS = 200f
private const val STAGGER_MS = 40f
private const val ENTRANCE_MS = 360 // ROW_MS + 4 × STAGGER_MS, for five rows

/** Half a list button's 52 dp, so [ListScreen] can centre the last row. */
private val ROW_HALF_HEIGHT = 26.dp

/** Summary ring: diameter, stroke, and the gap between Step arcs. */
private val RING_SIZE = 124.dp
private val RING_STROKE = 8.dp
internal const val RING_GAP_DEG = 14f

/** Start's ring for the last Wash. */
private val MINI_RING_SIZE = 40.dp
private val MINI_RING_STROKE = 4.dp

/** Stop's EdgeButton: [EdgeButtonSize.Small]'s 56 dp and the 3 dp it keeps below itself. The Wash ring opens around it. */
private val STOP_HEIGHT = 59.dp
// ponytail: guessed margin between the ring's caps and Stop; tune on a device.
private val STOP_CLEARANCE = 4.dp

/** Start → Wash: the ring blooms from the Start button, from this fraction of its size up to 1. */
private const val BLOOM_FROM = 0.2f

/** The 40 s target reached: a thin ring travels in from the screen's edge to [RIPPLE_TO] of its radius, fading. */
private const val RIPPLE_MS = 700
private const val RIPPLE_TO = 0.45f

/** All five Steps covered: [BURST_DROPS] droplets leave the Coverage ring over [BURST_MS]. */
private const val BURST_DROPS = 10
private const val BURST_MS = 650
private val BURST_REACH = 20.dp

/** Strong ease-out: moves at once, settles gently. */
private val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

class MainActivity : ComponentActivity() {
    /** Counts Tile and complication taps; each one is answered once, as a tap on Start wash. */
    private var startRequests by mutableIntStateOf(0)

    /** A Wash the Tile asked to show, until the Summary is open. */
    private var openRequest by mutableStateOf<Long?>(null)

    /** Null while the screen is interactive. */
    private var ambient by mutableStateOf<Ambient?>(null)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) { ambient = Ambient(ambientDetails.burnInProtectionRequired) }

        override fun onUpdateAmbient() { ambient = ambient?.let { it.copy(updates = it.updates + 1) } }

        override fun onExitAmbient() { ambient = null }
    }
    private var ambientObserver: AmbientLifecycleObserver? = null

    /**
     * Ambient-aware only while a Wash runs: that is when the app has something to show in ambient mode. At any
     * other time the system handles ambient for it, as for any app, rather than leaving a bright Start on the display.
     */
    private fun watchAmbient(washing: Boolean) {
        if (washing == (ambientObserver != null)) return
        if (washing) {
            ambientObserver = AmbientLifecycleObserver(this, ambientCallback).also { lifecycle.addObserver(it) }
            return
        }
        val observer = ambientObserver ?: return
        lifecycle.removeObserver(observer)
        // Removing it skips the lifecycle's remaining steps, which are how it lets go of ambient mode.
        val state = lifecycle.currentState
        if (state.isAtLeast(Lifecycle.State.RESUMED)) observer.onPause(this)
        if (state.isAtLeast(Lifecycle.State.STARTED)) observer.onStop(this)
        observer.onDestroy(this)
        ambientObserver = null
        ambient = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) takeStart(intent)
        addOnNewIntentListener(::takeStart)
        lifecycleScope.launch { WashService.status.map { it.running }.distinctUntilChanged().collect(::watchAmbient) }
        setContent { HapticWashTheme { AppScaffold { App({ startRequests }, { openRequest }, { openRequest = null }, { ambient }) } } }
    }

    private fun takeStart(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_START, false)) startRequests++
        intent.getLongExtra(EXTRA_OPEN, -1).takeIf { it >= 0 }?.let { openRequest = it }
    }

    companion object {
        const val EXTRA_START = "start"

        /** A Wash id: opens that Wash's Summary (the Tile's ring and words). */
        const val EXTRA_OPEN = "open"

        /** Opens the app and starts a Wash, from outside it (the complication; the Tile builds its own launch action). */
        fun startIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_START, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}

/**
 * Ambient mode: [burnIn] is whether the display needs its image moved about, [updates] counts the once-a-minute
 * redraws the system allows, which is what moves it.
 */
data class Ambient(val burnIn: Boolean, val updates: Int = 0)

/** True while ambient mode has the screen and a Wash is running, for the Wash screen beneath to stand still. */
private val LocalAmbient = compositionLocalOf { false }

/** Where Start's button sat in the window, so the Wash ring can bloom from it. Written by Start, read by Wash. */
private object StartOrigin { var centre = Offset.Unspecified }

/**
 * Max depth 2 (§4.1.5): Start → In-progress → Summary (`result`, the one just ended), Start → History → Summary, Start → Settings.
 * [startRequests] is a provider, not an Int: the nav graph keeps its destination lambdas, so a captured
 * Int would stay at its first value and a Tile tap on an already-open Start would be missed.
 */
@Composable
fun App(startRequests: () -> Int = { 0 }, openRequest: () -> Long? = { null }, onOpened: () -> Unit = {}, ambient: () -> Ambient? = { null }) {
    val nav = rememberSwipeDismissableNavController()
    val running by WashService.status.collectAsState()
    val ambientNow = ambient().takeIf { running.running }
    var answered by rememberSaveable { mutableIntStateOf(0) }
    // A Tile or complication tap: back to Start, which then starts (or rejoins) the Wash.
    LaunchedEffect(startRequests()) { if (startRequests() > answered) nav.popBackStack("start", inclusive = false) }
    // A tap on the Tile's ring or words: that Wash's Summary, over Start, as if opened from History.
    LaunchedEffect(openRequest()) {
        val id = openRequest() ?: return@LaunchedEffect
        nav.popBackStack("start", inclusive = false)
        nav.navigate("summary/$id")
        onOpened()
    }
    // The Wash screen stays composed under ambient mode, so waking returns to it as it was; ambient only covers it.
    Box {
        CompositionLocalProvider(LocalAmbient provides (ambientNow != null)) {
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
                composable("wash") { WashScreen(onEnd = { id -> nav.navigate("result/$id") { popUpTo("start") } }) }
                composable("result/{id}") { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull(), fromWash = true) }
                composable("history") { HistoryScreen(onOpen = { nav.navigate("summary/$it") }) }
                composable("summary/{id}") { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull()) }
                composable("settings") { SettingsScreen() }
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
    val colors = MaterialTheme.colorScheme
    val line = with(LocalDensity.current) { 1.5.dp.toPx() }
    val liveStroke = CircularProgressIndicatorDefaults.largeStrokeWidth
    ScreenScaffold {
        Box(
            Modifier.fillMaxSize().background(Color.Black).offset(shift, shift).drawBehind {
                // The live ring's circle and its 60° gap at the top, as a thin outline.
                val inset = CircularProgressIndicatorDefaults.FullScreenPadding.toPx() + liveStroke.toPx() / 2
                drawArc(
                    colors.onSurfaceVariant, 300f, 300f, false, Offset(inset, inset),
                    Size(size.width - 2 * inset, size.height - 2 * inset), style = Stroke(line, cap = StrokeCap.Round),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.ambient_washing),
                style = MaterialTheme.typography.displaySmall.rounded().copy(color = colors.onBackground, drawStyle = Stroke(line)),
            )
        }
    }
}

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. Items shrink and fade at the round edge via [spec]. */
@Composable
private fun ListScreen(backdrop: Boolean = true, content: TransformingLazyColumnScope.(spec: TransformationSpec) -> Unit) {
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // Like the watch's own Settings: the last row can scroll up to the middle of the screen, with black below it.
    val centre = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() / 2 } - ROW_HALF_HEIGHT
    val accent = MaterialTheme.colorScheme.primary
    // A dark wash of the accent behind the list, like watchOS's app gradients.
    Box(Modifier.fillMaxSize().drawBehind {
        if (backdrop) drawRect(Brush.radialGradient(listOf(accent.copy(alpha = 0.18f), Color.Transparent), Offset(size.width / 2, size.height * 0.3f), size.maxDimension * 0.6f))
    }) {
        ScreenScaffold(state, scrollIndicator = { ScrollIndicatorUnlessCapturing(state) }) { padding ->
            TransformingLazyColumn(
                state = state,
                contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = centre,
                ),
            ) { content(spec) }
        }
    }
}

/** Full width, with the item's height following the edge transformation. Surfaces also take `SurfaceTransformation(spec)`. */
private fun TransformingLazyColumnItemScope.morph(spec: TransformationSpec) =
    Modifier.fillMaxWidth().transformedHeight(this, spec)

/** A list's title: morphs at the edge like the rows, and keeps the top padding Material asks of a first header. */
@Composable
private fun TransformingLazyColumnItemScope.Header(spec: TransformationSpec, text: String) = ListHeader(
    morph(spec).minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding),
    transformation = SurfaceTransformation(spec),
) { Text(text) }

/** The default scroll indicator, hidden from long screenshots, where it would be frozen mid-list. */
@Composable
private fun ScrollIndicatorUnlessCapturing(state: TransformingLazyColumnState) {
    if (!LocalScrollCaptureInProgress.current) ScrollIndicator(state)
}

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
    // ponytail: scrolls only on small screens, where the buttons sit below the fold; 220 dp is a guess, tune on a device.
    val compact = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 220.dp
    ScreenScaffold(state, scrollIndicator = { ScrollIndicatorUnlessCapturing(state) }) { padding ->
        TransformingLazyColumn(state = state, contentPadding = padding, userScrollEnabled = compact, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
            // The last takeaway in the Tile's words, so the screen says something at a glance. Weight and size make the hierarchy, not a container.
            item {
                val last = washes.firstOrNull()
                Column(morphContent(spec).semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (last?.coverageShown == true) MiniRing(last.coverage)
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
            // The one primary action, in the middle with the secondary actions below it.
            item {
                val (press, pressScale) = rememberPressScale()
                // From the watch's theme, like the rest of the app: primary into tertiary, the colour the ring turns at the target.
                val colors = listOf(MaterialTheme.colorScheme.primaryDim, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                // A slow drift of the gradient so Start feels alive. Read only in the draw phase, so it never recomposes;
                // it stops by itself when the system's animation scale is 0.
                val drift by rememberInfiniteTransition(label = "startGradient")
                    .animateFloat(0f, 1f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "drift")
                Button(
                    ::start,
                    morph(spec).then(pressScale).onGloballyPositioned { StartOrigin.centre = it.positionInWindow() + Offset(it.size.width / 2f, it.size.height / 2f) }
                        .clip(CircleShape)
                        .drawBehind {
                            // Water: the colours flow one way, never reversing. Mirrored tiles repeat every two bands, so sliding
                            // by exactly two ends where it began and the loop has no seam.
                            val band = Offset(size.width, size.height * 0.6f)
                            drawRect(Brush.linearGradient(colors, band * (2 * drift), band * (2 * drift + 1), TileMode.Mirror))
                            // A soft light drifting on a looping figure-of-eight, at its own pace over the flow.
                            val a = 2 * PI.toFloat() * drift
                            val light = Offset(size.width * (0.5f + 0.3f * sin(a)), size.height * (0.5f + 0.25f * sin(2 * a)))
                            drawRect(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent), light, size.width * 0.45f))
                        },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimary),
                    interactionSource = press,
                    transformation = SurfaceTransformation(spec),
                    icon = { Icon(painterResource(R.drawable.ic_wash), null) },
                    label = { Text(stringResource(if (status.running) R.string.session_running else R.string.start_wash), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                )
            }
            // Rarely needed, so small and secondary to Start; 48 dp targets (§4.1.3).
            // Not edge-scaled: this low on the screen the transformation would shrink them below 48 dp.
            item {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
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
    // Derived state, so the ring's progress lambda reads the live value at draw time.
    val elapsedS by remember { derivedStateOf { if (status.running) ((now - status.startedAtEpochMs) / 1000f).coerceAtLeast(0f) else 0f } }
    // The ring changes colour once it fills, so reaching the target is visible at a glance.
    val full by remember { derivedStateOf { elapsedS >= RING_TARGET_S } }
    // The whole seconds on screen: the text recomposes once a second, not every frame.
    val shownS by remember { derivedStateOf { if (full) elapsedS.toInt().toFloat() else ceil(RING_TARGET_S - elapsedS) } }
    val ambient = LocalAmbient.current
    LaunchedEffect(ambient) {
        // Ambient mode covers this screen and draws once a minute: nothing here needs to tick. Waking catches up at once.
        if (ambient) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            // Every frame while the ring moves, so it glides and turns colour on the same frame as the time-reached ticks.
            // Otherwise four times a second, but never past the target: the ripple and pop land with the ticks.
            if (ring && !full) withFrameMillis {}
            else delay(if (status.running && !full) (Haptics.TARGET_MS - (now - status.startedAtEpochMs)).coerceIn(1L, 250L) else 250L)
        }
    }
    // Start → Wash: the ring and the countdown bloom from where the Start button was (0 = at the button, 1 = settled).
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    var screen by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // Reaching the target, seen as well as felt: a ripple in from the edge and a small pop on the number, on the frame
    // the three ticks play. Only for a target reached while watching, not one that passed while the screen was away.
    val ripple = remember { Animatable(1f) }
    val pop = remember { Animatable(1f) }
    val startedFull = remember { full }
    val watching by rememberUpdatedState(!ambient)
    LaunchedEffect(full) {
        if (!full || startedFull || !watching) return@LaunchedEffect
        launch { pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f) }
        ripple.snapTo(0f)
        ripple.animateTo(1f, tween(RIPPLE_MS, easing = EaseOutStrong))
    }
    val ringColor by animateColorAsState(if (full) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary, tween(300))
    val (press, pressScale) = rememberPressScale()
    // A dark wash of the ring's colour behind everything, like watchOS's app gradients; it brightens once the target is reached.
    val glow by animateFloatAsState(if (full) 0.32f else 0.18f, tween(300))
    // The glow breathes while measuring, and swells over the last RAMP_S before the target so the ripple lands as a release.
    // Stopped in ambient mode, where nothing here moves.
    val breath = remember { Animatable(0.5f) }
    LaunchedEffect(ambient) {
        if (ambient) return@LaunchedEffect
        while (true) {
            breath.animateTo(1f, tween(2000, easing = FastOutSlowInEasing))
            breath.animateTo(0f, tween(2000, easing = FastOutSlowInEasing))
        }
    }
    ScreenScaffold { padding ->
        Box(
            Modifier.fillMaxSize().onGloballyPositioned { screen = it }.drawBehind {
                val ramp = if (full) 0f else ((elapsedS - (RING_TARGET_S - GLOW_RAMP_S)) / GLOW_RAMP_S).coerceIn(0f, 1f)
                val alpha = glow + (breath.value - 0.5f) * 0.08f + 0.08f * ramp
                drawRect(Brush.radialGradient(listOf(ringColor.copy(alpha = alpha * enter.value), Color.Transparent)))
                val r = ripple.value
                if (r < 1f) {
                    val edge = size.minDimension / 2
                    drawCircle(ringColor, lerp(edge, edge * RIPPLE_TO, r), alpha = (1 - r) * 0.7f, style = Stroke(3.dp.toPx()))
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            // A 60° gap centred on 12 o'clock leaves TimeText clear, and one at 6 o'clock leaves Stop clear: two arcs,
            // down the right then up the left, filled one after the other.
            // Drawn, not CircularProgressIndicator: that one restarts its own animation on every new value,
            // so fed every frame it never moves until the value stops changing at 100 %.
            if (ring) {
                val colors = ProgressIndicatorDefaults.colors(indicatorColor = ringColor)
                val stroke = CircularProgressIndicatorDefaults.largeStrokeWidth
                Spacer(
                    Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding).graphicsLayer { alpha = enter.value }.drawBehind {
                        // Scaled up about its centre and slid from the Start button's place, so one shape grows into the next.
                        val pad = CircularProgressIndicatorDefaults.FullScreenPadding.toPx()
                        val centre = size.center
                        val from = screen?.takeIf { it.isAttached && StartOrigin.centre.isSpecified }
                            ?.windowToLocal(StartOrigin.centre)?.minus(Offset(pad, pad)) ?: centre
                        val p = enter.value
                        withTransform({
                            translate((1 - p) * (from.x - centre.x), (1 - p) * (from.y - centre.y))
                            val s = lerp(BLOOM_FROM, 1f, p)
                            scale(s, s, centre)
                        }) {
                            val g = stopGapHalfDeg(stroke.toPx())
                            val (right, left) = halves((elapsedS / RING_TARGET_S).coerceAtMost(1f))
                            drawCircularProgressIndicator(right, colors, stroke, startAngle = 300f, endAngle = 90f - g)
                            drawCircularProgressIndicator(left, colors, stroke, startAngle = 90f + g, endAngle = 240f)                        }
                    },
                )
            }
            Column(
                Modifier.graphicsLayer { val p = enter.value; alpha = p; scaleX = lerp(0.9f, 1f, p); scaleY = scaleX }.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Counts down to the target, then shows the total. Tabular digits, so the time doesn't shift sideways as it ticks.
                Text(
                    duration(shownS),
                    Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                    style = MaterialTheme.typography.displayMedium.rounded().copy(fontFeatureSettings = "tnum"),
                )
                Text(
                    if (full) stringResource(R.string.target_reached, RING_TARGET_S.toInt()) else stringResource(R.string.time_left),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (status.timerOnly) Notice(stringResource(R.string.timer_only))
            }
            // An EdgeButton, wide and on the screen's edge: wet, soapy fingers miss small targets. Answers the tap at once,
            // before the Wash has finished saving. Tonal, so the countdown leads and Stop stays easy to hit.
            EdgeButton(
                onClick = { stopping = true; WashService.stop(context) },
                modifier = Modifier.align(Alignment.BottomCenter).graphicsLayer { alpha = enter.value }.then(pressScale),
                buttonSize = EdgeButtonSize.Small,
                enabled = !stopping,
                colors = ButtonDefaults.filledTonalButtonColors(),
                interactionSource = press,
            ) { Text(stringResource(if (stopping) R.string.stopping else R.string.stop_wash)) }
        }
    }
}

/** A Wash's Summary: the one just ended ([fromWash]), or one from History. */
@Composable
private fun SummaryScreen(id: Long?, fromWash: Boolean = false) {
    val context = LocalContext.current
    val wash = remember(id) {
        WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(context).read().find { it.id == id }
    }
    val steps = stringArrayResource(R.array.steps)
    val colors = MaterialTheme.colorScheme
    // Rows arrive in a short cascade as the end pulses play. Decorative: scrolling is never blocked.
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing)) }
    // C6: straight from a Wash with the live ring on, that ring shrinks into the Coverage ring (0 = live, 1 = landed).
    val morph = remember { Animatable(if (fromWash && wash?.coverageShown == true && Settings(context).progressRing) 0f else 1f) }
    LaunchedEffect(Unit) { morph.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    // The Coverage ring's centre in this screen's space, so a moving nav transition doesn't throw the morph off.
    var ringCentre by remember { mutableStateOf(Offset.Unspecified) }
    var screen by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Box(Modifier.onGloballyPositioned { screen = it }) {
        ListScreen { spec ->
            if (wash == null) return@ListScreen
            // The takeaway first, so the answer is on screen before any scrolling.
            if (wash.coverageShown) item {
                CoverageRing(wash.coverage, { entrance.value }, morphContent(spec)
                    .onGloballyPositioned { ring -> screen?.let { ringCentre = it.localBoundingBoxOf(ring).center } }
                    .graphicsLayer { alpha = morph.value })
            }
            else item { Header(spec, duration(wash.durationS)) }
            when {
                wash.modelVersion == null -> item { Notice(stringResource(R.string.timer_only), morphContent(spec)) }
                !wash.coverageShown -> item { Notice(stringResource(R.string.too_short), morphContent(spec)) }
                else -> {
                    item { Notice(stringResource(if (allCovered(wash.coverage)) R.string.washed_for_all else R.string.washed_for, duration(wash.durationS)), morphContent(spec)) }
                    items(wash.coverage.size) { i ->
                        val c = wash.coverage[i]
                        val rise = with(LocalDensity.current) { 6.dp.toPx() }
                        // Each Step has its own glyph, so a row is found by shape; colour only adds emphasis.
                        Row(
                            morphContent(spec).graphicsLayer {
                                val p = staggered(entrance.value, i)
                                alpha = p
                                translationY = (1 - p) * rise
                            }.padding(horizontal = 16.dp).semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // A soft chip behind the glyph, tinted only when the Step looks covered; the words and the mark say the rest.
                            Box(
                                Modifier.size(32.dp).clip(CircleShape).background(if (c.status == StepStatus.OK) colors.primary.copy(alpha = 0.18f) else colors.surfaceContainer).clearAndSetSemantics {},
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(painterResource(STEP_ICONS[c.label - 1]), null, Modifier.size(20.dp), tint = if (c.status == StepStatus.OK) colors.primary else colors.onSurfaceVariant)
                            }
                            Column {
                                Text(steps[c.label - 1], style = MaterialTheme.typography.titleSmall)
                                // The Status as a mark and in words, never colour alone (§4.1.10); the mark is read as the words.
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(glyph(c.status), Modifier.clearAndSetSemantics {}, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    Text(stringResource(statusText(c.status)), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (morph.value < 1f) LiveRing(wash!!.durationS, { morph.value }, { ringCentre })
    }
}

/**
 * The In-progress ring as it was when the Wash ended, drawn over the Summary and shrinking into the
 * Coverage ring at [centre] as [morph] goes 0 → 1, fading as it goes. Same geometry as WashScreen's.
 */
@Composable
private fun LiveRing(durationS: Float, morph: () -> Float, centre: () -> Offset) {
    val color = if (durationS >= RING_TARGET_S) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val liveStroke = CircularProgressIndicatorDefaults.largeStrokeWidth
    Canvas(Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding)) {
        val p = morph()
        val stroke = lerp(liveStroke.toPx(), RING_STROKE.toPx(), p)
        val r = lerp(size.minDimension / 2 - liveStroke.toPx() / 2, RING_SIZE.toPx() / 2 - RING_STROKE.toPx() / 2, p)
        // Canvas space is inset by the padding; the target centre is in the screen's.
        val pad = CircularProgressIndicatorDefaults.FullScreenPadding.toPx()
        val c = lerp(center, centre().takeIf { it.isSpecified }?.minus(Offset(pad, pad)) ?: center, p)
        val g = stopGapHalfDeg(liveStroke.toPx())
        val (right, left) = halves((durationS / RING_TARGET_S).coerceAtMost(1f))
        val style = Stroke(stroke, cap = StrokeCap.Round)
        if (right > 0f) drawArc(color, 300f, (150f - g) * right, false, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r), alpha = 1 - p, style = style)
        if (left > 0f) drawArc(color, 90f + g, (150f - g) * left, false, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r), alpha = 1 - p, style = style)
    }
}

/** The Wash ring's progress split over its two equal arcs: the right fills first, then the left. */
private fun halves(progress: Float) = (progress * 2).coerceAtMost(1f) to (progress * 2 - 1).coerceIn(0f, 1f)

/**
 * Half the Wash ring's bottom gap, in degrees, for a ring drawn as WashScreen draws it (in a canvas inset by
 * FullScreenPadding, [stroke] wide): where the stroke would reach the top of Stop, plus [STOP_CLEARANCE].
 */
private fun DrawScope.stopGapHalfDeg(stroke: Float): Float {
    val r = size.minDimension / 2 - stroke / 2 // the arc's centre line
    val screenR = size.minDimension / 2 + CircularProgressIndicatorDefaults.FullScreenPadding.toPx()
    // Below this height, measured down from the centre, the stroke would touch the button.
    val clear = screenR - (STOP_HEIGHT + STOP_CLEARANCE).toPx() - stroke / 2
    return Math.toDegrees(acos((clear / r).coerceIn(-1f, 1f)).toDouble()).toFloat()
}

@Composable
private fun HistoryScreen(onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val washes by remember { History.get(context).washes }.collectAsState()
    ListScreen { spec ->
        item { Header(spec, stringResource(R.string.history)) }
        if (washes.isEmpty()) item { Notice(stringResource(R.string.history_empty), morphContent(spec)) }
        items(washes) { w ->
            FilledTonalButton(
                onClick = { onOpen(w.id) },
                modifier = morph(spec),
                transformation = SurfaceTransformation(spec),
                icon = if (w.coverageShown) ({ MiniRing(w.coverage, 28.dp, 3.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)) }) else null,
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
    ListScreen(backdrop = false) { spec ->
        item { Header(spec, stringResource(R.string.settings)) }
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

/** Press feedback for Start and Stop: a slight, critically damped shrink while the finger is down. */
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
    // All five covered: once the last arc lands, the ring gives one small spring pop and a burst of droplets.
    val pop = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    if (ok == coverage.size) LaunchedEffect(Unit) {
        snapshotFlow(entrance).first { it >= 1f } // follows the system animation scale, unlike a fixed delay
        launch { burst.snapTo(0f); burst.animateTo(1f, tween(BURST_MS, easing = EaseOutStrong)) }
        pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f)
    }
    Box(modifier.height(RING_SIZE).clearAndSetSemantics { contentDescription = takeaway }, contentAlignment = Alignment.Center) {
        val ink = ink(coverage)
        Canvas(Modifier.size(RING_SIZE).graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
            coverageArcs(coverage, RING_STROKE.toPx(), colors.surfaceContainer, ink) { staggered(entrance(), it) }
        }
        // Drawn past the ring's bounds: the droplets leave from its outer edge.
        Canvas(Modifier.size(RING_SIZE)) {
            val b = burst.value
            if (b < 1f) repeat(BURST_DROPS) { i ->
                val a = Math.toRadians(-90.0 + i * 360.0 / BURST_DROPS)
                val r = size.minDimension / 2 + BURST_REACH.toPx() * b
                val drop = (if (i % 2 == 0) 4.dp else 3.dp).toPx() * (1 - b)
                drawCircle(ink, drop, center + Offset(cos(a).toFloat(), sin(a).toFloat()) * r, alpha = 1 - b)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // The answer as one big rounded numeral: tabular, and tracked tighter the larger it is.
            Text(
                ok.toString(),
                style = MaterialTheme.typography.displayMedium.rounded().copy(fontFeatureSettings = "tnum", letterSpacing = (-0.02).em),
            )
            Text(stringResource(R.string.ring_caption), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

/** Start's small copy of [CoverageRing], so the last Wash reads at a glance in the app's one shape. A [track] that contrasts with its surface keeps an all-empty ring visible. */
@Composable
private fun MiniRing(coverage: List<StepCoverage>, size: Dp = MINI_RING_SIZE, stroke: Dp = MINI_RING_STROKE, track: Color = MaterialTheme.colorScheme.surfaceContainer) {
    val ink = ink(coverage)
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        coverageArcs(coverage, stroke.toPx(), track, ink)
    }
}

/**
 * The five Steps as arcs, clockwise from 12 o'clock in list order, filling this canvas. [fill] is the
 * drawn part of each arc's [share], for the Summary's entrance. The Tile draws the same ring ([WashTileService]).
 */
private fun DrawScope.coverageArcs(coverage: List<StepCoverage>, stroke: Float, track: Color, ink: Color, fill: (Int) -> Float = { 1f }) {
    val topLeft = Offset(stroke / 2, stroke / 2)
    val arc = Size(size.width - stroke, size.height - stroke)
    val style = Stroke(stroke, cap = StrokeCap.Round)
    val slot = 360f / coverage.size
    coverage.forEachIndexed { i, c ->
        val start = -90f + i * slot + RING_GAP_DEG / 2
        val sweep = slot - RING_GAP_DEG
        drawArc(track, start, sweep, false, topLeft, arc, style = style)
        val share = share(c.status)
        if (share > 0f) drawArc(ink, start, sweep * share * fill(i), false, topLeft, arc, style = style)
    }
}

/** The ring's fill: tertiary when all five look covered, the colour the Wash ring turns at the target. */
@Composable
private fun ink(coverage: List<StepCoverage>) =
    if (allCovered(coverage)) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

/** One glyph per Step, in canonical order (labels 1..5). */
private val STEP_ICONS = intArrayOf(R.drawable.ic_step_palms, R.drawable.ic_step_backs, R.drawable.ic_step_between, R.drawable.ic_step_thumbs, R.drawable.ic_step_tips)

/** How much of a Step's arc is filled: shape carries the Status, not colour (§4.1.10). */
internal fun share(s: StepStatus) = when (s) {
    StepStatus.OK -> 1f
    StepStatus.BRIEF -> 0.5f
    StepStatus.NOT_DETECTED -> 0f
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

/**
 * Previews, in the app's theme and scaffold. Start is left out: it asks for a permission and loads the model.
 * History and the Wash show their empty and idle states, as nothing is saved or running in a preview.
 */
@Composable
private fun PreviewFrame(content: @Composable () -> Unit) = HapticWashTheme { AppScaffold { content() } }

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WashPreview() = PreviewFrame { WashScreen(onEnd = {}) }

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun SettingsPreview() = PreviewFrame { SettingsScreen() }

@WearPreviewDevices
@Composable
private fun HistoryPreview() = PreviewFrame { HistoryScreen(onOpen = {}) }

@WearPreviewDevices
@Composable
private fun CoverageRingPreview() = PreviewFrame {
    val statuses = listOf(StepStatus.OK, StepStatus.OK, StepStatus.BRIEF, StepStatus.NOT_DETECTED, StepStatus.OK)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CoverageRing(statuses.mapIndexed { i, s -> StepCoverage(i + 1, 8f, s) }, { 1f }, Modifier.fillMaxWidth())
    }
}

@WearPreviewDevices
@Composable
private fun AmbientPreview() = PreviewFrame { AmbientWash(shift = 0.dp) }
