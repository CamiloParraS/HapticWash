package com.hapticwash.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.hapticwash.R
import com.hapticwash.session.WashOutcome
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType

/** Start's ghost button is gone by this share of the bloom. */
private const val GHOST_FADE = 0.4f

/** Start → Wash: the ring blooms from the Start button, from this fraction of its size up to 1. */
private const val BLOOM_FROM = 0.2f

internal const val TARGET_CARD_MS = 6_000L

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
    val stopHold = rememberStopHold()
    Box(Modifier.fillMaxSize()) {
        // Gone once faded: nothing under the Summary should take a press.
        if (leave.value < 1f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1 - leave.value }) {
            WashBody(stopHold, onEnd = { result = it }, handedOver = result != null, onCancelled = onCancelled)
        }
        result?.let { SummaryScreen(it, fromWash = fresh, stoppedByHold = stopHold.committed, onGuide = onGuide) }
        if (result == null) TargetCheckCard(onGuide)
    }
}

/**
 * Leaves the Wash screen when the running Wash ends: [onEnd] with its id for its Summary, after [stopHold]'s beat; or
 * [onCancelled] when Auto-end cancelled it, since no Summary follows a Wash saved nowhere.
 */
@Composable
private fun OnWashEnd(stopHold: StopHoldState, onEnd: (Long) -> Unit, onCancelled: () -> Unit) {
    val latest by LocalWashControls.current.outcome.collectAsState()
    val before = remember { latest?.n } // an ending from before this screen is not this Wash's
    LaunchedEffect(latest) {
        val ending = latest?.takeIf { it.n != before } ?: return@LaunchedEffect
        when (val o = ending.outcome) {
            is WashOutcome.Ended -> { stopHold.beat(); onEnd(o.wash.id) }
            is WashOutcome.Cancelled -> onCancelled()
        }
    }
}

/** Elapsed time, optional progress ring, a changing movement cue, hold to stop. Cues are time-driven and never a Step Status. */
@Composable
private fun WashBody(stopHold: StopHoldState, onEnd: (Long) -> Unit, handedOver: Boolean, onCancelled: () -> Unit) {
    val context = LocalContext.current
    val controls = LocalWashControls.current
    OnWashEnd(stopHold, onEnd, onCancelled)
    StopHoldCues(stopHold)
    val statusState = controls.status.collectAsState()
    val status by statusState
    val ring = remember { controls.settings.progressRing }
    // The Summary draws the ring and the stop square from here on, shrinking them into its own; these must not double them.
    val carried = handedOver && ring
    val ambient = LocalAmbient.current
    val elapsedS by rememberElapsedS(statusState, everyFrame = ring, ambient = ambient)
    // The ring changes colour once it fills, so reaching the target is visible at a glance.
    val full by remember { derivedStateOf { LiveWash.full(elapsedS) } }
    // The whole seconds on screen: the text recomposes once a second, not every frame.
    val shownS by remember { derivedStateOf { LiveWash.shownS(elapsedS) } }
    val lap by remember { derivedStateOf { LiveWash.lap(elapsedS) } }
    val moments = rememberWashMoments(full, lap, watching = !ambient)
    // Start → Wash: the ring and the countdown bloom from where the Start button was (0 = at the button, 1 = settled).
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    var screen by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val cues = stringArrayResource(R.array.wash_cues)
    val cheers = stringArrayResource(R.array.wash_cues_after)
    val cue by remember { derivedStateOf { LiveWash.cue(elapsedS, cues, cheers) } }
    val ringColor by animateColorAsState(if (full) Five.Done else Five.Tint, tween(300))
    // Each ring lit along its diagonal; past the target, mint (DESIGN.md's Mint Reached).
    val ringFrom by animateColorAsState(if (full) Five.DoneLight else Five.TintMist, tween(300))
    val ringTo by animateColorAsState(if (full) Five.DoneRing else Five.Tint, tween(300))
    val ringTrack by animateColorAsState((if (full) Five.Done else Five.Tint).copy(alpha = 0.16f), tween(300))
    // Committed and waiting for the save: the extra warmth eases off.
    val settle by animateFloatAsState(if (stopHold.stopping) 0.4f else 1f, tween(600))
    // The ring's colour washing down from the top, like watchOS's app gradients; it brightens once the target is reached,
    // breathes while measuring, and swells over the last seconds before the target so the ripple lands as a release.
    val glow by animateFloatAsState(if (full) 0.3f else 0.22f, tween(300))
    val breath = rememberBreath(ambient)
    ScreenScaffold { padding ->
        Box(
            Modifier.fillMaxSize().onGloballyPositioned { screen = it }
                .holdToStop(stopHold, context.getString(R.string.stop_wash)) { controls.stop() }
                .background(Color.Black).tintWash(ringColor) {
                    // Timer-only has no wash: the screen stays plain black, like its hairline.
                    if (status.timerOnly) return@tintWash 0f
                    // Gives way to the stop colour as the hold fills.
                    (glow + (breath.value - 0.5f) * 0.08f + 0.08f * LiveWash.glowRamp(elapsedS)) * enter.value *
                        (1 - EaseOutStrong.transform((stopHold.hold.value * 2).coerceAtMost(1f)))
                }.drawBehind {
                    stopWarmth(stopHold, settle)
                    ripple(moments.lap.value, ringColor, 0.35f, 2.dp.toPx())
                    ripple(moments.ripple.value, ringColor, 0.7f, 3.dp.toPx())
                },
            contentAlignment = Alignment.Center,
        ) {
            val origin = LocalStartOrigin.current
            val from = { screen?.takeIf { it.isAttached && origin.centre.isSpecified }?.windowToLocal(origin.centre) }
            if (ring) {
                Spacer(Modifier.fillMaxSize().graphicsLayer { alpha = if (carried) 0f else enter.value }.drawBehind {
                    liveRing(elapsedS, enter.value, from() ?: size.center, ringTrack, ringFrom, ringTo, moments.shine.value)
                })
            } else {
                // Timer-only quiet cue: a white hairline on the same path, filling with the target. Static between ticks (no
                // motion to reduce), it shows the watch is running and how far along, without the full ring the user kept off.
                Spacer(Modifier.fillMaxSize().graphicsLayer { alpha = enter.value }.drawBehind {
                    val line = 2.5.dp.toPx()
                    edgeArc(SolidColor(Color.White.copy(alpha = 0.18f)), 1f, line, LIVE_STROKE.toPx())
                    edgeArc(SolidColor(Color.White), LiveWash.ringFill(elapsedS), line, LIVE_STROKE.toPx())
                })
            }
            Spacer(Modifier.fillMaxSize().drawBehind { if (!carried) stopMark(stopHold) })
            Column(
                Modifier.graphicsLayer {
                    val p = enter.value
                    val s = stopHold.swap
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
                    Modifier.graphicsLayer { scaleX = moments.pop.value; scaleY = moments.pop.value },
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
                        Modifier.graphicsLayer { alpha = if (stopHold.stopping) 0f else 1f },
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
                Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).graphicsLayer { alpha = stopHold.hint.value * (1 - stopHold.swap) },
                color = Five.TextTertiary,
                style = FiveType.labelSmall,
            )
            // Under the stop square while holding: "Hold to stop", before the target a nudge to keep going (never a block),
            // and "Stopping…" once committed.
            Text(
                stringResource(when { stopHold.stopping -> R.string.stopping; !full -> R.string.stop_early; else -> R.string.hold_to_stop }),
                Modifier.align(Alignment.TopCenter)
                    .padding(top = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } / 2 - STOP_LIFT + STOP_DISC / 2 + 12.dp, start = 34.dp, end = 34.dp)
                    .graphicsLayer { alpha = if (stopHold.stopping) 1f else stopHold.swap },
                color = Color.White,
                style = FiveType.body,
                textAlign = TextAlign.Center,
            )
            // Only while the ring blooms, which is when Start's place is known.
            if (ring && enter.value < 0.999f) from()?.let { StartGhost(it, origin.size) { enter.value } }
        }
    }
}

