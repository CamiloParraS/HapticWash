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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
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
import com.hapticwash.BuildConfig
import com.hapticwash.session.Haptics
import com.hapticwash.session.History
import com.hapticwash.session.Settings
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashService
import com.hapticwash.session.allCovered
import com.hapticwash.session.WashSession
import com.hapticwash.session.worthSaving
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
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
private val START_RING_SIZE = 60.dp
private val START_RING_STROKE = 5.dp
private val START_RING_SIZE_COMPACT = 36.dp
private val START_RING_STROKE_COMPACT = 4.dp

/** The stop symbol drawn at the centre while holding, and how much of the hold the countdown stays readable for. */
private val STOP_MARK = 28.dp
private const val SWAP_FROM = 0.3f

/** How far the countdown has given way to the stop symbol at hold [h]: 0 until [SWAP_FROM], then eased to 1. */
private fun swap(h: Float) = EaseOutStrong.transform(((h - SWAP_FROM) / (1 - SWAP_FROM)).coerceIn(0f, 1f))

/** The Wash ring: one arc round the screen, leaving a 60° gap at 12 o'clock for TimeText. */
private const val RING_START_DEG = 300f
private const val RING_SWEEP_DEG = 300f

/** Stop is a hold anywhere on the Wash screen: the sweep takes [HOLD_MS], and the "Hold to stop" hint stays [HINT_MS]. */
// ponytail: 700 ms is a guess; tune with wet hands on a device.
private const val HOLD_MS = 700
private const val HINT_MS = 3000L

/** The Summary's arcs and rows begin once the ring landing from the Wash is this far in. */
private const val ENTRANCE_AT = 0.6f

/** Start's ghost button is gone by this share of the bloom. */
private const val GHOST_FADE = 0.4f

/** "Hold to stop" waits this long after the bloom before it fades in, so it doesn't compete with it. */
private const val HINT_DELAY_MS = 700L

/** The sweep's share drawn on the press itself, so touch-down is answered before the first frame of the hold. */
private const val PRESS_HEAD = 0.03f

/** A hold that ended the Wash, until its Summary has taken over: the ring carries the stop colour across. */
private object StopHold {
    var committed = false
    var committedAt = 0L
}

/** A hold's commit gets at least this long on screen before the result takes over. */
private const val COMMIT_BEAT_MS = 450L

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
        // ponytail: opening the app restarts Twist start's listening after a process kill or a sensor failure;
        // reboot and kill with the app closed are ticket 05.
        if (Settings(this).twistStart) WashService.listen(this)
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
private object StartOrigin {
    var centre = Offset.Unspecified
    var size = IntSize.Zero
}

/**
 * Max depth 2 (§4.1.5): Start → In-progress, which shows its own Summary when the Wash ends, Start → History → Summary, Start → Settings.
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
    // A Wash that began without a tap on Start (a Twist): show it. A tap has already navigated, so Start is no longer current.
    LaunchedEffect(running.running) {
        if (running.running && nav.currentBackStackEntry?.destination?.route == "start") nav.navigate("wash")
    }
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
                composable(Routes.START) {
                    StartScreen(
                        onWash = { nav.navigate("wash") },
                        onHistory = { nav.navigate("history") },
                        onSettings = { nav.navigate("settings") },
                        autoStart = startRequests() > answered,
                        onAutoStarted = { answered = startRequests() },
                    )
                }
                composable(Routes.WASH) { WashScreen(onGuide = { nav.navigate(Routes.guide(it)) }) }
                composable(Routes.HISTORY) { HistoryScreen(onOpen = { nav.navigate(Routes.summary(it)) }) }
                composable(Routes.SUMMARY) { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull(), onGuide = { l -> nav.navigate(Routes.guide(l)) }) }
                composable(Routes.GUIDE) { GuideScreen(it.arguments?.getString("label")?.toIntOrNull()) }
                composable(Routes.SETTINGS) { SettingsScreen(onCredits = { nav.navigate(Routes.CREDITS) }) }
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
    Box(Modifier.fillMaxSize().then(if (backdrop) Modifier.accentGlow(MaterialTheme.colorScheme.primary) else Modifier)) {
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

/** Start's entrance plays once per process: coming back from History or Settings shows it settled. */
private object StartEntrance { var played = false }

