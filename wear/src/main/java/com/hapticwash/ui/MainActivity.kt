package com.hapticwash.ui

import android.Manifest
import android.app.PendingIntent
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.wear.compose.material3.Icon
import com.hapticwash.session.StepCoverage
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.em
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.MotionDurationScale
import kotlin.coroutines.coroutineContext
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.SwitchButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.hapticwash.R
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.wear.compose.material3.VerticalPagerScaffold
import androidx.wear.compose.material3.VerticalPageIndicator
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.FilledIconButton
import com.hapticwash.inference.StepModel
import com.hapticwash.BuildConfig
import com.hapticwash.session.Haptics
import com.hapticwash.session.History
import com.hapticwash.session.Settings
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashService
import com.hapticwash.session.allCovered
import com.hapticwash.session.focusCounts
import com.hapticwash.session.focusStep
import com.hapticwash.session.WashSession
import com.hapticwash.session.worthSaving
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.CoroutineScope
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

/** Summary's Five Rings: diameter. Stroke and gap scale with it ([RING_STROKE_OF], [RING_GAP_OF]) so every size keeps a hole in the middle. */
private val RING_SIZE = 108.dp
internal const val RING_STROKE_OF = 7.4f / 108f
internal const val RING_GAP_OF = 2f / 108f
private val RING_STROKE = RING_SIZE * RING_STROKE_OF

/** Start's rings for the last Wash, and History's. */
private val START_RING_SIZE = 62.dp
private val START_RING_SIZE_COMPACT = 44.dp
private val HISTORY_RING_SIZE = 30.dp

/** The live ring round the screen's edge: 12 dp, 5 dp in from the bezel. */
private val LIVE_STROKE = 12.dp
private val LIVE_INSET = 5.dp

/** The stop symbol drawn at the centre while holding, in its disc, and how much of the hold the countdown stays readable for. */
private val STOP_MARK = 26.dp
private val STOP_DISC = 64.dp
private const val SWAP_FROM = 0.3f

/** The stop disc sits this far above the centre, so its line of text fits under it. */
private val STOP_LIFT = 15.dp

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

/** Live Wash cues: a new line every [CUE_S] s; past the target a cheer every [CHEER_S] s. A lap (every [LAP_S] s before the target) gets a small ripple and pop. */
private const val CUE_S = 5
private const val CHEER_S = 6
private const val LAP_S = 10

/** Past the target the ring laps itself once every [RING_TARGET_S], its head shadowed by [LAP_SHADOW], like an Activity ring past 100 %. */
private val LAP_SHADOW = 2.4.dp

/** All five Steps covered: [BURST_DROPS] droplets leave the Coverage ring over [BURST_MS]. */
private const val BURST_DROPS = 10
private const val BURST_MS = 650
private val BURST_REACH = 20.dp

/** Before the burst, a light rolls over the five arcs in order over [ROLL_MS]; the burst goes as it leaves the last. */
private const val ROLL_MS = 700
private const val ROLL_BURST_AT = 0.75f

/** The 40 s target reached: a bead of light runs once round the live ring over [SHINE_MS], [SHINE_DEG] long. */
private const val SHINE_MS = 650
private const val SHINE_DEG = 48f

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
    val context = LocalContext.current
    val running by WashService.status.collectAsState()
    val ambientNow = ambient().takeIf { running.running }
    var answered by rememberSaveable { mutableIntStateOf(0) }
    // Shown on Start after auto-end cancels a Wash, until the user acts.
    var cancelledNotice by rememberSaveable { mutableStateOf(false) }
    // A Tile or complication tap: back to Start, which then starts (or rejoins) the Wash.
    LaunchedEffect(startRequests()) { if (startRequests() > answered) nav.popBackStack(Routes.START, inclusive = false) }
    // A Wash that began without a tap on Start (a Twist): show it. A tap has already navigated, so Start is no longer current.
    LaunchedEffect(running.running) {
        if (running.running && nav.currentBackStackEntry?.destination?.route == Routes.START) nav.navigate(Routes.WASH)
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
        CompositionLocalProvider(LocalAmbient provides (ambientNow != null)) {
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
private const val DROP_PATH = "M12,2C6.67,6.55 4,10.48 4,13.8C4,18.78 7.8,22 12,22s8,-3.22 8,-8.2C20,10.48 17.33,6.55 12,2z"

/**
 * The live ring's arc round the screen: 300° clockwise from 1 o'clock, leaving 60° at the top for TimeText, [part] of
 * it drawn. Its centre line sits [LIVE_INSET] plus half of [ringStroke] in from the edge, so every arc drawn on that path
 * (the ring, the hold, the hairlines) shares one circle whatever its own [stroke].
 */
private fun DrawScope.edgeArc(brush: Brush, part: Float, stroke: Float, ringStroke: Float = stroke, from: Float = 0f) {
    if (part <= from) return
    val inset = LIVE_INSET.toPx() + ringStroke / 2
    drawArc(
        brush, RING_START_DEG + RING_SWEEP_DEG * from, RING_SWEEP_DEG * (part - from), false, Offset(inset, inset),
        Size(size.width - 2 * inset, size.height - 2 * inset), style = Stroke(stroke, cap = StrokeCap.Round),
    )
}

/** The point at [part] along [edgeArc]'s path. */
private fun DrawScope.edgePoint(part: Float, ringStroke: Float): Offset {
    val r = size.minDimension / 2 - LIVE_INSET.toPx() - ringStroke / 2
    val a = Math.toRadians((RING_START_DEG + RING_SWEEP_DEG * part).toDouble())
    return center + Offset(cos(a).toFloat(), sin(a).toFloat()) * r
}

/** A ring's diagonal gradient, [from] at the top left to [to] at the bottom right, as DESIGN.md's rings are lit. */
private fun DrawScope.diagonal(from: Color, to: Color) = Brush.linearGradient(listOf(from, to), Offset.Zero, Offset(size.width, size.height))

/**
 * DESIGN.md's Tint-Washes-Down: [color] falling from above the top edge, an ellipse wider than it is tall
 * (130 % × 70 % of the screen), fading to black by 78 % of the way. Each screen washes in its own colour.
 */
private fun Modifier.tintWash(color: Color, alpha: () -> Float) = drawBehind {
    val a = alpha()
    if (a <= 0f) return@drawBehind
    val c = Offset(size.width / 2, -0.08f * size.height)
    // A circle 70 % of the height across, stretched sideways into the ellipse.
    scale(1.3f * size.width / (0.7f * size.height), 1f, c) {
        drawRect(
            Brush.radialGradient(0f to color.copy(alpha = a), 0.52f to color.copy(alpha = 0.06f), 0.78f to Color.Transparent, center = c, radius = 0.7f * size.height),
            Offset(-size.width, 0f), Size(size.width * 3, size.height),
        )
    }
}

private fun Modifier.tintWash(color: Color, alpha: Float = 0.34f) = tintWash(color) { alpha }

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. Items shrink and fade at the round edge via [spec]. */
@Composable
private fun ListScreen(wash: Color? = Five.Tint, washAlpha: Float = 0.3f, content: TransformingLazyColumnScope.(spec: TransformationSpec) -> Unit) {
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // Like the watch's own Settings: the last row can scroll up to the middle of the screen, with black below it.
    val centre = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() / 2 } - ROW_HALF_HEIGHT
    Box(Modifier.fillMaxSize().background(Color.Black).then(if (wash != null) Modifier.tintWash(wash, washAlpha) else Modifier)) {
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

/** Start's entrance plays once per process: coming back from History or Settings shows it settled. [filled] is the Wash its ring last filled for. */
private object StartEntrance {
    var played = false
    var filled: Long? = null
}

/** Row [i] of Start fades and rises with the Summary's stagger, driven by [entrance] (0..1 over [ENTRANCE_MS]). */
private fun Modifier.rise(entrance: () -> Float, i: Int) = graphicsLayer {
    val p = staggered(entrance(), i)
    alpha = p
    translationY = (1 - p) * 8.dp.toPx()
}

/** 0 → 1 over [ENTRANCE_MS] each time the screen opens, for [rise]. */
@Composable
private fun rememberEntrance(): () -> Float {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing)) }
    return { p.value }
}

/** A setting's glyph: it pops with a bouncy spring each time [flip] changes, and mirrors with it. */
@Composable
private fun SettingIcon(res: Int, flip: Boolean = false, tint: Color = Color.White, size: Dp = 22.dp) {
    val pop = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(flip) {
        if (first) first = false else { pop.snapTo(0.4f); pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow * 3)) }
    }
    val mirror by animateFloatAsState(if (flip) -1f else 1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
    Icon(painterResource(res), null, Modifier.size(size).graphicsLayer { scaleX = pop.value * mirror; scaleY = pop.value }, tint = tint)
}

/** A [size] disc of [fill] with [content] centred: a row's leading glyph, as in watchOS Settings. */
@Composable
private fun GlyphDisc(fill: Color, size: Dp = 32.dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    Box(modifier.size(size).clip(CircleShape).background(fill).clearAndSetSemantics {}, contentAlignment = Alignment.Center) { content() }

/** Full width, with the item's height following the edge transformation. Surfaces also take `SurfaceTransformation(spec)`. */
private fun TransformingLazyColumnItemScope.morph(spec: TransformationSpec) =
    Modifier.fillMaxWidth().transformedHeight(this, spec)

/** A list's title: morphs at the edge like the rows, and keeps the top padding Material asks of a first header. */
@Composable
private fun TransformingLazyColumnItemScope.Header(spec: TransformationSpec, text: String, color: Color = Five.Tint) = ListHeader(
    morph(spec).minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding),
    transformation = SurfaceTransformation(spec),
    contentColor = color,
) { Text(text, Modifier.titlePop(), style = FiveType.title, textAlign = TextAlign.Center) }

