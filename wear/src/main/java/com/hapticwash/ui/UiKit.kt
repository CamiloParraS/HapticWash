package com.hapticwash.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.flow.first

/** Summary entrance: each row fades and rises over [ROW_MS], [STAGGER_MS] after the one above. */
private const val ROW_MS = 200f
internal const val STAGGER_MS = 40f
internal const val ENTRANCE_MS = 360 // ROW_MS + 4 × STAGGER_MS, for five rows

/** Half a list button's 52 dp, so [ListScreen] can centre the last row. */
private val ROW_HALF_HEIGHT = 26.dp

/** Strong ease-out: moves at once, settles gently. */
internal val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

/**
 * Where Start's button sat in the window, so the Wash ring can bloom from it. Written by Start, read by Wash: one per
 * App, provided as [LocalStartOrigin] (a fresh, unknown one in previews).
 */
internal class StartOrigin {
    var centre = Offset.Unspecified
    var size = IntSize.Zero
}

internal val LocalStartOrigin = staticCompositionLocalOf { StartOrigin() }

/**
 * DESIGN.md's Tint-Washes-Down: [color] falling from above the top edge, an ellipse wider than it is tall
 * (130 % × 70 % of the screen), fading to black by 78 % of the way. Each screen washes in its own colour.
 */
internal fun Modifier.tintWash(color: Color, alpha: () -> Float) = drawBehind {
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

internal fun Modifier.tintWash(color: Color, alpha: Float = 0.34f) = tintWash(color) { alpha }

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. Items shrink and fade at the round edge via [spec]. */
@Composable
internal fun ListScreen(wash: Color? = Five.Tint, washAlpha: Float = 0.3f, content: TransformingLazyColumnScope.(spec: TransformationSpec) -> Unit) {
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

/** Row [i] of Start fades and rises with the Summary's stagger, driven by [entrance] (0..1 over [ENTRANCE_MS]). */
internal fun Modifier.rise(entrance: () -> Float, i: Int) = graphicsLayer {
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
internal fun SettingIcon(res: Int, flip: Boolean = false, tint: Color = Color.White, size: Dp = 22.dp) {
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
internal fun GlyphDisc(fill: Color, size: Dp = 32.dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    Box(modifier.size(size).clip(CircleShape).background(fill).clearAndSetSemantics {}, contentAlignment = Alignment.Center) { content() }

/** Full width, with the item's height following the edge transformation. Surfaces also take `SurfaceTransformation(spec)`. */
internal fun TransformingLazyColumnItemScope.morph(spec: TransformationSpec) =
    Modifier.fillMaxWidth().transformedHeight(this, spec)

/** A list's title: morphs at the edge like the rows, and keeps the top padding Material asks of a first header. */
@Composable
internal fun TransformingLazyColumnItemScope.Header(spec: TransformationSpec, text: String, color: Color = Five.Tint) = ListHeader(
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
internal fun ScrollIndicatorUnlessCapturing(state: TransformingLazyColumnState) {
    if (!LocalScrollCaptureInProgress.current) ScrollIndicator(state)
}

/** [morph] for plain content that has no surface of its own. */
internal fun TransformingLazyColumnItemScope.morphContent(spec: TransformationSpec) =
    morph(spec).graphicsLayer { with(spec) { applyContainerTransformation(scrollProgress) } }

/** A secondary action as a translucent circle on the bottom arc. */
@Composable
internal fun GlassButton(icon: Int, label: String, onClick: () -> Unit, size: Dp = 48.dp) =
    FilledTonalIconButton(
        onClick, Modifier.size(size), shapes = IconButtonDefaults.animatedShapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Five.Glass, contentColor = Color.White),
    ) { Icon(painterResource(icon), label, Modifier.size(24.dp)) }

/** A line under a list, quieter than the rows: when a change takes effect. */
@Composable
internal fun Footnote(text: String, modifier: Modifier) =
    Text(text, modifier.padding(horizontal = 30.dp, vertical = 4.dp), color = Five.TextSecondary, style = FiveType.labelSmall, textAlign = TextAlign.Center)

/** Press feedback for Start and Stop: a slight, critically damped shrink while the finger is down. */
@Composable
internal fun rememberPressScale(): Pair<MutableInteractionSource, Modifier> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium))
    return source to Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Row [i]'s share of the Summary entrance, eased: 0 before its turn, 1 once it has settled. */
internal fun staggered(entrance: Float, i: Int) =
    EaseOutStrong.transform(((entrance * ENTRANCE_MS - i * STAGGER_MS) / ROW_MS).coerceIn(0f, 1f))

/**
 * Previews, in the app's theme and scaffold, over [controls] (idle and empty unless given a running Wash or a
 * History). Start is left out: it asks for a permission.
 */
@Composable
internal fun PreviewFrame(controls: WashControls = PreviewWashControls(), content: @Composable () -> Unit) =
    HapticWashTheme { AppScaffold { CompositionLocalProvider(LocalWashControls provides controls) { content() } } }