/** Row [i] of Start fades and rises with the Summary's stagger, driven by [entrance] (0..1 over [ENTRANCE_MS]). */
private fun Modifier.rise(entrance: () -> Float, i: Int) = graphicsLayer {
    val p = staggered(entrance(), i)
    alpha = p
    translationY = (1 - p) * 8.dp.toPx()
}

/** A dark wash of [accent] behind a screen, like watchOS's app gradients. */
private fun Modifier.accentGlow(accent: Color) = drawBehind {
    drawRect(Brush.radialGradient(listOf(accent.copy(alpha = 0.18f), Color.Transparent), Offset(size.width / 2, size.height * 0.3f), size.maxDimension * 0.6f))
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
    val entrance = remember { Animatable(if (StartEntrance.played) 1f else 0f) }
    // The ring's arcs fill in just after the rows start rising, and the count climbs with them.
    val fillIn = remember { Animatable(if (StartEntrance.played) 1f else 0f) }
    LaunchedEffect(Unit) {
        StartEntrance.played = true
        launch { delay(120); fillIn.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing)) }
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // ponytail: scrolls only on small screens, where the buttons sit below the fold; 220 dp is a guess, tune on a device.
    val compact = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 220.dp
    // Tertiary when the last wash covered all five, like the ring, so the home screen carries the result.
    val lastShown = washes.firstOrNull()?.takeIf { it.coverageShown }
    val glow by animateColorAsState(if (lastShown?.let { allCovered(it.coverage) } == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary, tween(300))
    Box(Modifier.fillMaxSize().accentGlow(glow)) {
    ScreenScaffold(state, scrollIndicator = { ScrollIndicatorUnlessCapturing(state) }) { padding ->
        // Small screens: the stack sits ~10 dp lower, under the clock. The scaffold's bottom inset is generous, so trimming it keeps the stack from overflowing into a scroll.
        val inset = if (compact) PaddingValues(start = padding.calculateStartPadding(LayoutDirection.Ltr), top = padding.calculateTopPadding() + 6.dp, end = padding.calculateEndPadding(LayoutDirection.Ltr), bottom = (padding.calculateBottomPadding() - 14.dp).coerceAtLeast(0.dp)) else padding
        TransformingLazyColumn(state = state, contentPadding = inset, userScrollEnabled = compact, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
            // The last takeaway as one hero: the ring with its count inside, the time under it. Other results (too short,
            // Timer-only, none yet) have no ring and keep the sentence.
            item {
                val last = washes.firstOrNull()
                val label = last?.let { lastWashLabel(context, it) }
                val sentence = last?.let { hint(context.resources, it) } ?: stringResource(R.string.history_empty)
                val description = listOfNotNull(label, sentence).joinToString(". ")
                val ok = last?.coverage?.count { it.status == StepStatus.OK } ?: 0
                // Only recomposes when the shown digit changes.
                val shown by remember(ok) { derivedStateOf { (ok * fillIn.value).roundToInt() } }
                if (compact) {
                    // Small screens: the ring beside its text, centred and inset so the ring clears the round edge.
                    Row(
                        morphContent(spec).rise({ entrance.value }, 0).clearAndSetSemantics { contentDescription = description }
                            .fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (last?.coverageShown == true) {
                            MiniRing(last.coverage, START_RING_SIZE_COMPACT, START_RING_STROKE_COMPACT, MaterialTheme.colorScheme.surfaceContainerHigh) { staggered(fillIn.value, it) }
                            Column {
                                Text(stringResource(R.string.coverage_count, shown), style = MaterialTheme.typography.titleMedium.rounded().copy(fontFeatureSettings = "tnum"))
                                // Steps, not when: "Last wash · Oct 1" read as a daily habit. The time stays in the spoken description.
                                Text(stringResource(R.string.steps_covered), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        } else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(sentence, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                            if (label != null) Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            if (timerOnly) Text(stringResource(R.string.timer_only_short), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else Column(morphContent(spec).rise({ entrance.value }, 0).clearAndSetSemantics { contentDescription = description }, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (last?.coverageShown == true) {
                        Box(contentAlignment = Alignment.Center) {
                            MiniRing(last.coverage, START_RING_SIZE, START_RING_STROKE, MaterialTheme.colorScheme.surfaceContainerHigh) { staggered(fillIn.value, it) }
                            Text(
                                shown.toString(),
                                style = MaterialTheme.typography.titleLarge.rounded().copy(fontFeatureSettings = "tnum"),
                            )
                        }
                    } else Text(sentence, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    if (label != null) Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
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
                    morph(spec).rise({ entrance.value }, 1).then(pressScale).then(if (compact) Modifier.height(48.dp) else Modifier).onGloballyPositioned { StartOrigin.centre = it.positionInWindow() + Offset(it.size.width / 2f, it.size.height / 2f); StartOrigin.size = it.size }
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
                    icon = if (compact) null else ({ Icon(painterResource(R.drawable.ic_wash), null) }),
                    label = { Text(stringResource(if (status.running) R.string.session_running else R.string.start_wash), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                )
            }
            // Rarely needed, so small and secondary to Start; 48 dp targets (§4.1.3).
            // Not edge-scaled: this low on the screen the transformation would shrink them below 48 dp.
            item {
                Row(Modifier.fillMaxWidth().rise({ entrance.value }, 2).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    FilledTonalIconButton(onHistory, Modifier.size(48.dp), shapes = IconButtonDefaults.animatedShapes()) { Icon(painterResource(R.drawable.ic_history), stringResource(R.string.history)) }
                    FilledTonalIconButton(onSettings, Modifier.size(48.dp), shapes = IconButtonDefaults.animatedShapes()) { Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings)) }
                }
            }
        }
    }
    }
}

/**
 * The Wash, and then its Summary in the same place: the result fades in over the Wash instead of sliding in as a
 * new screen, so the ring and the stop square can carry across it. Swiping back from either goes to Start.
 */
@Composable
private fun WashScreen(onGuide: (Int) -> Unit = {}) {
    var result by remember { mutableStateOf<Long?>(null) }
    val leave = remember { Animatable(0f) }
    LaunchedEffect(result) { if (result != null) leave.animateTo(1f, tween(200)) }
    Box(Modifier.fillMaxSize()) {
        // Gone once faded: nothing under the Summary should take a press.
        if (leave.value < 1f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1 - leave.value }) { WashBody(onEnd = { result = it }, handedOver = result != null) }
        result?.let { SummaryScreen(it, fromWash = true, onGuide = onGuide) }
        if (result == null) TargetCheckCard()
    }
}

/**
 * Target check: at the target, names the Steps not detected so far (name + icon). Closes after 6 s or on tap; the Wash
 * runs on behind it. The service clears the Steps when the Wash ends, which closes it too.
 */
@Composable
private fun TargetCheckCard() {
    val undetected = WashService.status.collectAsState().value.undetected
    var dismissed by remember { mutableStateOf(false) }
    LaunchedEffect(undetected) {
        dismissed = false
        if (undetected.isNotEmpty()) { delay(TARGET_CARD_MS); dismissed = true }
    }
    if (undetected.isEmpty() || dismissed) return
    val steps = stringArrayResource(R.array.steps)
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).clickable { dismissed = true }.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.status_not_detected), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        undetected.forEach {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(STEP_ICONS[it - 1]), contentDescription = null, Modifier.size(24.dp))
                Text(steps[it - 1], style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private const val TARGET_CARD_MS = 6_000L

/** Elapsed time, optional progress ring, hold to stop. Never names a Step. */
@Composable
private fun WashBody(onEnd: (Long) -> Unit, handedOver: Boolean) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    val last by WashService.lastWash.collectAsState()
    val before = remember { last?.id }
    LaunchedEffect(last) {
        val ended = last?.takeIf { it.id != before } ?: return@LaunchedEffect
        // The save is quick; a hold gets a beat for its commit to be seen first.
        if (StopHold.committed) delay((COMMIT_BEAT_MS - (System.currentTimeMillis() - StopHold.committedAt)).coerceAtLeast(0))
        onEnd(ended.id)
    }
    val ring = remember { Settings(context).progressRing }
    // The Summary draws the ring and the stop square from here on, shrinking them into its own; these must not double them.
    val carried = handedOver && ring
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
    // Hold anywhere to stop (0 = not held, 1 = committed). Wet, soapy fingers miss a button, and a hold survives water
    // on the glass. Press responds at once; the sweep is linear while the user decides, and release snaps back fast.
    val hold = remember { Animatable(0f) }
    // The commit: the stop square and the ring's stroke spring past their size and settle (1 = at rest).
    val commitPop = remember { Animatable(1f) }
    val hint = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        StopHold.committed = false
        delay(HINT_DELAY_MS)
        if (hold.value == 0f) hint.animateTo(1f, tween(300))
        delay(HINT_MS)
        hint.animateTo(0f, tween(400))
    }
    val stopInk = MaterialTheme.colorScheme.error
    val stopGlow = MaterialTheme.colorScheme.errorContainer
    // Committed and waiting for the save: the extra glow eases off and the stop square breathes, so the wait isn't a freeze.
    val settle by animateFloatAsState(if (stopping) 0.4f else 1f, tween(600))
    val stopBreath = remember { Animatable(1f) }
    LaunchedEffect(stopping) {
        if (!stopping) return@LaunchedEffect
        delay(400) // after the commit pop
        while (true) {
            stopBreath.animateTo(0.88f, tween(700, easing = FastOutSlowInEasing))
            stopBreath.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
    }
    val scope = rememberCoroutineScope()
    val feedback = LocalHapticFeedback.current
    fun stop() { stopping = true; WashService.stop(context) }
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
            Modifier.fillMaxSize().onGloballyPositioned { screen = it }
                .semantics { onLongClick(label = context.getString(R.string.stop_wash)) { if (!stopping) stop(); true } }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        if (stopping) return@awaitEachGesture
                        scope.launch {
                            hint.snapTo(0f)
                            // Answers the press on the first frame: a sliver of the sweep and the colour, then the rest.
                            hold.snapTo(PRESS_HEAD)
                            hold.animateTo(1f, tween(HOLD_MS, easing = LinearEasing))
                            feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            StopHold.committed = true
                            StopHold.committedAt = System.currentTimeMillis()
                            launch { commitPop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 3f) }
                            stop()
                        }
                        waitForUpOrCancellation()
                        // A new animateTo cancels the sweep in flight; once committed there is nothing to undo.
                        if (!stopping && hold.value < 1f) scope.launch { hold.animateTo(0f, tween(200, easing = EaseOutStrong)) }
                    }
                }.drawBehind {
                val ramp = if (full) 0f else ((elapsedS - (RING_TARGET_S - GLOW_RAMP_S)) / GLOW_RAMP_S).coerceIn(0f, 1f)
                val alpha = glow + (breath.value - 0.5f) * 0.08f + 0.08f * ramp
                // The whole screen warms toward the stop colour as the hold fills: fully the stop colour by half way, and brighter than the Wash's own glow, so the change is seen from a glance.
                val h = hold.value
                val shift = EaseOutStrong.transform((h * 2).coerceAtMost(1f))
                drawRect(Brush.radialGradient(listOf(androidx.compose.ui.graphics.lerp(ringColor, stopGlow, shift).copy(alpha = (alpha + 0.5f * h * settle) * enter.value), Color.Transparent)))
                val r = ripple.value
                if (r < 1f) {
                    val edge = size.minDimension / 2
                    drawCircle(ringColor, lerp(edge, edge * RIPPLE_TO, r), alpha = (1 - r) * 0.7f, style = Stroke(3.dp.toPx()))
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            // One arc from 300° round to 240°: the 60° gap at 12 o'clock leaves TimeText clear.
            // Drawn, not CircularProgressIndicator: that one restarts its own animation on every new value,
            // so fed every frame it never moves until the value stops changing at 100 %.
            if (ring) {
                val colors = ProgressIndicatorDefaults.colors(indicatorColor = ringColor)
                val stroke = CircularProgressIndicatorDefaults.largeStrokeWidth
                Spacer(
                    Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding).graphicsLayer { alpha = if (carried) 0f else enter.value }.drawBehind {
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
                            drawCircularProgressIndicator((elapsedS / RING_TARGET_S).coerceAtMost(1f), colors, stroke, startAngle = RING_START_DEG, endAngle = RING_START_DEG + RING_SWEEP_DEG)
                        }
                    },
                )
            }
            // Hold to stop: the same ring, drawn the same way, filling a second time in the stop colour, so it reads as
            // the ring turning into "end" rather than a new bar. Drawn even with the progress ring off.
            val holdColors = ProgressIndicatorDefaults.colors(indicatorColor = stopInk, trackColor = Color.Transparent)
            val holdStroke = CircularProgressIndicatorDefaults.largeStrokeWidth
            Spacer(Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding).drawBehind {
                val h = hold.value
                if (h <= 0f || carried) return@drawBehind
                drawCircularProgressIndicator(h, holdColors, holdStroke * commitPop.value, startAngle = RING_START_DEG, endAngle = RING_START_DEG + RING_SWEEP_DEG)
                // The stop symbol grows in at the centre as the countdown gives way to it.
                val side = STOP_MARK.toPx() * lerp(0.6f, 1f, swap(h)) * commitPop.value * stopBreath.value
                drawRoundRect(stopInk, center - Offset(side / 2, side / 2), Size(side, side), CornerRadius(side * 0.22f), alpha = swap(h))
            })
            Column(
                Modifier.graphicsLayer {
                    val p = enter.value
                    val s = swap(hold.value)
                    alpha = p * (1 - s)
                    scaleX = lerp(0.9f, 1f, p) * (1 - 0.1f * s)
                    scaleY = scaleX
                }.padding(padding),
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
                // Said once, then gone: the whole screen is the Stop target. "Stopping…" replaces it at once on commit.
                Text(
                    stringResource(if (stopping) R.string.stopping else R.string.hold_to_stop),
                    Modifier.graphicsLayer { alpha = if (stopping) 1f else hint.value },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            // Start's button, left where it was: it shrinks and fades in the first moments of the bloom, so the ring
            // reads as coming out of it. Only while the ring blooms, which is when Start's place is known.
            val at = screen?.takeIf { it.isAttached && ring && StartOrigin.centre.isSpecified }?.windowToLocal(StartOrigin.centre)
            if (at != null && enter.value < 0.999f) {
                val size = StartOrigin.size
                Row(
                    Modifier.size(with(LocalDensity.current) { size.width.toDp() }, with(LocalDensity.current) { size.height.toDp() })
                        .offset { IntOffset((at.x - size.width / 2f).toInt(), (at.y - size.height / 2f).toInt()) }
                        .graphicsLayer {
                            val p = enter.value
                            alpha = 1f - (p / GHOST_FADE).coerceAtMost(1f)
                            scaleX = lerp(1f, 0.6f, p)
                            scaleY = scaleX
                        }
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primaryDim, MaterialTheme.colorScheme.primary))),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_wash), null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.start_wash), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** A Wash's Summary: the one just ended ([fromWash]), or one from History. */
@Composable
private fun SummaryScreen(id: Long?, fromWash: Boolean = false, onGuide: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val wash = remember(id) {
        WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(context).read().find { it.id == id }
    }
    val steps = stringArrayResource(R.array.steps)
    val colors = MaterialTheme.colorScheme
    // Rows arrive in a short cascade as the end pulses play. Decorative: scrolling is never blocked.
    val entrance = remember { Animatable(0f) }
    // C6: straight from a Wash with the live ring on, that ring shrinks into the Coverage ring (0 = live, 1 = landed).
    val stoppedByHold = remember { fromWash && StopHold.committed.also { StopHold.committed = false } }
    val morph = remember { Animatable(if (fromWash && wash?.coverageShown == true && Settings(context).progressRing) 0f else 1f) }
    LaunchedEffect(Unit) { morph.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    // The arcs and rows start once the ring is most of the way in, so they fill where they can be seen.
    LaunchedEffect(Unit) {
        snapshotFlow { morph.value }.first { it >= ENTRANCE_AT }
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    // Ended by a hold, the stop square becomes the answer: it shrinks into the ring's centre and the numeral grows from there.
    fun numeral() = if (stoppedByHold) EaseOutStrong.transform(((morph.value - 0.4f) / 0.6f).coerceIn(0f, 1f)) else 1f
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
                    .graphicsLayer { alpha = morph.value }, ::numeral)
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
                            }.clip(RoundedCornerShape(16.dp)).clickable { onGuide(c.label) }.padding(horizontal = 16.dp).semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            if (!wash.worthSaving()) item { Notice(stringResource(R.string.too_short_to_save), morphContent(spec)) }
        }
        if (morph.value < 1f) LiveRing(wash!!.durationS, { morph.value }, { ringCentre }, stoppedByHold)
        if (stoppedByHold && morph.value < 1f) {
            val ink = colors.error
            Canvas(Modifier.fillMaxSize()) {
                val p = EaseOutStrong.transform(morph.value)
                val from = Offset(size.width / 2, size.height / 2)
                val c = lerp(from, ringCentre.takeIf { it.isSpecified } ?: from, p)
                val side = STOP_MARK.toPx() * lerp(1f, 0.4f, p)
                drawRoundRect(ink, c - Offset(side / 2, side / 2), Size(side, side), CornerRadius(side * 0.22f), alpha = 1 - numeral())
            }
        }
    }
}