/** A bold list title lands with a small overshoot, as the first beat of its screen. */
@Composable
private fun Modifier.titlePop(): Modifier {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)) }
    return graphicsLayer { val k = 0.7f + 0.3f * p.value; scaleX = k; scaleY = k; alpha = (p.value * 3f).coerceIn(0f, 1f) }
}

/** The default scroll indicator, hidden from long screenshots, where it would be frozen mid-list. */
@Composable
private fun ScrollIndicatorUnlessCapturing(state: TransformingLazyColumnState) {
    if (!LocalScrollCaptureInProgress.current) ScrollIndicator(state)
}

/** [morph] for plain content that has no surface of its own. */
private fun TransformingLazyColumnItemScope.morphContent(spec: TransformationSpec) =
    morph(spec).graphicsLayer { with(spec) { applyContainerTransformation(scrollProgress) } }

@Composable
private fun StartScreen(onWash: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit, autoStart: Boolean, onAutoStarted: () -> Unit, cancelledNotice: Boolean = false) {
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
    LaunchedEffect(Unit) {
        StartEntrance.played = true
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    // The ring's arcs fill in just after the rows start rising, and the count climbs with them. Again each time there
    // is a Wash Start has not shown yet: back from a Summary, the result lands here too rather than already being there.
    val lastId = washes.firstOrNull()?.id
    val fillIn = remember { Animatable(if (lastId != null && lastId == StartEntrance.filled) 1f else 0f) }
    LaunchedEffect(lastId) {
        if (lastId == null || (lastId == StartEntrance.filled && fillIn.value == 1f)) return@LaunchedEffect
        StartEntrance.filled = lastId
        fillIn.snapTo(0f)
        delay(120)
        fillIn.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    val state = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    // ponytail: scrolls only on small screens, where the buttons sit below the fold; 220 dp is a guess, tune on a device.
    val compact = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 220.dp
    Box(Modifier.fillMaxSize().background(Color.Black).tintWash(Five.Tint)) {
    ScreenScaffold(state, scrollIndicator = { ScrollIndicatorUnlessCapturing(state) }) { padding ->
        // Small screens: the stack sits ~10 dp lower, under the clock. The scaffold's bottom inset is generous, so trimming it keeps the stack from overflowing into a scroll.
        val inset = if (compact) PaddingValues(start = padding.calculateStartPadding(LayoutDirection.Ltr), top = padding.calculateTopPadding() + 6.dp, end = padding.calculateEndPadding(LayoutDirection.Ltr), bottom = (padding.calculateBottomPadding() - 14.dp).coerceAtLeast(0.dp)) else padding
        TransformingLazyColumn(state = state, contentPadding = inset, userScrollEnabled = compact, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)) {
            // The last takeaway as one hero: the rings beside the fraction, and when it was underneath. A cancelled Wash, the
            // first run, and results with no rings (too short, Timer-only, none detected) say it in words instead.
            item {
                val last = washes.firstOrNull()
                val label = last?.let { lastWashLabel(context, it) }
                val sentence = when {
                    cancelledNotice -> stringResource(R.string.wash_cancelled)
                    last == null -> stringResource(R.string.start_intro)
                    else -> hint(context.resources, last)
                }
                val description = listOfNotNull(sentence, label.takeUnless { cancelledNotice }).joinToString(". ")
                val ok = last?.coverage?.count { it.status == StepStatus.OK } ?: 0
                // Empty rings read as a score of zero, so no rings (and a plain sentence) when nothing looked covered.
                val ring = last?.takeIf { !cancelledNotice && it.coverageShown && ok > 0 }
                // Only recomposes when the shown digit changes.
                val shown by remember(ok) { derivedStateOf { (ok * fillIn.value).roundToInt() } }
                val timerOnlyText = stringResource(R.string.timer_only_short)
                Column(
                    morphContent(spec).rise({ entrance.value }, 0).clearAndSetSemantics { contentDescription = description }.padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when {
                        ring != null -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                                FiveRings(ring.coverage, if (compact) START_RING_SIZE_COMPACT else START_RING_SIZE) { staggered(fillIn.value, it) }
                                Column {
                                    Text(buildAnnotatedString { append(shown.toString()); withStyle(SpanStyle(fontSize = 16.sp, color = Five.TextSecondary)) { append("/5") } }, style = FiveType.headline)
                                    Text(stringResource(R.string.steps_covered), color = Five.TextSecondary, style = FiveType.labelSmall)
                                }
                            }
                            // Steps first, then when: the time is context, not the result.
                            if (label != null) Text(label, Modifier.padding(top = 5.dp), color = Five.TextSecondary, style = FiveType.label, textAlign = TextAlign.Center)
                        }
                        cancelledNotice || last == null -> {
                            // A drop in its disc over one line: the first run says what the rings are, a cancelled Wash why nothing was saved.
                            val small = cancelledNotice || compact
                            GlyphDisc(if (cancelledNotice) Color.White.copy(alpha = 0.1f) else Five.Tint.copy(alpha = 0.18f), if (small) 34.dp else 42.dp) {
                                Icon(painterResource(R.drawable.ic_wash), null, Modifier.size(if (small) 18.dp else 22.dp), tint = if (cancelledNotice) Five.TextSecondary else Five.Tint)
                            }
                            Text(sentence, Modifier.padding(top = 7.dp), style = FiveType.bodySmall.copy(fontSize = 12.5.sp), textAlign = TextAlign.Center)
                        }
                        else -> {
                            Text(sentence, style = FiveType.body, textAlign = TextAlign.Center)
                            if (label != null) Text(label, Modifier.padding(top = 3.dp), color = Five.TextSecondary, style = FiveType.label, textAlign = TextAlign.Center)
                        }
                    }
                    if (timerOnly && sentence != timerOnlyText) Text(timerOnlyText, color = Five.TextSecondary, style = FiveType.labelSmall)
                }
            }
            // The one primary action: a wide capsule in the middle, the secondary actions on the arc below it.
            item {
                val (press, pressScale) = rememberPressScale()
                val running = status.running
                // A slow drift of the gradient so Start feels alive. Read only in the draw phase, so it never recomposes;
                // it stops by itself when the system's animation scale is 0.
                val drift by rememberInfiniteTransition(label = "startGradient")
                    .animateFloat(0f, 1f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "drift")
                Button(
                    ::start,
                    morph(spec).rise({ entrance.value }, 1).then(pressScale).padding(horizontal = 14.dp).height(if (compact) 48.dp else 50.dp)
                        .onGloballyPositioned { StartOrigin.centre = it.positionInWindow() + Offset(it.size.width / 2f, it.size.height / 2f); StartOrigin.size = it.size }
                        .clip(CircleShape)
                        .drawBehind {
                            // A Wash already running: the capsule is a quiet way back to it, not a second start.
                            if (running) return@drawBehind
                            // Water: the colours flow one way, never reversing. Mirrored tiles repeat every two bands, so sliding
                            // by exactly two ends where it began and the loop has no seam.
                            val band = Offset(size.width, size.height * 0.6f)
                            drawRect(Brush.linearGradient(START_GRADIENT, band * (2 * drift), band * (2 * drift + 1), TileMode.Mirror))
                            // A soft light drifting on a looping figure-of-eight, at its own pace over the flow.
                            val a = 2 * PI.toFloat() * drift
                            val light = Offset(size.width * (0.5f + 0.3f * sin(a)), size.height * (0.5f + 0.25f * sin(2 * a)))
                            drawRect(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent), light, size.width * 0.45f))
                            // Breathing: the whole capsule brightens and settles, three slow breaths per drift loop (~4 s each) so it stays seamless.
                            drawRect(Color.White.copy(alpha = 0.07f * (0.5f - 0.5f * cos(3 * a))))
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (running) Five.PlatterRaised else Color.Transparent,
                        contentColor = if (running) Five.Tint else Five.OnTint,
                        iconColor = if (running) Five.Tint else Five.OnTint,
                    ),
                    interactionSource = press,
                    transformation = SurfaceTransformation(spec),
                    icon = {
                        if (running) Box(Modifier.size(9.dp).clip(CircleShape).background(Five.Tint))
                        else Icon(painterResource(R.drawable.ic_wash), null, Modifier.padding(end = 7.dp).size(20.dp))
                    },
                    label = { Text(stringResource(if (running) R.string.session_running else R.string.start_wash), style = FiveType.bodyLarge, maxLines = 1) },
                )
            }
            // History and Settings as glass buttons on the bottom arc, watchOS-toolbar style; 48 dp targets (§4.1.3).
            // Not edge-scaled: this low on the screen the transformation would shrink them below 48 dp.
            item {
                Row(Modifier.fillMaxWidth().rise({ entrance.value }, 2), horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)) {
                    GlassButton(R.drawable.ic_history, stringResource(R.string.history), onHistory)
                    GlassButton(R.drawable.ic_settings, stringResource(R.string.settings), onSettings)
                }
            }
        }
    }
    }
}

/** The primary capsule's water: light into the tint into sky, 135° (DESIGN.md's Pool Light gradient). */
private val START_GRADIENT = listOf(Five.TintLight, Five.Tint, Five.TintSky)

/** A secondary action as a translucent circle on the bottom arc. */
@Composable
private fun GlassButton(icon: Int, label: String, onClick: () -> Unit, size: Dp = 48.dp) =
    FilledTonalIconButton(
        onClick, Modifier.size(size), shapes = IconButtonDefaults.animatedShapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Five.Glass, contentColor = Color.White),
    ) { Icon(painterResource(icon), label, Modifier.size(24.dp)) }

