package com.hapticwash.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.MotionDurationScale
import com.hapticwash.session.Haptics
import com.hapticwash.session.WashStatus
import kotlin.coroutines.coroutineContext
import kotlin.math.ceil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Live Wash cues: a new line every [CUE_S] s; past the target a cheer every [CHEER_S] s. A lap (every [LAP_S] s before the target) gets a small ripple and pop. */
private const val CUE_S = 5
private const val CHEER_S = 6
private const val LAP_S = 10

/** The 40 s target reached: a thin ring travels in from the screen's edge to [RIPPLE_TO] of its radius, fading. */
internal const val RIPPLE_MS = 700
internal const val RIPPLE_TO = 0.45f

/**
 * What the live Wash screen shows at [elapsedS] seconds into the Wash. Time-driven, never detection-driven: the same
 * for everyone, so nothing here implies a Step Status. Pure, so each second's display is unit-tested.
 */
internal object LiveWash {
    /** The 40 s target is reached: the ring turns mint and the time counts up. */
    fun full(elapsedS: Float) = elapsedS >= RING_TARGET_S

    /** The whole seconds on screen: counting down to the target, then the Wash's total. */
    fun shownS(elapsedS: Float) = if (full(elapsedS)) elapsedS.toInt().toFloat() else ceil(RING_TARGET_S - elapsedS)

    /** Which [LAP_S] lap of the way to the target this is; 0 once the target is reached. */
    fun lap(elapsedS: Float) = if (full(elapsedS)) 0 else (elapsedS / LAP_S).toInt()

    /** The live ring's fill, 0..1, full at the target. */
    fun ringFill(elapsedS: Float) = (elapsedS / RING_TARGET_S).coerceAtMost(1f)

    /** Past the target, how far round its current lap the ring has run, 0..1. */
    fun overFill(elapsedS: Float) = ((elapsedS - RING_TARGET_S) / RING_TARGET_S) % 1f

    /** The glow's swell over the last [GLOW_RAMP_S] before the target, 0..1; 0 once reached. */
    fun glowRamp(elapsedS: Float) = if (full(elapsedS)) 0f else ((elapsedS - (RING_TARGET_S - GLOW_RAMP_S)) / GLOW_RAMP_S).coerceIn(0f, 1f)

    /** The movement cue, holding on the last one, then past the target the cheers in turn. */
    fun cue(elapsedS: Float, cues: Array<String>, cheers: Array<String>) =
        if (full(elapsedS)) cheers[((elapsedS - RING_TARGET_S) / CHEER_S).toInt() % cheers.size]
        else cues[(elapsedS / CUE_S).toInt().coerceAtMost(cues.lastIndex)]
}

/**
 * Seconds since the running Wash began, 0 when none runs. Ticks every frame while [everyFrame] (the progress ring is
 * moving), otherwise four times a second and never past the target, so the target's ripple lands with its haptic
 * ticks. Stands still under [ambient], which covers the screen and draws once a minute; waking catches up at once.
 */
@Composable
internal fun rememberElapsedS(status: State<WashStatus>, everyFrame: Boolean, ambient: Boolean): State<Float> {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // Derived state, so the ring's progress lambda reads the live value at draw time.
    val elapsedS = remember { derivedStateOf { status.value.let { if (it.running) ((now - it.startedAtEpochMs) / 1000f).coerceAtLeast(0f) else 0f } } }
    LaunchedEffect(ambient) {
        if (ambient) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            val s = status.value
            val full = LiveWash.full(elapsedS.value)
            if (everyFrame && !full) withFrameMillis {}
            else delay(if (s.running && !full) (Haptics.TARGET_MS - (now - s.startedAtEpochMs)).coerceIn(1L, 250L) else 250L)
        }
    }
    return elapsedS
}

/** The live Wash's celebrations, each 0..1 (1 = at rest). */
@Stable
internal class WashMoments {
    /** The target reached: a ripple in from the edge. */
    val ripple = Animatable(1f)

    /** A pop on the number, at the target and, lighter, at each lap. */
    val pop = Animatable(1f)

    /** The ring, now whole, run round once by a bead of light as it turns colour. */
    val shine = Animatable(1f)

    /** Each lap before the target: a lighter ripple. */
    val lap = Animatable(1f)
}

/**
 * Plays [WashMoments] as [full] and [lap] change, only while [watching]: a target or lap that passed while the screen
 * was away (or opened mid-Wash) is not celebrated late.
 */
@Composable
internal fun rememberWashMoments(full: Boolean, lap: Int, watching: Boolean): WashMoments {
    val m = remember { WashMoments() }
    val startedFull = remember { full }
    val startedLap = remember { lap } // coming back mid-Wash is not a new lap
    val seen by rememberUpdatedState(watching)
    LaunchedEffect(full) {
        if (!full || startedFull || !seen) return@LaunchedEffect
        launch { m.pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 2f) }
        launch { m.shine.snapTo(0f); m.shine.animateTo(1f, tween(SHINE_MS, easing = FastOutSlowInEasing)) }
        m.ripple.snapTo(0f)
        m.ripple.animateTo(1f, tween(RIPPLE_MS, easing = EaseOutStrong))
    }
    LaunchedEffect(lap) {
        if (lap == 0 || lap == startedLap || !seen) return@LaunchedEffect
        launch { m.pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialVelocity = 1f) }
        m.lap.snapTo(0f)
        m.lap.animateTo(1f, tween(RIPPLE_MS, easing = EaseOutStrong))
    }
    return m
}

/**
 * The glow's slow breath, 0..1, while measuring. Still in [ambient] mode, where nothing on the Wash screen moves, and
 * when animations are off (animator scale 0 finishes each at once, so the loop would spin).
 */
@Composable
internal fun rememberBreath(ambient: Boolean): Animatable<Float, AnimationVector1D> {
    val breath = remember { Animatable(0.5f) }
    LaunchedEffect(ambient) {
        if (ambient) return@LaunchedEffect
        if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) return@LaunchedEffect
        while (true) {
            breath.animateTo(1f, tween(2000, easing = FastOutSlowInEasing))
            breath.animateTo(0f, tween(2000, easing = FastOutSlowInEasing))
        }
    }
    return breath
}
