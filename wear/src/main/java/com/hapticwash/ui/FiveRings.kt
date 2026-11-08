package com.hapticwash.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.hapticwash.R
import com.hapticwash.session.Haptics
import com.hapticwash.session.History
import com.hapticwash.session.StepCoverage
import com.hapticwash.session.StepStatus
import com.hapticwash.ui.theme.Five
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Progress ring target. A UI constant: nothing in Coverage reads it. */
internal const val RING_TARGET_S = Haptics.TARGET_MS / 1000f
internal const val GLOW_RAMP_S = 3f

/** Summary's Five Rings: diameter. Stroke and gap scale with it ([RING_STROKE_OF], [RING_GAP_OF]) so every size keeps a hole in the middle. */
private val RING_SIZE = 108.dp
internal const val RING_STROKE_OF = 7.4f / 108f
internal const val RING_GAP_OF = 2f / 108f
private val RING_STROKE = RING_SIZE * RING_STROKE_OF

/** Start's rings for the last Wash, and History's. */
internal val START_RING_SIZE = 62.dp
internal val START_RING_SIZE_COMPACT = 44.dp
internal val HISTORY_RING_SIZE = 30.dp

/** The live ring round the screen's edge: 12 dp, 5 dp in from the bezel. */
internal val LIVE_STROKE = 12.dp
private val LIVE_INSET = 5.dp

/** The Wash ring: one arc round the screen, leaving a 60° gap at 12 o'clock for TimeText. */
private const val RING_START_DEG = 300f
private const val RING_SWEEP_DEG = 300f

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
internal const val SHINE_MS = 650
private const val SHINE_DEG = 48f

/**
 * The live ring's arc round the screen: 300° clockwise from 1 o'clock, leaving 60° at the top for TimeText, [part] of
 * it drawn. Its centre line sits [LIVE_INSET] plus half of [ringStroke] in from the edge, so every arc drawn on that path
 * (the ring, the hold, the hairlines) shares one circle whatever its own [stroke].
 */
internal fun DrawScope.edgeArc(brush: Brush, part: Float, stroke: Float, ringStroke: Float = stroke, from: Float = 0f) {
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
internal fun DrawScope.diagonal(from: Color, to: Color) = Brush.linearGradient(listOf(from, to), Offset.Zero, Offset(size.width, size.height))

/**
 * The In-progress ring as it was when the Wash ended, drawn over the Summary and shrinking into the
 * Coverage ring at [centre] as [morph] goes 0 → 1, fading as it goes. Same geometry as WashScreen's.
 */
@Composable
internal fun LiveRing(durationS: Float, morph: () -> Float, centre: () -> Offset, stopped: Boolean) {
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

/** A Wash with no Coverage, in place of its rings: a dashed circle, the same mark as Not detected. */
@Composable
internal fun DashedDisc() = Canvas(Modifier.size(32.dp).clearAndSetSemantics {}) {
    val line = 1.5.dp.toPx()
    drawCircle(Five.TextTertiary, size.minDimension / 2 - line / 2, style = Stroke(line, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))))
}

/**
 * The Five Rings: one concentric ring per Step, outer = Palms, each in its own colour. Shape carries the Status, not
 * colour (§4.1.10): Looks covered closes the ring, May have been brief fills half, Not detected leaves only the track,
 * like the [StatusMark]s below. The rows say it in words.
 */
@Composable
internal fun CoverageRing(coverage: List<StepCoverage>, entrance: () -> Float, modifier: Modifier) {
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
internal fun FiveRings(coverage: List<StepCoverage>, size: Dp, fill: (Int) -> Float = { 1f }) =
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
internal fun DrawScope.lapOver(part: Float, color: Color, stroke: Float) {
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
internal fun DrawScope.bead(p: Float, stroke: Float) {
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
internal val STEP_ART = intArrayOf(R.drawable.step_art_palms, R.drawable.step_art_backs, R.drawable.step_art_between, R.drawable.step_art_thumbs, R.drawable.step_art_tips)
internal val STEP_ICONS = intArrayOf(R.drawable.ic_step_palms, R.drawable.ic_step_backs, R.drawable.ic_step_between, R.drawable.ic_step_thumbs, R.drawable.ic_step_tips)

/**
 * A Step Status as a shape in the Step's [color], like its ring: Looks covered a full disc with a check, May have been
 * brief an outline with its right half filled, Not detected a dashed outline. Fills clockwise from the top as [fill] goes
 * 0 → 1. Drawn, not a ✓ ◐ ○ glyph, which every font sets at a different size. Read as the words beside it.
 */
@Composable
internal fun StatusMark(s: StepStatus, color: Color, fill: () -> Float = { 1f }) {
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
internal fun CheckMark(color: Color, progress: () -> Float = { 1f }) {
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

@WearPreviewDevices
@Composable
private fun CoverageRingPreview() = PreviewFrame {
    val statuses = listOf(StepStatus.OK, StepStatus.OK, StepStatus.BRIEF, StepStatus.NOT_DETECTED, StepStatus.OK)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CoverageRing(statuses.mapIndexed { i, s -> StepCoverage(i + 1, 8f, s) }, { 1f }, Modifier.fillMaxWidth())
    }
}