/**
 * The Wash, and then its Summary in the same place: the result fades in over the Wash instead of sliding in as a
 * new screen, so the ring and the stop square can carry across it. Swiping back from either goes to Start.
 */
@Composable
private fun WashScreen(onGuide: (Int) -> Unit, onCancelled: () -> Unit = {}) {
    // Saved, so coming back from a Step guide opened off this Summary lands on the Summary, not a Wash that has ended.
    var result by rememberSaveable { mutableStateOf<Long?>(null) }
    val fresh = remember { result == null } // false when restored: no ring morph replayed
    val leave = remember { Animatable(if (result != null) 1f else 0f) }
    LaunchedEffect(result) { if (result != null) leave.animateTo(1f, tween(200)) }
    Box(Modifier.fillMaxSize()) {
        // Gone once faded: nothing under the Summary should take a press.
        if (leave.value < 1f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1 - leave.value }) { WashBody(onEnd = { result = it }, handedOver = result != null, onCancelled = onCancelled) }
        result?.let { SummaryScreen(it, fromWash = fresh, onGuide = onGuide) }
        if (result == null) TargetCheckCard(onGuide)
    }
}

/**
 * Target check: at the target, names the Steps not detected so far (name + icon). Closes after 6 s or on tap; the Wash
 * runs on behind it. The service clears the Steps when the Wash ends, which closes it too.
 */
@Composable
private fun TargetCheckCard(onGuide: (Int) -> Unit) {
    val undetected = WashService.status.collectAsState().value.undetected
    var dismissed by remember { mutableStateOf(false) }
    // The Steps last named, so the card still has them while it leaves after the service has cleared them.
    var shown by remember { mutableStateOf(undetected) }
    LaunchedEffect(undetected) {
        dismissed = false
        if (undetected.isNotEmpty()) { shown = undetected; delay(TARGET_CARD_MS); dismissed = true }
    }
    // It rises out of the Wash and sinks back into it, so the Wash is felt to run on behind it.
    AnimatedVisibility(
        undetected.isNotEmpty() && shown.isNotEmpty() && !dismissed,
        enter = fadeIn(tween(200)) + scaleIn(tween(360, easing = EaseOutStrong), initialScale = 0.9f),
        exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.94f),
    ) { TargetCheckBody(shown, onGuide) { dismissed = true } }
}

@Composable
private fun TargetCheckBody(undetected: List<Int>, onGuide: (Int) -> Unit, onDismiss: () -> Unit) {
    val steps = stringArrayResource(R.array.steps)
    // The time it has left, as a hairline on the Wash ring's path that drains to its start: it closes by itself.
    val left = remember { Animatable(1f) }
    LaunchedEffect(Unit) { left.animateTo(0f, tween(TARGET_CARD_MS.toInt(), easing = LinearEasing)) }
    Column(
        Modifier.fillMaxSize().background(Color.Black).tintWash(Five.Tint, 0.2f).drawBehind {
            edgeArc(SolidColor(Five.Tint.copy(alpha = 0.7f)), left.value, 3.dp.toPx(), LIVE_STROKE.toPx())
        }.clickable(onClickLabel = stringResource(R.string.close), role = Role.Button) { onDismiss() }.padding(horizontal = 24.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.target_todo), Modifier.padding(bottom = 4.dp).semantics { heading() }, color = Five.Tint, style = FiveType.title, textAlign = TextAlign.Center)
        // One left edge for the discs, so the names line up whatever their width. Each Step in its ring's colour with its
        // glyph, so it is found by colour and by shape. A row opens that Step's guide.
        Column(horizontalAlignment = Alignment.Start) {
            undetected.forEach {
                Row(
                    Modifier.heightIn(min = 48.dp).clickable(onClickLabel = stringResource(R.string.open_guide, steps[it - 1]), role = Role.Button) { onDismiss(); onGuide(it) },
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphDisc(Five.step(it), 34.dp) { Icon(painterResource(STEP_ICONS[it - 1]), null, Modifier.size(20.dp), tint = Color.Black) }
                    Text(steps[it - 1], style = FiveType.bodyLarge)
                }
            }
        }
    }
}

private const val TARGET_CARD_MS = 6_000L
private const val CANCEL_GRACE_MS = 600L