/** A ring travelling in from the screen's edge to [RIPPLE_TO] of its radius as [p] goes 0 → 1, fading from [alpha]. */
private fun DrawScope.ripple(p: Float, color: Color, alpha: Float, stroke: Float) {
    if (p >= 1f) return
    val edge = size.minDimension / 2
    drawCircle(color, lerp(edge, edge * RIPPLE_TO, p), alpha = (1 - p) * alpha, style = Stroke(stroke))
}

/**
 * The live progress ring: one arc from 300° round to 240°, the 60° gap at 12 o'clock leaving TimeText clear, lapping
 * itself past the target with a bead of light run round once ([shine]). Blooms from [from] (Start's button) as [enter]
 * goes 0 → 1, scaled up about the centre so one shape grows into the next. Drawn, not CircularProgressIndicator: that
 * restarts its own animation on every new value, so fed every frame it never moves until the value stops changing.
 */
private fun DrawScope.liveRing(elapsedS: Float, enter: Float, from: Offset, track: Color, fillFrom: Color, fillTo: Color, shine: Float) {
    val centre = size.center
    val stroke = LIVE_STROKE.toPx()
    withTransform({
        translate((1 - enter) * (from.x - centre.x), (1 - enter) * (from.y - centre.y))
        val s = lerp(BLOOM_FROM, 1f, enter)
        scale(s, s, centre)
    }) {
        edgeArc(SolidColor(track), 1f, stroke)
        edgeArc(diagonal(fillFrom, fillTo), LiveWash.ringFill(elapsedS), stroke)
        if (LiveWash.full(elapsedS)) lapOver(LiveWash.overFill(elapsedS), fillTo, stroke)
        bead(shine, stroke)
    }
}

/**
 * Start's button, left where it was ([at], [size] in px): it shrinks and fades in the first moments of the bloom
 * ([enter]), so the ring reads as coming out of it.
 */
@Composable
private fun StartGhost(at: Offset, size: IntSize, enter: () -> Float) {
    val density = LocalDensity.current
    Row(
        Modifier.size(with(density) { size.width.toDp() }, with(density) { size.height.toDp() })
            .offset { IntOffset((at.x - size.width / 2f).toInt(), (at.y - size.height / 2f).toInt()) }
            .graphicsLayer {
                val p = enter()
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

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WashPreview() = PreviewFrame(PreviewWashControls(PreviewWashControls.running(23f), settings = MemorySettings(progressRing = true))) { WashScreen(onGuide = {}) }

@WearPreviewDevices
@Composable
private fun WashTimerOnlyPreview() = PreviewFrame(PreviewWashControls(PreviewWashControls.running(12f, timerOnly = true))) { WashScreen(onGuide = {}) }
