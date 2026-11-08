package com.hapticwash.ui

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.SwitchButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.hapticwash.BuildConfig
import com.hapticwash.R
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val HUB_ROW_MIN = 54.dp
private val TOGGLE_ROW_MIN = 56.dp

/**
 * A settings row arrives: it rises into place on a spring with a trace of overshoot, [STAGGER_MS] after the row above.
 * The same rise as Start's and the Summary's rows, so the app has one way of arriving; the bounce is saved for the toggles.
 */
@Composable
internal fun settle(i: Int): Modifier {
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
internal fun SettingsScreen(onWash: () -> Unit, onStart: () -> Unit, onCredits: () -> Unit) {
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
internal fun WashSettingsScreen() {
    val settings = LocalWashControls.current.settings
    val status by LocalWashControls.current.status.collectAsState()
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
internal fun StartSettingsScreen() {
    val controls = LocalWashControls.current
    val settings = controls.settings
    val status by controls.status.collectAsState()
    var wrist by remember { mutableStateOf(settings.wrist) }
    var twist by remember { mutableStateOf(settings.twistStart) }
    var warn by remember { mutableStateOf(false) }
    fun setTwist(on: Boolean) {
        twist = on
        controls.twistStart(on)
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

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun SettingsPreview() = PreviewFrame { SettingsScreen(onWash = {}, onStart = {}, onCredits = {}) }