/** Elapsed time, optional progress ring, a changing movement cue, hold to stop. Cues are time-driven and never a Step Status. */
@Composable
private fun WashBody(onEnd: (Long) -> Unit, handedOver: Boolean, onCancelled: () -> Unit) {
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
    // A Wash that never began is cancelled and saved nowhere, so no Summary follows: the service just stops running. Leave then, or the stop hold waits for a save that never comes.
    var sawRunning by remember { mutableStateOf(false) }
    val handed by rememberUpdatedState(handedOver)
    LaunchedEffect(status.running) {
        if (status.running) { sawRunning = true; return@LaunchedEffect }
        if (!sawRunning) return@LaunchedEffect
        delay(CANCEL_GRACE_MS) // a normal end publishes its Wash first and the effect above hands over
        if (!handed) onCancelled()
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
    // And the ring, now whole, is run round once by a bead of light as it turns colour.
    val shine = remember { Animatable(1f) }
    val startedFull = remember { full }
    val watching by rememberUpdatedState(!ambient)
    LaunchedEffect(full) {
        if (!full || startedFull || !watching) return@LaunchedEffect
        launch { pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f) }
        launch { shine.snapTo(0f); shine.animateTo(1f, tween(SHINE_MS, easing = FastOutSlowInEasing)) }
        ripple.snapTo(0f)
        ripple.animateTo(1f, tween(RIPPLE_MS, easing = EaseOutStrong))
    }
    // Joy on the way: every LAP_S a lighter ripple and a nudge on the number, so 40 s reads as four laps, not one long wait.
    val lap = remember { Animatable(1f) }
    val lapIndex by remember { derivedStateOf { if (full) 0 else (elapsedS / LAP_S).toInt() } }
    val startedLap = remember { lapIndex } // coming back mid-Wash is not a new lap
    LaunchedEffect(lapIndex) {
        if (lapIndex == 0 || lapIndex == startedLap || !watching) return@LaunchedEffect
        launch { pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialVelocity = 1f) }
        lap.snapTo(0f)
        lap.animateTo(1f, tween(RIPPLE_MS, easing = EaseOutStrong))
    }
    val cues = stringArrayResource(R.array.wash_cues)
    val cheers = stringArrayResource(R.array.wash_cues_after)
    // Time-driven, not detection-driven: the same lines for everyone, so no cue implies a Step Status.
    val cue by remember { derivedStateOf { if (full) cheers[((elapsedS - RING_TARGET_S) / CHEER_S).toInt() % cheers.size] else cues[(elapsedS / CUE_S).toInt().coerceAtMost(cues.lastIndex)] } }
    val ringColor by animateColorAsState(if (full) Five.Done else Five.Tint, tween(300))
    // Each ring lit along its diagonal; past the target, mint (DESIGN.md's Mint Reached).
    val ringFrom by animateColorAsState(if (full) Five.DoneLight else Five.TintMist, tween(300))
    val ringTo by animateColorAsState(if (full) Five.DoneRing else Five.Tint, tween(300))
    val ringTrack by animateColorAsState((if (full) Five.Done else Five.Tint).copy(alpha = 0.16f), tween(300))
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
    val stopInk = Five.Stop
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
    // The ring's colour washing down from the top, like watchOS's app gradients; it brightens once the target is reached.
    val glow by animateFloatAsState(if (full) 0.3f else 0.22f, tween(300))
    // The glow breathes while measuring, and swells over the last RAMP_S before the target so the ripple lands as a release.
    // Stopped in ambient mode, where nothing here moves.
    val breath = remember { Animatable(0.5f) }
    LaunchedEffect(ambient) {
        if (ambient) return@LaunchedEffect
        // Animator scale 0 finishes every animation at once, so this loop would spin; hold still instead.
        if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) return@LaunchedEffect
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
                }.background(Color.Black).tintWash(ringColor) {
                    // Timer-only has no wash: the screen stays plain black, like its hairline.
                    if (status.timerOnly) return@tintWash 0f
                    val ramp = if (full) 0f else ((elapsedS - (RING_TARGET_S - GLOW_RAMP_S)) / GLOW_RAMP_S).coerceIn(0f, 1f)
                    // Gives way to the stop colour as the hold fills.
                    (glow + (breath.value - 0.5f) * 0.08f + 0.08f * ramp) * enter.value * (1 - EaseOutStrong.transform((hold.value * 2).coerceAtMost(1f)))
                }.drawBehind {
                // The whole screen warms red from the centre as the hold fills: fully by half way, so the change is seen at a glance.
                val h = hold.value
                val shift = EaseOutStrong.transform((h * 2).coerceAtMost(1f))
                if (shift > 0f) drawRect(Brush.radialGradient(
                    0f to stopInk.copy(alpha = (0.3f + 0.08f * (1 - settle) / 0.6f) * shift), 0.6f to stopInk.copy(alpha = 0.08f * shift), 0.8f to Color.Transparent,
                ))
                val l = lap.value
                if (l < 1f) {
                    val edge = size.minDimension / 2
                    drawCircle(ringColor, lerp(edge, edge * RIPPLE_TO, l), alpha = (1 - l) * 0.35f, style = Stroke(2.dp.toPx()))
                }
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
                Spacer(
                    Modifier.fillMaxSize().graphicsLayer { alpha = if (carried) 0f else enter.value }.drawBehind {
                        // Scaled up about its centre and slid from the Start button's place, so one shape grows into the next.
                        val centre = size.center
                        val from = screen?.takeIf { it.isAttached && StartOrigin.centre.isSpecified }?.windowToLocal(StartOrigin.centre) ?: centre
                        val p = enter.value
                        val stroke = LIVE_STROKE.toPx()
                        withTransform({
                            translate((1 - p) * (from.x - centre.x), (1 - p) * (from.y - centre.y))
                            val s = lerp(BLOOM_FROM, 1f, p)
                            scale(s, s, centre)
                        }) {
                            edgeArc(SolidColor(ringTrack), 1f, stroke)
                            edgeArc(diagonal(ringFrom, ringTo), (elapsedS / RING_TARGET_S).coerceAtMost(1f), stroke)
                            if (full) lapOver(((elapsedS - RING_TARGET_S) / RING_TARGET_S) % 1f, ringTo, stroke)
                            bead(shine.value, stroke)
                        }
                    },
                )
            } else {
                // Timer-only quiet cue: a white hairline on the same path, filling with the target. Static between ticks (no
                // motion to reduce), it shows the watch is running and how far along, without the full ring the user kept off.
                Spacer(Modifier.fillMaxSize().graphicsLayer { alpha = enter.value }.drawBehind {
                    val line = 2.5.dp.toPx()
                    edgeArc(SolidColor(Color.White.copy(alpha = 0.18f)), 1f, line, LIVE_STROKE.toPx())
                    edgeArc(SolidColor(Color.White), (elapsedS / RING_TARGET_S).coerceAtMost(1f), line, LIVE_STROKE.toPx())
                })
            }
            // Hold to stop: the same ring, drawn the same way, filling a second time in the stop colour, so it reads as
            // the ring turning into "end" rather than a new bar. Drawn even with the progress ring off.
            Spacer(Modifier.fillMaxSize().drawBehind {
                val h = hold.value
                if (h <= 0f || carried) return@drawBehind
                edgeArc(SolidColor(stopInk), h, LIVE_STROKE.toPx() * commitPop.value * (if (stopping) 14f / 12f else 1f), LIVE_STROKE.toPx())
                // The stop symbol grows in at the centre, in its disc, as the countdown gives way to it.
                val s = swap(h)
                drawCircle(stopInk.copy(alpha = 0.22f), STOP_DISC.toPx() / 2 * lerp(0.6f, 1f, s) * commitPop.value, center - Offset(0f, STOP_LIFT.toPx()), alpha = s)
                val side = STOP_MARK.toPx() * lerp(0.6f, 1f, s) * commitPop.value * stopBreath.value
                drawRoundRect(stopInk, center - Offset(side / 2, side / 2 + STOP_LIFT.toPx()), Size(side, side), CornerRadius(side * 7f / 26f), alpha = s)
            })
            Column(
                Modifier.graphicsLayer {
                    val p = enter.value
                    val s = swap(hold.value)
                    alpha = p * (1 - s)
                    scaleX = lerp(0.9f, 1f, p) * (1 - 0.1f * s)
                    scaleY = scaleX
                }.padding(padding),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Counts down to the target, then shows the total: big rounded numerals in the ring's colour, white with no
                // model. Tabular digits, so the time doesn't shift sideways as it ticks.
                Text(
                    duration(shownS),
                    Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                    color = if (status.timerOnly) Color.White else ringColor,
                    style = FiveType.display,
                )
                Text(
                    if (full) stringResource(R.string.target_reached, RING_TARGET_S.toInt()) else stringResource(R.string.time_left),
                    color = Five.TextSecondary,
                    style = FiveType.labelLarge,
                )
                if (status.timerOnly) Text(stringResource(R.string.timer_only), Modifier.padding(top = 8.dp, start = 30.dp, end = 30.dp), color = Five.TextSecondary, style = FiveType.labelSmall, textAlign = TextAlign.Center)
                // The cue (a movement or a cheer) owns this line; it gives way to nothing but the stop hold.
                else Box(Modifier.padding(top = 8.dp, start = 30.dp, end = 30.dp).heightIn(min = 36.dp), contentAlignment = Alignment.TopCenter) {
                    AnimatedContent(
                        cue,
                        Modifier.graphicsLayer { alpha = if (stopping) 0f else 1f },
                        transitionSpec = {
                            (fadeIn(tween(320, delayMillis = 80)) + slideInVertically(tween(420, easing = EaseOutStrong)) { it / 2 }) togetherWith
                                (fadeOut(tween(160)) + slideOutVertically(tween(260, easing = EaseOutStrong)) { -it / 3 })
                        },
                        label = "cue",
                    ) {
                        Text(it, color = Color.White, style = FiveType.body, textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
            }
            // "Hold to stop", said once near the bottom arc in the first seconds, then gone.
            Text(
                stringResource(R.string.hold_to_stop),
                Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).graphicsLayer { alpha = hint.value * (1 - swap(hold.value)) },
                color = Five.TextTertiary,
                style = FiveType.labelSmall,
            )
            // Under the stop square while holding: "Hold to stop", before the target a nudge to keep going (never a block),
            // and "Stopping…" once committed.
            Text(
                stringResource(when { stopping -> R.string.stopping; !full -> R.string.stop_early; else -> R.string.hold_to_stop }),
                Modifier.align(Alignment.TopCenter)
                    .padding(top = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } / 2 - STOP_LIFT + STOP_DISC / 2 + 12.dp, start = 34.dp, end = 34.dp)
                    .graphicsLayer { alpha = if (stopping) 1f else swap(hold.value) },
                color = Color.White,
                style = FiveType.body,
                textAlign = TextAlign.Center,
            )
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
                        .background(Brush.linearGradient(START_GRADIENT)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_wash), null, Modifier.size(20.dp), tint = Five.OnTint)
                    Spacer(Modifier.width(7.dp))
                    Text(stringResource(R.string.start_wash), color = Five.OnTint, style = FiveType.bodyLarge, maxLines = 1)
                }
            }
        }
    }
}

