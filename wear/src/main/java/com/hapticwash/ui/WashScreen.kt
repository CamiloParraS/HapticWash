package com.hapticwash.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.hapticwash.R
import com.hapticwash.session.Haptics
import com.hapticwash.session.Settings
import com.hapticwash.session.WashService
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import com.hapticwash.ui.theme.rounded
import kotlin.coroutines.coroutineContext
import kotlin.math.ceil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The stop symbol drawn at the centre while holding, in its disc, and how much of the hold the countdown stays readable for. */
internal val STOP_MARK = 26.dp
private val STOP_DISC = 64.dp
private const val SWAP_FROM = 0.3f

/** The stop disc sits this far above the centre, so its line of text fits under it. */
internal val STOP_LIFT = 15.dp

/** How far the countdown has given way to the stop symbol at hold [h]: 0 until [SWAP_FROM], then eased to 1. */
private fun swap(h: Float) = EaseOutStrong.transform(((h - SWAP_FROM) / (1 - SWAP_FROM)).coerceIn(0f, 1f))

/** Stop is a hold anywhere on the Wash screen: the sweep takes [HOLD_MS], and the "Hold to stop" hint stays [HINT_MS]. */
// ponytail: 700 ms is a guess; tune with wet hands on a device.
private const val HOLD_MS = 700
private const val HINT_MS = 3000L

/** Start's ghost button is gone by this share of the bloom. */
private const val GHOST_FADE = 0.4f

/** "Hold to stop" waits this long after the bloom before it fades in, so it doesn't compete with it. */
private const val HINT_DELAY_MS = 700L

/** The sweep's share drawn on the press itself, so touch-down is answered before the first frame of the hold. */
private const val PRESS_HEAD = 0.03f

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

/**
 * The Wash, and then its Summary in the same place: the result fades in over the Wash instead of sliding in as a
 * new screen, so the ring and the stop square can carry across it. Swiping back from either goes to Start.
 */
@Composable
internal fun WashScreen(onGuide: (Int) -> Unit, onCancelled: () -> Unit = {}) {
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

internal const val TARGET_CARD_MS = 6_000L
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

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WashPreview() = PreviewFrame { WashScreen(onGuide = {}) }
