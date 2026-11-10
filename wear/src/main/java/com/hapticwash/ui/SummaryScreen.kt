package com.hapticwash.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.VerticalPageIndicator
import androidx.wear.compose.material3.VerticalPagerScaffold
import com.hapticwash.R
import com.hapticwash.session.StepCoverage
import com.hapticwash.session.StepStatus
import com.hapticwash.session.allCovered
import com.hapticwash.session.focusCounts
import com.hapticwash.session.focusStep
import com.hapticwash.session.worthSaving
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import kotlinx.coroutines.flow.first

/** The Summary's arcs and rows begin once the ring landing from the Wash is this far in. */
private const val ENTRANCE_AT = 0.6f

/** A Wash's Summary: the one just ended ([fromWash], [stoppedByHold] when a hold ended it), or one from History. */
@Composable
internal fun SummaryScreen(id: Long?, fromWash: Boolean = false, stoppedByHold: Boolean = false, onGuide: (Int) -> Unit) {
    val context = LocalContext.current
    val controls = LocalWashControls.current
    val wash = remember(id) { id?.let(controls::wash) }
    val steps = stringArrayResource(R.array.steps)
    val focus = remember(id) { controls.history.value.let { h -> focusStep(h)?.let { it to focusCounts(h, it) } } }
    val focusLabel = focus?.first
    // Rows arrive in a short cascade as the end pulses play. Decorative: scrolling is never blocked.
    val entrance = remember { Animatable(0f) }
    // C6: straight from a Wash with the live ring on, that ring shrinks into the Coverage rings (0 = live, 1 = landed).
    val byHold = remember { fromWash && stoppedByHold }
    val morph = remember { Animatable(if (fromWash && wash?.coverageShown == true && controls.settings.progressRing) 0f else 1f) }
    LaunchedEffect(Unit) { morph.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
    // The rings and rows start once the live ring is most of the way in, so they fill where they can be seen.
    LaunchedEffect(Unit) {
        snapshotFlow { morph.value }.first { it >= ENTRANCE_AT }
        entrance.animateTo(1f, tween(ENTRANCE_MS, easing = LinearEasing))
    }
    // Ended by a hold, the stop square becomes the answer: it shrinks into the rings' centre and the words grow in under them.
    fun numeral() = if (byHold) EaseOutStrong.transform(((morph.value - 0.4f) / 0.6f).coerceIn(0f, 1f)) else 1f
    // The rings' centre in this screen's space, so a moving nav transition doesn't throw the morph off.
    var ringCentre by remember { mutableStateOf(Offset.Unspecified) }
    var screen by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val estimated = wash != null && wash.modelVersion != null && wash.coverageShown
    val all = estimated && allCovered(wash!!.coverage)
    Box(Modifier.onGloballyPositioned { screen = it }) {
        // Mint once all five look covered; a faint white wash when there are no rings to show.
        ListScreen(if (all) Five.Done else if (estimated) Five.Tint else Five.Neutral, if (estimated) 0.26f else 0.1f) { spec ->
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
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Five.PlatterRaised, contentColor = Five.Ink),
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
                            color = if (all) Five.Done else Five.Ink,
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
                        GlyphDisc(Five.Ink.copy(alpha = 0.1f)) { CheckMark(Five.Ink) { staggered(entrance.value, at) } }
                        Text(covered.joinToString(", ") { steps[it.label - 1] }, Modifier.weight(1f), color = Five.TextSecondary, style = FiveType.label)
                    }
                }
            }
            if (!wash.worthSaving()) item { Text(stringResource(R.string.too_short_to_save), morphContent(spec).padding(top = 6.dp, start = 30.dp, end = 30.dp), color = Five.TextSecondary, style = FiveType.labelSmall, textAlign = TextAlign.Center) }
        }
        if (morph.value < 1f) LiveRing(wash!!.durationS, { morph.value }, { ringCentre }, byHold)
        if (byHold && morph.value < 1f) {
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
        GlyphDisc(if (ok) color else color.copy(alpha = 0.2f)) { Icon(painterResource(STEP_ICONS[c.label - 1]), null, Modifier.size(19.dp), tint = if (ok) Five.onStep(c.label) else color) }
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
internal fun GuideScreen(label: Int?) {
    if (label == null || label !in 1..5) return
    val pager = rememberPagerState(initialPage = label - 1) { 5 }
    VerticalPagerScaffold(
        pager,
        Modifier.background(Five.Bg),
        pageIndicator = { VerticalPageIndicator(pager, selectedColor = Five.step(pager.currentPage + 1), unselectedColor = Five.TextTertiary) },
    ) {
        VerticalPager(pager, Modifier.fillMaxSize()) { page -> GuidePage(page + 1) { page - (pager.currentPage + pager.currentPageOffsetFraction) } }
    }
}

/** One Step's guide page. [offset] is how far the page is from the middle of the pager, in pages: positive while it is below, still to come. */
@Composable
private fun GuidePage(label: Int, offset: () -> Float = { 0f }) {
    val color = Five.step(label)
    val scroll = rememberScrollState()
    val compact = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } < 220.dp
    val art = if (compact) 46.dp else 52.dp
    val page = stringResource(R.string.page_of, label, 5)
    // The words trail the page a little and dissolve as it leaves, and arrive the same way: the swipe reads as one page
    // handing over to the next, not a card sliding under still text. Read in the draw phase, so paging never recomposes.
    val words = Modifier.graphicsLayer {
        val o = offset().coerceIn(-1f, 1f)
        alpha = EaseOutStrong.transform((1f - 1.5f * kotlin.math.abs(o)).coerceIn(0f, 1f))
        translationY = -o * 22.dp.toPx()
    }
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
                rotationZ = 16f * o
            }.clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(STEP_ART[label - 1]), null, Modifier.size(art).clip(CircleShape).background(Color(0xFFEEEEEE)))
        }
        Text(stringArrayResource(R.array.steps)[label - 1], words.padding(top = 2.dp), style = FiveType.title.copy(fontSize = 16.5.sp), color = color, textAlign = TextAlign.Center)
        Text(stringArrayResource(R.array.step_how)[label - 1], words, style = FiveType.bodySmall.copy(fontSize = 13.5.sp), textAlign = TextAlign.Center)
        Text(stringArrayResource(R.array.step_whys)[label - 1], words, color = Five.TextSecondary, style = FiveType.label.copy(fontSize = 11.5.sp), textAlign = TextAlign.Center)
    }
}