/** A Wash's Summary: the one just ended ([fromWash]), or one from History. */
@Composable
private fun SummaryScreen(id: Long?, fromWash: Boolean = false, onGuide: (Int) -> Unit) {
    val context = LocalContext.current
    val wash = remember(id) {
        WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(context).read().find { it.id == id }
    }
    val steps = stringArrayResource(R.array.steps)
    val focus = remember(id) { History.get(context).read().let { h -> focusStep(h)?.let { it to focusCounts(h, it) } } }
    val focusLabel = focus?.first
    // Rows arrive in a short cascade as the end pulses play. Decorative: scrolling is never blocked.
    val entrance = remember { Animatable(0f) }
    // C6: straight from a Wash with the live ring on, that ring shrinks into the Coverage rings (0 = live, 1 = landed).
    val stoppedByHold = remember { fromWash && StopHold.committed.also { StopHold.committed = false } }
    val morph = remember { Animatable(if (fromWash && wash?.coverageShown == true && Settings(context).progressRing) 0f else 1f) }
    LaunchedEffect(Unit) { morph.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    // The rings and rows start once the live ring is most of the way in, so they fill where they can be seen.
    LaunchedEffect(Unit) {
        snapshotFlow { morph.value }.first { it >= ENTRANCE_AT }
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    // Ended by a hold, the stop square becomes the answer: it shrinks into the rings' centre and the words grow in under them.
    fun numeral() = if (stoppedByHold) EaseOutStrong.transform(((morph.value - 0.4f) / 0.6f).coerceIn(0f, 1f)) else 1f
    // The rings' centre in this screen's space, so a moving nav transition doesn't throw the morph off.
    var ringCentre by remember { mutableStateOf(Offset.Unspecified) }
    var screen by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val estimated = wash != null && wash.modelVersion != null && wash.coverageShown
    val all = estimated && allCovered(wash!!.coverage)
    Box(Modifier.onGloballyPositioned { screen = it }) {
        // Mint once all five look covered; a faint white wash when there are no rings to show.
        ListScreen(if (all) Five.Done else if (estimated) Five.Tint else Color.White, if (estimated) 0.26f else 0.1f) { spec ->
            if (wash == null) return@ListScreen
            if (!estimated) {
                // End states, no rings: the duration is the answer, then what happened, why in one line, and a way out.
                item { Text(duration(wash.durationS), morphContent(spec).padding(top = 6.dp), style = FiveType.displaySmall, textAlign = TextAlign.Center) }
                item {
                    Column(morphContent(spec).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(if (wash.modelVersion == null) R.string.timer_only_short else R.string.too_short), style = FiveType.bodyLarge, textAlign = TextAlign.Center)
                        Text(stringResource(if (wash.modelVersion == null) R.string.timer_only_why else R.string.too_short_why), color = Five.TextSecondary, style = FiveType.label.copy(lineHeight = 1.3.em), textAlign = TextAlign.Center)
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = { (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() },
                        modifier = morph(spec).padding(horizontal = 36.dp).height(48.dp),
                        transformation = SurfaceTransformation(spec),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Five.PlatterRaised, contentColor = Color.White),
                        label = { Text(stringResource(R.string.close), Modifier.fillMaxWidth(), style = FiveType.body, textAlign = TextAlign.Center) },
                    )
                }
            } else {
                // Clear of the clock, so the outer ring is not tucked under it.
                item { Spacer(Modifier.height(6.dp)) }
                item {
                    CoverageRing(wash.coverage, { entrance.value }, morphContent(spec)
                        .onGloballyPositioned { ring -> screen?.let { ringCentre = it.localBoundingBoxOf(ring).center } }
                        .graphicsLayer { alpha = morph.value })
                }
                // The answer in words under the rings; all five, the praise line in mint. The duration is context, quieter.
                item {
                    Column(morphContent(spec).graphicsLayer { val n = numeral(); alpha = n; scaleX = lerp(0.8f, 1f, n); scaleY = scaleX }, horizontalAlignment = Alignment.CenterHorizontally) {
                        val ok = wash.coverage.count { it.status == StepStatus.OK }
                        Text(
                            if (all) stringResource(R.string.praise) else stringResource(R.string.history_hint, ok),
                            Modifier.padding(top = 7.dp, start = 20.dp, end = 20.dp),
                            color = if (all) Five.Done else Color.White,
                            style = if (all) FiveType.headlineSmall.copy(fontSize = 20.sp) else FiveType.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                        Text(stringResource(R.string.washed_for, duration(wash.durationS)), Modifier.padding(top = 2.dp, bottom = 10.dp), color = Five.TextSecondary, style = FiveType.label)
                    }
                }
                // Only Steps that need a look get a row, the Focus Step first with its history line; the covered ones fold
                // into one line, since they all say the same. All five covered: all five stay as rows, so every guide is a tap away.
                val missed = wash.coverage.filter { it.status != StepStatus.OK }
                val attention = (missed.ifEmpty { wash.coverage } + wash.coverage.filter { c -> c.label == focusLabel && missed.isNotEmpty() && c.status == StepStatus.OK })
                    .sortedBy { if (it.label == focusLabel && missed.isNotEmpty()) 0 else 1 }
                items(attention.size) { i ->
                    val c = attention[i]
                    val isFocus = c.label == focusLabel && !all
                    StepRow(
                        c, steps[c.label - 1], focus?.second?.takeIf { isFocus }, { staggered(entrance.value, i) },
                        morphContent(spec).rise({ entrance.value }, i),
                    ) { onGuide(c.label) }
                }
                val covered = wash.coverage.filter { c -> c.status == StepStatus.OK && attention.none { it.label == c.label } }
                if (covered.isNotEmpty()) item {
                    // Opens the guides at the first covered Step; the pager walks the rest. It arrives last in the cascade.
                    val at = attention.size
                    Row(
                        morphContent(spec).rise({ entrance.value }, at).clip(PLATTER).border(1.dp, Five.Hairline, PLATTER)
                            .clickable(onClickLabel = stringResource(R.string.open_guide, steps[covered.first().label - 1]), role = Role.Button) { onGuide(covered.first().label) }
                            .heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 8.dp).semantics(mergeDescendants = true) {},
                        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlyphDisc(Color.White.copy(alpha = 0.1f)) { CheckMark(Color.White) { staggered(entrance.value, at) } }
                        Text(covered.joinToString(", ") { steps[it.label - 1] }, Modifier.weight(1f), color = Five.TextSecondary, style = FiveType.label)
                    }
                }
            }
            if (!wash.worthSaving()) item { Text(stringResource(R.string.too_short_to_save), morphContent(spec).padding(top = 6.dp, start = 30.dp, end = 30.dp), color = Five.TextSecondary, style = FiveType.labelSmall, textAlign = TextAlign.Center) }
        }
        if (morph.value < 1f) LiveRing(wash!!.durationS, { morph.value }, { ringCentre }, stoppedByHold)
        if (stoppedByHold && morph.value < 1f) {
            Canvas(Modifier.fillMaxSize()) {
                val p = EaseOutStrong.transform(morph.value)
                val from = Offset(size.width / 2, size.height / 2 - STOP_LIFT.toPx())
                val c = lerp(from, ringCentre.takeIf { it.isSpecified } ?: from, p)
                val side = STOP_MARK.toPx() * lerp(1f, 0.4f, p)
                drawRoundRect(Five.Stop, c - Offset(side / 2, side / 2), Size(side, side), CornerRadius(side * 7f / 26f), alpha = 1 - numeral())
            }
        }
    }
}

/** DESIGN.md's platter: 24 dp corners, so a 50 dp row reads almost as a capsule. */
private val PLATTER = RoundedCornerShape(24.dp)

/**
 * A Step that needs a look, as a platter: its glyph in its ring's colour (filled when covered), the name, the Status as a
 * mark and in words (§4.1.10), and for the Focus Step its history line ([focus]: times in the last Washes) on a platter of
 * its colour. Opens that Step's guide.
 */
@Composable
private fun StepRow(c: StepCoverage, name: String, focus: Pair<Int, Int>?, fill: () -> Float, modifier: Modifier, onClick: () -> Unit) {
    val ok = c.status == StepStatus.OK
    val color = Five.step(c.label)
    Row(
        modifier.clip(PLATTER).background(if (focus != null) color.copy(alpha = 0.2f) else Five.Platter)
            .clickable(onClickLabel = stringResource(R.string.open_guide, name), role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 8.dp).semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphDisc(if (ok) color else color.copy(alpha = 0.2f)) { Icon(painterResource(STEP_ICONS[c.label - 1]), null, Modifier.size(19.dp), tint = if (ok) Color.Black else color) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(name, style = FiveType.body)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                // Fills as the row rises, the way its ring fills above.
                StatusMark(c.status, color, fill)
                Text(stringResource(statusText(c.status)), color = Five.TextSecondary, style = FiveType.label)
            }
            if (focus != null) Text(stringResource(R.string.focus_row, focus.first, focus.second), color = Five.stepLight(c.label), style = FiveType.labelSmall.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

/**
 * The Step guides, one page per Step in canonical order, opened on the tapped Step. They page vertically with the crown
 * or a swipe, dots on the right edge as in watchOS, so one tap reaches all five (a fully covered Wash still has a way
 * to every guide), and a swipe right is back, as everywhere else. Each page takes its Step's colour: name, Hegasy crop
 * (CC BY-SA, separate file; D34), one-line technique, one why line.
 */
@Composable
private fun GuideScreen(label: Int?) {
    if (label == null || label !in 1..5) return
    val pager = rememberPagerState(initialPage = label - 1) { 5 }
    VerticalPagerScaffold(
        pager,
        Modifier.background(Color.Black),
        pageIndicator = { VerticalPageIndicator(pager, selectedColor = Five.step(pager.currentPage + 1), unselectedColor = Five.TextTertiary) },
    ) {
        VerticalPager(pager, Modifier.fillMaxSize()) { page -> GuidePage(page + 1) { pager.currentPage - page + pager.currentPageOffsetFraction } }
    }
}

/** One Step's guide page. [offset] is how far the page is from the middle of the pager, in pages, signed. */
@Composable
private fun GuidePage(label: Int, offset: () -> Float = { 0f }) {
    val color = Five.step(label)
    val scroll = rememberScrollState()
    val compact = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 220.dp
    val art = if (compact) 46.dp else 52.dp
    val page = stringResource(R.string.page_of, label, 5)
    // Scrolls if it must: on a small screen, or at a large font size, the title, technique and why line may not all fit.
    Column(
        // The last 24 dp fade out, so text that runs past the round edge dissolves rather than being cut by a hard edge.
        Modifier.fillMaxSize().tintWash(color, 0.33f).graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
            drawContent()
            val fade = 24.dp.toPx()
            drawRect(Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = size.height - fade, endY = size.height), blendMode = BlendMode.DstIn)
        }.verticalScroll(scroll).padding(start = 30.dp, end = 30.dp, top = if (compact) 24.dp else 27.dp, bottom = 24.dp).semantics(mergeDescendants = true) {
            stateDescription = page
        },
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The picture leads, ringed in its Step's colour: a circle sits in the narrow top of a round screen where a line of
        // text would meet the clock. Paging, it shrinks and rolls a little with the swipe, as if the hands turned over.
        Box(
            Modifier.size(art + 6.dp).graphicsLayer {
                val o = offset().coerceIn(-1f, 1f)
                val k = lerp(1f, 0.7f, kotlin.math.abs(o))
                scaleX = k; scaleY = k
                rotationZ = -16f * o
            }.clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(STEP_ART[label - 1]), null, Modifier.size(art).clip(CircleShape).background(Color(0xFFEEEEEE)))
        }
        Text(stringArrayResource(R.array.steps)[label - 1], Modifier.padding(top = 2.dp), style = FiveType.title.copy(fontSize = 16.5.sp), color = color, textAlign = TextAlign.Center)
        Text(stringArrayResource(R.array.step_how)[label - 1], style = FiveType.bodySmall.copy(fontSize = 13.5.sp), textAlign = TextAlign.Center)
        Text(stringArrayResource(R.array.step_whys)[label - 1], color = Five.TextSecondary, style = FiveType.label.copy(fontSize = 11.5.sp), textAlign = TextAlign.Center)
    }
}

/**
 * The In-progress ring as it was when the Wash ended, drawn over the Summary and shrinking into the
 * Coverage ring at [centre] as [morph] goes 0 → 1, fading as it goes. Same geometry as WashScreen's.
 */
@Composable
private fun LiveRing(durationS: Float, morph: () -> Float, centre: () -> Offset, stopped: Boolean) {
    val base = if (durationS >= RING_TARGET_S) Five.Done else Five.Tint
    val stop = Five.Stop
    Canvas(Modifier.fillMaxSize()) {
        val p = morph()
        // From the live ring at the edge into the outermost of the Five Rings.
        val stroke = lerp(LIVE_STROKE.toPx(), RING_STROKE.toPx(), p)
        val r = lerp(size.minDimension / 2 - LIVE_INSET.toPx() - LIVE_STROKE.toPx() / 2, RING_SIZE.toPx() / 2 - RING_STROKE.toPx() / 2, p)
        val c = lerp(center, centre().takeIf { it.isSpecified } ?: center, p)
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
        // Nothing yet: the glyph and the one sentence.
        if (washes.isEmpty()) item {
            Column(morphContent(spec).padding(horizontal = 28.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlyphDisc(Five.Tint.copy(alpha = 0.16f), 46.dp) { Icon(painterResource(R.drawable.ic_history), null, Modifier.size(24.dp), tint = Five.Tint) }
                Text(stringResource(R.string.history_empty), color = Five.TextSecondary, style = FiveType.label.copy(lineHeight = 1.3.em), textAlign = TextAlign.Center)
            }
        }
        // Newest first, one small day label per run of Washes so the rows don't repeat the date.
        val zone = java.time.ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        washes.groupBy { java.time.Instant.ofEpochMilli(it.startedAtEpochMs).atZone(zone).toLocalDate() }.forEach { (_, day) ->
            item {
                Text(
                    DateUtils.getRelativeTimeSpanString(day.first().startedAtEpochMs, now, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString(),
                    morphContent(spec).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 2.dp).semantics { heading() },
                    color = Five.TextSecondary,
                    style = FiveType.label,
                )
            }
            items(day) { w ->
                // The outcome leads (tiny rings, then the fraction); the time and length follow. Washes with no Coverage are
                // dashed and dimmed.
                FilledTonalButton(
                    onClick = { onOpen(w.id) },
                    modifier = morph(spec).then(if (w.coverageShown) Modifier else Modifier.graphicsLayer { alpha = 0.85f }),
                    transformation = SurfaceTransformation(spec),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Five.Platter, contentColor = Color.White, secondaryContentColor = Five.TextSecondary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    // Empty rings would read as a score of zero: no Step looked covered gets the dashed circle too.
                    icon = { if (w.coverageShown && w.coverage.any { it.status == StepStatus.OK }) FiveRings(w.coverage, HISTORY_RING_SIZE) else DashedDisc() },
                    label = { Text(hint(LocalContext.current.resources, w), maxLines = 2, style = FiveType.bodySmall.copy(fontWeight = FontWeight.SemiBold)) },
                    secondaryLabel = { Text("${DateUtils.formatDateTime(context, w.startedAtEpochMs, DateUtils.FORMAT_SHOW_TIME)} · ${duration(w.durationS)}", maxLines = 1, style = FiveType.label) },
                )
            }
        }
    }
}

/** A Wash with no Coverage, in place of its rings: a dashed circle, the same mark as Not detected. */
@Composable
private fun DashedDisc() = Canvas(Modifier.size(32.dp).clearAndSetSemantics {}) {
    val line = 1.5.dp.toPx()
    drawCircle(Five.TextTertiary, size.minDimension / 2 - line / 2, style = Stroke(line, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))))
}

