package com.hapticwash.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hapticwash.ui.theme.Five
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The stop symbol drawn at the centre while holding, in its disc, and how much of the hold the countdown stays readable for. */
internal val STOP_MARK = 26.dp
internal val STOP_DISC = 64.dp
private const val SWAP_FROM = 0.3f

/** The stop disc sits this far above the centre, so its line of text fits under it. */
internal val STOP_LIFT = 15.dp

/** Stop is a hold anywhere on the Wash screen: the sweep takes [HOLD_MS], and the "Hold to stop" hint stays [HINT_MS]. */
// ponytail: 700 ms is a guess; tune with wet hands on a device.
private const val HOLD_MS = 700
private const val HINT_MS = 3000L

/** "Hold to stop" waits this long after the bloom before it fades in, so it doesn't compete with it. */
private const val HINT_DELAY_MS = 700L

/** The sweep's share drawn on the press itself, so touch-down is answered before the first frame of the hold. */
private const val PRESS_HEAD = 0.03f

/** A hold's commit gets at least this long on screen before the result takes over. */
private const val COMMIT_BEAT_MS = 450L

/**
 * Hold anywhere to stop a Wash. Wet, soapy fingers miss a button, and a hold survives water on the glass. The press
 * responds at once; the sweep is linear while the user decides; release snaps back fast; a full hold commits.
 * One per Wash screen, which hands [committed] to the Summary so the stop square can carry across.
 */
@Stable
internal class StopHoldState {
    /** 0 = not held, 1 = committed. */
    val hold = Animatable(0f)

    /** The commit: the stop square and the ring's stroke spring past their size and settle (1 = at rest). */
    val commitPop = Animatable(1f)

    /** "Hold to stop", said once in the first seconds. */
    val hint = Animatable(0f)

    /** Committed and waiting for the save: the stop square breathes, so the wait isn't a freeze. */
    val breath = Animatable(1f)

    /** Stop was asked for, by a hold or the accessibility action. */
    var stopping by mutableStateOf(false)
        internal set

    private var committedAt = -1L

    /** A full hold ended the Wash (not the accessibility action). */
    val committed get() = committedAt >= 0

    /** How far the countdown has given way to the stop symbol: 0 until [SWAP_FROM] of the hold, then eased to 1. */
    val swap get() = EaseOutStrong.transform(((hold.value - SWAP_FROM) / (1 - SWAP_FROM)).coerceIn(0f, 1f))

    /** The save is quick; after a hold, wait out the rest of the commit's beat so it is seen before the result. */
    suspend fun beat() {
        if (committed) delay((COMMIT_BEAT_MS - (System.currentTimeMillis() - committedAt)).coerceAtLeast(0))
    }

    internal fun commit() {
        committedAt = System.currentTimeMillis()
    }
}

@Composable
internal fun rememberStopHold() = remember { StopHoldState() }

/** The hint's one showing, and the stop square's breath once [StopHoldState.stopping]. Run while the Wash shows. */
@Composable
internal fun StopHoldCues(state: StopHoldState) {
    LaunchedEffect(Unit) {
        delay(HINT_DELAY_MS)
        if (state.hold.value == 0f) state.hint.animateTo(1f, tween(300))
        delay(HINT_MS)
        state.hint.animateTo(0f, tween(400))
    }
    LaunchedEffect(state.stopping) {
        if (!state.stopping) return@LaunchedEffect
        delay(400) // after the commit pop
        while (true) {
            state.breath.animateTo(0.88f, tween(700, easing = FastOutSlowInEasing))
            state.breath.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
    }
}

/** The hold gesture anywhere on this element, and a long-click accessibility action [label]; either calls [onStop] once. */
@Composable
internal fun Modifier.holdToStop(state: StopHoldState, label: String, onStop: () -> Unit): Modifier {
    val scope = rememberCoroutineScope()
    val feedback = LocalHapticFeedback.current
    val stop by rememberUpdatedState { state.stopping = true; onStop() }
    return semantics { onLongClick(label = label) { if (!state.stopping) stop(); true } }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                if (state.stopping) return@awaitEachGesture
                scope.launch {
                    state.hint.snapTo(0f)
                    // Answers the press on the first frame: a sliver of the sweep and the colour, then the rest.
                    state.hold.snapTo(PRESS_HEAD)
                    state.hold.animateTo(1f, tween(HOLD_MS, easing = LinearEasing))
                    feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.commit()
                    launch { state.commitPop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), initialVelocity = 3f) }
                    stop()
                }
                waitForUpOrCancellation()
                // A new animateTo cancels the sweep in flight; once committed there is nothing to undo.
                if (!state.stopping && state.hold.value < 1f) scope.launch { state.hold.animateTo(0f, tween(200, easing = EaseOutStrong)) }
            }
        }
}

/** The whole screen warms red from the centre as the hold fills: fully by half way, so the change is seen at a glance. [settle] eases it once committed. */
internal fun DrawScope.stopWarmth(state: StopHoldState, settle: Float) {
    val shift = EaseOutStrong.transform((state.hold.value * 2).coerceAtMost(1f))
    if (shift > 0f) drawRect(Brush.radialGradient(
        0f to Five.Stop.copy(alpha = (0.3f + 0.08f * (1 - settle) / 0.6f) * shift), 0.6f to Five.Stop.copy(alpha = 0.08f * shift), 0.8f to Color.Transparent,
    ))
}

/**
 * The live ring filling a second time in the stop colour, so it reads as the ring turning into "end" rather than a
 * new bar (drawn even with the progress ring off), and the stop symbol growing in at the centre, in its disc.
 */
internal fun DrawScope.stopMark(state: StopHoldState) {
    val h = state.hold.value
    if (h <= 0f) return
    val pop = state.commitPop.value
    edgeArc(SolidColor(Five.Stop), h, LIVE_STROKE.toPx() * pop * (if (state.stopping) 14f / 12f else 1f), LIVE_STROKE.toPx())
    val s = state.swap
    drawCircle(Five.Stop.copy(alpha = 0.22f), STOP_DISC.toPx() / 2 * lerp(0.6f, 1f, s) * pop, center - Offset(0f, STOP_LIFT.toPx()), alpha = s)
    val side = STOP_MARK.toPx() * lerp(0.6f, 1f, s) * pop * state.breath.value
    drawRoundRect(Five.Stop, center - Offset(side / 2, side / 2 + STOP_LIFT.toPx()), Size(side, side), CornerRadius(side * 7f / 26f), alpha = s)
}