/** A Step's guide page: name, Hegasy crop (CC BY-SA, separate file; D34), one sourced why line. */
@Composable
private fun GuideScreen(label: Int?) {
    if (label == null || label !in 1..5) return
    val colors = MaterialTheme.colorScheme
    ScreenScaffold {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringArrayResource(R.array.steps)[label - 1], style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Image(painterResource(STEP_ART[label - 1]), null, Modifier.size(100.dp).clip(CircleShape))
            Text(stringArrayResource(R.array.step_whys)[label - 1], color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

/**
 * The In-progress ring as it was when the Wash ended, drawn over the Summary and shrinking into the
 * Coverage ring at [centre] as [morph] goes 0 → 1, fading as it goes. Same geometry as WashScreen's.
 */
@Composable
private fun LiveRing(durationS: Float, morph: () -> Float, centre: () -> Offset, stopped: Boolean) {
    val base = if (durationS >= RING_TARGET_S) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val stop = MaterialTheme.colorScheme.error
    val liveStroke = CircularProgressIndicatorDefaults.largeStrokeWidth
    Canvas(Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding)) {
        val p = morph()
        val stroke = lerp(liveStroke.toPx(), RING_STROKE.toPx(), p)
        val r = lerp(size.minDimension / 2 - liveStroke.toPx() / 2, RING_SIZE.toPx() / 2 - RING_STROKE.toPx() / 2, p)
        // Canvas space is inset by the padding; the target centre is in the screen's.
        val pad = CircularProgressIndicatorDefaults.FullScreenPadding.toPx()
        val c = lerp(center, centre().takeIf { it.isSpecified }?.minus(Offset(pad, pad)) ?: center, p)
        val reached = (durationS / RING_TARGET_S).coerceAtMost(1f)
        // Ended by a hold, the ring leaves full and in the stop colour, then settles into the Wash's own.
        val progress = if (stopped) lerp(1f, reached, p) else reached
        val color = if (stopped) androidx.compose.ui.graphics.lerp(stop, base, (p * 1.5f).coerceAtMost(1f)) else base
        if (progress > 0f) drawArc(color, RING_START_DEG, RING_SWEEP_DEG * progress, false, Offset(c.x - r, c.y - r), Size(2 * r, 2 * r), alpha = 1 - p, style = Stroke(stroke, cap = StrokeCap.Round))
    }
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
private fun CreditsScreen() {
    ListScreen(backdrop = false) { spec ->
        item { Header(spec, stringResource(R.string.credits)) }
        item { Notice(stringResource(R.string.credits_body), morphContent(spec)) }
    }
}

@Composable
private fun SettingsScreen(onCredits: () -> Unit = {}) {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    val status by WashService.status.collectAsState()
    var wrist by remember { mutableStateOf(settings.wrist) }
    var haptics by remember { mutableStateOf(settings.haptics) }
    var ring by remember { mutableStateOf(settings.progressRing) }
    var twist by remember { mutableStateOf(settings.twistStart) }
    var targetCheck by remember { mutableStateOf(settings.targetCheck) }
    var warn by remember { mutableStateOf(false) }
    fun setTwist(on: Boolean) {
        twist = on
        settings.twistStart = on
        if (on) WashService.listen(context) else WashService.unlisten(context)
    }
    // One row per setting: each row names the setting, and its current value, with an icon to scan by.
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
        // Turning it on asks first (§4.1 item 8); turning it off doesn't.
        item {
            SwitchButton(twist, { on -> if (on) warn = true else setTwist(false) },
                morph(spec), transformation = SurfaceTransformation(spec),
                icon = { Icon(painterResource(R.drawable.ic_wash), contentDescription = null) }, label = { Text(stringResource(R.string.twist_start)) })
        }
        // Absent entirely when the build flag ships the check disabled.
        if (BuildConfig.TARGET_CHECK) item {
            SwitchButton(targetCheck, { targetCheck = it; settings.targetCheck = it }, morph(spec), transformation = SurfaceTransformation(spec),
                icon = { Icon(painterResource(R.drawable.ic_step_tips), contentDescription = null) }, label = { Text(stringResource(R.string.target_check)) })
        }
        item {
            FilledTonalButton(onClick = onCredits, modifier = morph(spec), transformation = SurfaceTransformation(spec), label = { Text(stringResource(R.string.credits)) })
        }
        // Only said when it matters: Wrist side and the ring are read at Wash start.
        if (status.running) item { Notice(stringResource(R.string.applies_next_wash), morphContent(spec)) }
    }
    // The battery warning: one screen, turn on or leave off. Dismissing leaves it off.
    AlertDialog(
        visible = warn,
        onDismissRequest = { warn = false },
        title = { Text(stringResource(R.string.twist_warning_title)) },
        text = { Text(stringResource(R.string.twist_warning)) },
        confirmButton = { AlertDialogDefaults.ConfirmButton({ warn = false; setTwist(true) }) },
        dismissButton = { AlertDialogDefaults.DismissButton({ warn = false }) },
    )
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
private fun CoverageRing(coverage: List<StepCoverage>, entrance: () -> Float, modifier: Modifier, numeral: () -> Float = { 1f }) {
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
        Column(Modifier.graphicsLayer { val n = numeral(); alpha = n; scaleX = lerp(0.6f, 1f, n); scaleY = scaleX }, horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun MiniRing(coverage: List<StepCoverage>, size: Dp = MINI_RING_SIZE, stroke: Dp = MINI_RING_STROKE, track: Color = MaterialTheme.colorScheme.surfaceContainer, fill: (Int) -> Float = { 1f }) {
    val ink = ink(coverage)
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        coverageArcs(coverage, stroke.toPx(), track, ink, fill)
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
private val STEP_ART = intArrayOf(R.drawable.step_art_palms, R.drawable.step_art_backs, R.drawable.step_art_between, R.drawable.step_art_thumbs, R.drawable.step_art_tips)
private val STEP_ICONS =intArrayOf(R.drawable.ic_step_palms, R.drawable.ic_step_backs, R.drawable.ic_step_between, R.drawable.ic_step_thumbs, R.drawable.ic_step_tips)

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
private fun WashPreview() = PreviewFrame { WashScreen() }

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