@Composable
private fun CreditsScreen() {
    ListScreen(wash = null) { spec ->
        item { Header(spec, stringResource(R.string.credits), Color.White) }
        // The five illustrations as a strip, each ringed in its Step's colour.
        item {
            Row(morphContent(spec).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
                STEP_ART.forEachIndexed { i, art ->
                    Box(Modifier.size(30.dp).clip(CircleShape).background(Five.Steps[i]), contentAlignment = Alignment.Center) {
                        Image(painterResource(art), null, Modifier.size(26.dp).clip(CircleShape))
                    }
                }
            }
        }
        // The URL has no spaces; a zero-width space after "/" and "_" lets it wrap on the round edge instead of mid-word.
        item {
            Column(morphContent(spec).padding(horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.credits_illustrations), color = Five.TextSecondary, style = FiveType.labelSmall)
                Text(stringResource(R.string.credits_author), style = FiveType.bodySmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.credits_licence), color = Five.TextSecondary, style = FiveType.label, textAlign = TextAlign.Center)
                Text(stringResource(R.string.credits_body).substringAfter("cropped. ").substringAfter("recortadas. ").replace("/", "/​").replace("_", "_​"), Modifier.padding(top = 6.dp), color = Five.TextTertiary, style = FiveType.labelSmall.copy(lineHeight = 1.3.em), textAlign = TextAlign.Center)
            }
        }
    }
}

private val HUB_ROW_MIN = 54.dp
private val TOGGLE_ROW_MIN = 56.dp

/**
 * A settings row arrives: it rises into place on a spring with a trace of overshoot, [STAGGER_MS] after the row above.
 * The same rise as Start's and the Summary's rows, so the app has one way of arriving; the bounce is saved for the toggles.
 */
@Composable
private fun settle(i: Int): Modifier {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // Animator scale 0 finishes the spring at once; the stagger delay is not scaled, so skip it.
        if (coroutineContext[MotionDurationScale]?.scaleFactor != 0f) delay(i * STAGGER_MS.toLong())
        p.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
    }
    return Modifier.graphicsLayer {
        alpha = (p.value * 2f).coerceIn(0f, 1f)
        val k = 0.94f + 0.06f * p.value
        scaleX = k; scaleY = k
        translationY = (1 - p.value) * 14.dp.toPx()
    }
}

/** A short springy swell each time [key] changes (not on first composition): the row a toggle just flipped. */
@Composable
private fun swell(key: Any?): Modifier {
    val s = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(key) {
        if (first) first = false else { s.snapTo(1.07f); s.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium)) }
    }
    return Modifier.graphicsLayer { scaleX = s.value; scaleY = s.value }
}

/** A settings row's modifier and interaction source: edge morph, [settle], and the press shrink. */
@Composable
private fun TransformingLazyColumnItemScope.settingRow(spec: TransformationSpec, i: Int): Pair<MutableInteractionSource, Modifier> {
    val (source, press) = rememberPressScale()
    return source to morph(spec).then(settle(i)).then(press)
}

/** Every settings row is a platter: white words, grey details, values in the tint. */
@Composable
private fun platterColors() = ButtonDefaults.filledTonalButtonColors(containerColor = Five.Platter, contentColor = Color.White, secondaryContentColor = Five.TextSecondary, iconColor = Color.White)

/** Toggles take the app tint, never iOS green, on a platter row whichever way they are set. */
@Composable
private fun toggleColors() = SwitchButtonDefaults.switchButtonColors(
    checkedContainerColor = Five.Platter,
    checkedContentColor = Color.White,
    checkedSecondaryContentColor = Five.TextSecondary,
    checkedThumbColor = Color.White,
    checkedThumbIconColor = Five.Tint,
    checkedTrackColor = Five.Tint,
    checkedTrackBorderColor = Five.Tint,
    uncheckedContainerColor = Five.Platter,
    uncheckedContentColor = Color.White,
    uncheckedSecondaryContentColor = Five.TextSecondary,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Five.SwitchOff,
    uncheckedTrackBorderColor = Five.SwitchOff,
)

/** A hub row: a coloured glyph disc as in watchOS Settings, the label, and an optional [value] at the end. */
@Composable
private fun TransformingLazyColumnItemScope.HubRow(spec: TransformationSpec, i: Int, glyph: Int, disc: Color, label: String, onClick: () -> Unit, spin: Float = 0f, value: String? = null) {
    val (source, modifier) = settingRow(spec, i)
    FilledTonalButton(
        onClick = onClick, modifier = modifier.heightIn(min = HUB_ROW_MIN), interactionSource = source, transformation = SurfaceTransformation(spec), colors = platterColors(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        icon = { GlyphDisc(disc) { Icon(painterResource(glyph), null, Modifier.size(19.dp).graphicsLayer { rotationZ = spin }, tint = Color.White) } },
        label = {
            Text(label, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, style = FiveType.body)
            if (value != null) Text(value, color = Five.Tint, style = FiveType.bodySmall.copy(fontWeight = FontWeight.Bold))
        },
    )
}

/** Settings is a short hub: two groups by when a setting matters, Language as an inline value, Credits as quiet text. */
@Composable
private fun SettingsScreen(onWash: () -> Unit, onStart: () -> Unit, onCredits: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val feedback = LocalHapticFeedback.current
    var spanish by remember { mutableStateOf(context.resources.configuration.locales[0].language == "es") }
    // The globe spins as the value flips. The locale applies once the spin lands, since it recreates the screen.
    val spin by animateFloatAsState(if (spanish) 360f else 0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
    ListScreen(Five.HubGrey) { spec ->
        item { Header(spec, stringResource(R.string.settings), Color.White) }
        item { HubRow(spec, 1, R.drawable.ic_vibration, Five.Steps[1], stringResource(R.string.settings_wash), onWash) }
        item { HubRow(spec, 2, R.drawable.ic_wash, Five.TintDeep, stringResource(R.string.settings_start), onStart) }
        // Per-app language needs LocaleManager (API 33); older watches keep following the system language.
        if (Build.VERSION.SDK_INT >= 33) item {
            HubRow(spec, 3, R.drawable.ic_language, Five.HubIndigo, stringResource(R.string.language), {
                spanish = !spanish
                feedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                val tag = if (spanish) "es" else "en"
                scope.launch {
                    delay(420)
                    context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tag)
                }
            }, spin, if (spanish) "ES" else "EN")
        }
        item {
            val (source, modifier) = settingRow(spec, 4)
            FilledTonalButton(onClick = onCredits, modifier = modifier, interactionSource = source, transformation = SurfaceTransformation(spec),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.Transparent, contentColor = Five.TextSecondary),
                label = { Text(stringResource(R.string.credits), Modifier.fillMaxWidth(), style = FiveType.bodySmall, textAlign = TextAlign.Center) })
        }
    }
}

/** What the watch does during a Wash: plain toggle rows, no glyphs, so long Spanish labels get the width. */
@Composable
private fun WashSettingsScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    val status by WashService.status.collectAsState()
    var haptics by remember { mutableStateOf(settings.haptics) }
    var ring by remember { mutableStateOf(settings.progressRing) }
    var targetCheck by remember { mutableStateOf(settings.targetCheck) }
    val feedback = LocalHapticFeedback.current
    fun tick(on: Boolean) = feedback.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
    ListScreen(Five.Steps[1], 0.26f) { spec ->
        item { Header(spec, stringResource(R.string.settings_wash), Five.Steps[1]) }
        item {
            val (source, modifier) = settingRow(spec, 1)
            SwitchButton(haptics, { haptics = it; settings.haptics = it; tick(it) }, modifier.heightIn(min = TOGGLE_ROW_MIN).then(swell(haptics)), interactionSource = source, transformation = SurfaceTransformation(spec), colors = toggleColors(),
                label = { Text(stringResource(R.string.haptics), style = FiveType.body) })
        }
        item {
            val (source, modifier) = settingRow(spec, 2)
            SwitchButton(ring, { ring = it; settings.progressRing = it; tick(it) }, modifier.heightIn(min = TOGGLE_ROW_MIN).then(swell(ring)), interactionSource = source, transformation = SurfaceTransformation(spec), colors = toggleColors(),
                label = { Text(stringResource(R.string.progress_ring), style = FiveType.body) },
                secondaryLabel = { Text(stringResource(R.string.progress_ring_desc), style = FiveType.labelSmall) })
        }
        // Absent entirely when the build flag ships the check disabled.
        if (BuildConfig.TARGET_CHECK) item {
            val (source, modifier) = settingRow(spec, 3)
            SwitchButton(targetCheck, { targetCheck = it; settings.targetCheck = it; tick(it) }, modifier.heightIn(min = TOGGLE_ROW_MIN).then(swell(targetCheck)), interactionSource = source, transformation = SurfaceTransformation(spec), colors = toggleColors(),
                label = { Text(stringResource(R.string.target_check), style = FiveType.body) })
        }
        // Only said when it matters: the ring is read at Wash start.
        if (status.running) item { Footnote(stringResource(R.string.applies_next_wash), morphContent(spec)) }
    }
}

/** How a Wash begins: Twist start and Wrist side. Both are set once. */
@Composable
private fun StartSettingsScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    val status by WashService.status.collectAsState()
    var wrist by remember { mutableStateOf(settings.wrist) }
    var twist by remember { mutableStateOf(settings.twistStart) }
    var warn by remember { mutableStateOf(false) }
    fun setTwist(on: Boolean) {
        twist = on
        settings.twistStart = on
        if (on) WashService.listen(context) else WashService.unlisten(context)
    }
    val feedback = LocalHapticFeedback.current
    ListScreen(Five.TintDeep) { spec ->
        item { Header(spec, stringResource(R.string.settings_start), Five.HubBlueText) }
        // Turning it on asks first (§4.1 item 8); turning it off doesn't. The secondary line is the background-listening disclosure.
        item {
            val (source, modifier) = settingRow(spec, 1)
            SwitchButton(twist, { on -> if (on) warn = true else { setTwist(false); feedback.performHapticFeedback(HapticFeedbackType.ToggleOff) } },
                modifier.heightIn(min = TOGGLE_ROW_MIN).then(swell(twist)), interactionSource = source, transformation = SurfaceTransformation(spec), colors = toggleColors(),
                label = { Text(stringResource(R.string.twist_start), style = FiveType.body) },
                secondaryLabel = { Text(stringResource(R.string.twist_start_desc), style = FiveType.labelSmall) })
        }
        // Two values, so a tap flips it and the row shows the new one, in the tint: no dialog, and a second tap undoes it (§4.1.5).
        item {
            val (source, modifier) = settingRow(spec, 2)
            FilledTonalButton(
                onClick = { wrist = if (wrist == Wrist.LEFT) Wrist.RIGHT else Wrist.LEFT; settings.wrist = wrist; feedback.performHapticFeedback(HapticFeedbackType.ToggleOn) },
                modifier = modifier.heightIn(min = TOGGLE_ROW_MIN).then(swell(wrist)),
                interactionSource = source,
                colors = platterColors(),
                transformation = SurfaceTransformation(spec),
                label = {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.wrist_side), style = FiveType.body)
                        Crossfade(wrist, label = "wrist") { Text(stringResource(if (it == Wrist.LEFT) R.string.wrist_left else R.string.wrist_right), color = Five.Tint, style = FiveType.label) }
                    }
                    SettingIcon(R.drawable.ic_watch, flip = wrist == Wrist.RIGHT, tint = Five.TextSecondary)
                },
            )
        }
        if (status.running) item { Footnote(stringResource(R.string.applies_next_wash), morphContent(spec)) }
    }
    TwistSheet(warn, onCancel = { warn = false }, onConfirm = { warn = false; setTwist(true); feedback.performHapticFeedback(HapticFeedbackType.ToggleOn) })
}

/**
 * The Twist battery warning as a watchOS-style sheet: the cost first, then glass ✕ and tinted ✓ on the bottom arc. Still
 * a two-option choice (§4.1.5); swiping it away, like ✕, leaves Twist start off.
 */
@Composable
private fun TwistSheet(visible: Boolean, onCancel: () -> Unit, onConfirm: () -> Unit) = Dialog(visible = visible, onDismissRequest = onCancel) {
    val amber = Five.Steps[2]
    Box(Modifier.fillMaxSize().background(Color.Black).tintWash(amber, 0.28f)) {
        Column(Modifier.fillMaxWidth().padding(top = 30.dp, start = 26.dp, end = 26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            // A battery, a third full: the cost before the words.
            Canvas(Modifier.size(34.dp, 20.dp).clearAndSetSemantics {}) {
                val u = size.width / 34f
                drawRoundRect(amber, Offset(1 * u, 1 * u), Size(28 * u, 18 * u), CornerRadius(5 * u), style = Stroke(2 * u))
                drawRoundRect(amber, Offset(4.5f * u, 4.5f * u), Size(8 * u, 11 * u), CornerRadius(2 * u))
                drawRoundRect(amber, Offset(30.5f * u, 7 * u), Size(2.5f * u, 6 * u), CornerRadius(1 * u))
            }
            Text(stringResource(R.string.twist_warning_title), Modifier.padding(top = 2.dp).semantics { heading() }, style = FiveType.bodyLarge, textAlign = TextAlign.Center)
            Text(stringResource(R.string.twist_warning), color = Five.TextSecondary, style = FiveType.label.copy(lineHeight = 1.28.em), textAlign = TextAlign.Center)
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 11.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            FilledTonalIconButton(onCancel, Modifier.size(50.dp), shapes = IconButtonDefaults.animatedShapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Five.Glass, contentColor = Color.White)) {
                SheetMark(stringResource(android.R.string.cancel), Color.White) { s ->
                    moveTo(3 * s, 3 * s); lineTo(15 * s, 15 * s); moveTo(15 * s, 3 * s); lineTo(3 * s, 15 * s)
                }
            }
            FilledIconButton(onConfirm, Modifier.size(50.dp), shapes = IconButtonDefaults.animatedShapes(),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Five.Tint, contentColor = Five.OnTint)) {
                SheetMark(stringResource(R.string.twist_start), Five.OnTint) { s ->
                    moveTo(2.5f * s, 9.5f * s); lineTo(6.7f * s, 13.7f * s); lineTo(15.5f * s, 4.5f * s)
                }
            }
        }
    }
}

/** A sheet button's drawn mark on an 18-unit grid ([draw] gets the unit's size), read as [label]. */
@Composable
private fun SheetMark(label: String, color: Color, draw: Path.(Float) -> Unit) {
    val path = remember { Path() }
    Canvas(Modifier.size(18.dp).semantics { contentDescription = label }) {
        path.reset()
        path.draw(size.width / 18f)
        drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A line under a list, quieter than the rows: when a change takes effect. */
@Composable
private fun Footnote(text: String, modifier: Modifier) =
    Text(text, modifier.padding(horizontal = 30.dp, vertical = 4.dp), color = Five.TextSecondary, style = FiveType.labelSmall, textAlign = TextAlign.Center)

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
 * The Five Rings: one concentric ring per Step, outer = Palms, each in its own colour. Shape carries the Status, not
 * colour (§4.1.10): Looks covered closes the ring, May have been brief fills half, Not detected leaves only the track,
 * like the [StatusMark]s below. The rows say it in words.
 */
@Composable
private fun CoverageRing(coverage: List<StepCoverage>, entrance: () -> Float, modifier: Modifier) {
    val ok = coverage.count { it.status == StepStatus.OK }
    val takeaway = stringResource(R.string.history_hint, ok)
    // All five closed: once the last ring lands, a light passes over the five rings in order, a roll call, and as it
    // leaves the last one the rings give one small spring pop and a splash of droplets in the Step colours.
    val pop = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    val roll = remember { Animatable(0f) }
    if (ok == coverage.size) LaunchedEffect(Unit) {
        snapshotFlow(entrance).first { it >= 1f } // follows the system animation scale, unlike a fixed delay
        launch { roll.animateTo(1f, tween(ROLL_MS, easing = LinearEasing)) }
        snapshotFlow { roll.value }.first { it >= ROLL_BURST_AT }
        launch { burst.snapTo(0f); burst.animateTo(1f, tween(BURST_MS, easing = EaseOutStrong)) }
        pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f)
    }
    // Each ring carries its Step's glyph at its start, so the colours can be read without the rows: Palms outside, Fingertips in.
    val glyphs = STEP_ICONS.map { painterResource(it) }
    val scope = rememberCoroutineScope()
    Box(
        modifier.height(RING_SIZE).clearAndSetSemantics { contentDescription = takeaway }
            // A fidget: tap the rings and they give the same small spring pop as a full wash. Not a control, so no semantics.
            .pointerInput(Unit) { detectTapGestures { scope.launch { pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f) } } },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(RING_SIZE).graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
            fiveRings(coverage, { staggered(entrance(), it) }, glyphs) { i ->
                // The light's place runs from before the first ring to past the last, so it enters and leaves dark.
                val w = roll.value * (coverage.size + 2) - 1
                (1 - kotlin.math.abs(w - i)).coerceIn(0f, 1f)
            }
        }
        // Drawn past the rings' bounds: the droplets leave from the outer edge.
        Canvas(Modifier.size(RING_SIZE)) {
            val b = burst.value
            if (b < 1f) repeat(BURST_DROPS) { i ->
                val a = Math.toRadians(-90.0 + i * 360.0 / BURST_DROPS)
                // Three reaches and two sizes, so the splash reads as water, not a clock face.
                val r = size.minDimension / 2 + BURST_REACH.toPx() * b * (1f - 0.15f * (i % 3))
                val drop = (if (i % 2 == 0) 4.dp else 3.dp).toPx() * (1 - b)
                drawCircle(Five.Steps[i % Five.Steps.size], drop, center + Offset(cos(a).toFloat(), sin(a).toFloat()) * r, alpha = 1 - b)
            }
        }
    }
}

/** The Five Rings at [size], for Start, History and anywhere the last Wash reads at a glance. [fill] is each ring's drawn share. */
@Composable
private fun FiveRings(coverage: List<StepCoverage>, size: Dp, fill: (Int) -> Float = { 1f }) =
    Canvas(Modifier.size(size).clearAndSetSemantics {}) { fiveRings(coverage, fill) }

/**
 * The Five Rings filling this canvas, each Step on its own radius by its label (outer = Palms), stroke and gap in
 * proportion to the size ([RING_STROKE_OF], [RING_GAP_OF]). A 20 % track of the Step's colour, then the fill from 12
 * o'clock, lit along the diagonal from the colour to its light partner. [fill] is the drawn part of each ring's share,
 * for the entrances; [light] lays white over ring i for the roll call. The Tile draws the same rings ([WashTileService]).
 */
private fun DrawScope.fiveRings(coverage: List<StepCoverage>, fill: (Int) -> Float = { 1f }, glyphs: List<Painter>? = null, light: (Int) -> Float = { 0f }) {
    val stroke = size.minDimension * RING_STROKE_OF
    val gap = size.minDimension * RING_GAP_OF
    coverage.forEachIndexed { i, c ->
        val k = c.label - 1
        val r = size.minDimension / 2 - stroke / 2 - k * (stroke + gap)
        val topLeft = center - Offset(r, r)
        val arc = Size(2 * r, 2 * r)
        drawCircle(Five.Steps[k], r, alpha = 0.2f, style = Stroke(stroke))
        val share = share(c.status)
        val sweep = 360f * share * fill(i)
        if (sweep > 0f) drawArc(diagonal(Five.Steps[k], Five.StepsLight[k]), -90f, sweep, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
        val l = light(i)
        if (l > 0f && share > 0f) drawArc(Color.White, -90f, 360f * share, false, topLeft, arc, alpha = 0.6f * l, style = Stroke(stroke, cap = StrokeCap.Round))
        // A glint rides the head of the arc while it draws, and is gone when it lands.
        val f = fill(i)
        if (share > 0f && f > 0f && f < 1f) {
            val a = Math.toRadians(-90.0 + sweep)
            drawCircle(Color.White, stroke * 0.36f, center + Offset(cos(a).toFloat(), sin(a).toFloat()) * r, alpha = 0.85f * sin(PI.toFloat() * f))
        }
        // The Step's glyph sits on the arc's start cap (12 o'clock): dark on a lit arc, the Step's colour on a bare track.
        glyphs?.let {
            val t = (f * 3f).coerceIn(0f, 1f)
            val g = stroke * 0.82f * (0.5f + 0.5f * t)
            translate(center.x - g / 2, center.y - r - g / 2) {
                with(it[k]) { draw(Size(g, g), alpha = t, colorFilter = ColorFilter.tint(if (share > 0f) Color.Black else Five.Steps[k])) }
            }
        }
    }
}

/**
 * The live ring's second lap past the target, [part] of the way round, drawn over the first in [color] with its head
 * shadowed, as an Activity ring shows more than 100 %. The shadow is the platform's, which blurs where Compose can't.
 */
private fun DrawScope.lapOver(part: Float, color: Color, stroke: Float) {
    if (part <= 0f) return
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = android.graphics.Paint.Cap.ROUND
        this.color = color.toArgb()
        setShadowLayer(LAP_SHADOW.toPx(), 0f, 0f, Color.Black.copy(alpha = 0.75f).toArgb())
    }
    val inset = LIVE_INSET.toPx() + stroke / 2
    drawIntoCanvas {
        it.nativeCanvas.drawArc(inset, inset, size.width - inset, size.height - inset, RING_START_DEG, RING_SWEEP_DEG * part, false, paint)
    }
}

/**
 * A bead of light at [p] (0..1) along the live ring's arc, with a fading tail behind it, a little wider than the ring
 * so it reads as a swell running round. It comes in from nothing and goes out to nothing; drawn only while 0 < p < 1.
 */
private fun DrawScope.bead(p: Float, stroke: Float) {
    if (p <= 0f || p >= 1f) return
    val inset = LIVE_INSET.toPx() + stroke / 2
    val arc = Size(size.width - 2 * inset, size.height - 2 * inset)
    val head = RING_START_DEG + RING_SWEEP_DEG * p
    val fade = sin(PI.toFloat() * p)
    val parts = 6
    val seg = SHINE_DEG / parts
    repeat(parts) { k ->
        // Butt caps: round ones would overlap and double the light where the parts meet.
        val from = (head - (k + 1) * seg).coerceAtLeast(RING_START_DEG)
        val sweep = head - k * seg - from
        if (sweep > 0f) drawArc(Color.White, from, sweep, false, Offset(inset, inset), arc, alpha = 0.7f * fade * (1 - k / parts.toFloat()), style = Stroke(stroke * 1.4f))
    }
}

/** One glyph per Step, in canonical order (labels 1..5). */
private val STEP_ART = intArrayOf(R.drawable.step_art_palms, R.drawable.step_art_backs, R.drawable.step_art_between, R.drawable.step_art_thumbs, R.drawable.step_art_tips)
private val STEP_ICONS = intArrayOf(R.drawable.ic_step_palms, R.drawable.ic_step_backs, R.drawable.ic_step_between, R.drawable.ic_step_thumbs, R.drawable.ic_step_tips)

/** How much of a Step's arc is filled: shape carries the Status, not colour (§4.1.10). */
internal fun share(s: StepStatus) = when (s) {
    StepStatus.OK -> 1f
    StepStatus.BRIEF -> 0.5f
    StepStatus.NOT_DETECTED -> 0f
}


/** A Wash in a few words, for History rows and the Tile. */
internal fun hint(res: Resources, w: WashSession): String = when {
    w.modelVersion == null -> res.getString(R.string.timer_only_short)
    !w.coverageShown -> res.getString(R.string.too_short_short)
    w.coverage.none { it.status == StepStatus.OK } -> res.getString(R.string.none_detected_short)
    else -> res.getString(R.string.history_hint, w.coverage.count { it.status == StepStatus.OK })
}

/** "Last wash · 7:15 AM", or the date once it is not today. Start and the Tile. */
internal fun lastWashLabel(context: Context, w: WashSession) =
    "${context.getString(R.string.last_wash_title)} · ${DateUtils.getRelativeTimeSpanString(context, w.startedAtEpochMs)}"

private fun duration(s: Float): String {
    val t = s.toInt()
    return "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
}

/**
 * A Step Status as a shape in the Step's [color], like its ring: Looks covered a full disc with a check, May have been
 * brief an outline with its right half filled, Not detected a dashed outline. Fills clockwise from the top as [fill] goes
 * 0 → 1. Drawn, not a ✓ ◐ ○ glyph, which every font sets at a different size. Read as the words beside it.
 */
@Composable
private fun StatusMark(s: StepStatus, color: Color, fill: () -> Float = { 1f }) {
    val check = remember { Path() }
    Canvas(Modifier.size(11.dp).clearAndSetSemantics {}) {
        val line = 1.4.dp.toPx()
        val r = size.minDimension / 2 - line / 2
        val f = fill()
        when (s) {
            StepStatus.OK -> {
                drawArc(color, -90f, 360f * f, true)
                val u = size.width / 11f
                check.reset()
                check.moveTo(2.9f * u, 5.6f * u); check.lineTo(4.7f * u, 7.4f * u); check.lineTo(8.1f * u, 3.8f * u)
                drawPath(check, Color.Black, alpha = f, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            StepStatus.BRIEF -> {
                drawCircle(color, r, style = Stroke(line))
                drawArc(color, -90f, 180f * f, true, Offset(line / 2, line / 2), Size(2 * r, 2 * r))
            }
            StepStatus.NOT_DETECTED -> drawCircle(color, r, style = Stroke(line, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.2.dp.toPx(), 1.8.dp.toPx()))))
        }
    }
}

/** A check that draws itself as [progress] goes 0 → 1, for the covered Steps' row. */
@Composable
private fun CheckMark(color: Color, progress: () -> Float = { 1f }) {
    val path = remember { Path() }
    val part = remember { Path() }
    val measure = remember { PathMeasure() }
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        path.reset()
        path.moveTo(size.width * 0.1f, size.height * 0.55f)
        path.lineTo(size.width * 0.4f, size.height * 0.82f)
        path.lineTo(size.width * 0.9f, size.height * 0.2f)
        measure.setPath(path, false)
        part.reset()
        measure.getSegment(0f, measure.length * progress(), part, true)
        drawPath(part, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
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
private fun WashPreview() = PreviewFrame { WashScreen(onGuide = {}) }

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun SettingsPreview() = PreviewFrame { SettingsScreen(onWash = {}, onStart = {}, onCredits = {}) }

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
