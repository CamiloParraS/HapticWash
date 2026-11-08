package com.hapticwash.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.hapticwash.R
import com.hapticwash.inference.StepModel
import com.hapticwash.session.History
import com.hapticwash.session.Settings
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashService
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Start's entrance plays once per process: coming back from History or Settings shows it settled. [filled] is the Wash its ring last filled for. */
private object StartEntrance {
    var played = false
    var filled: Long? = null
}

@Composable
internal fun StartScreen(onWash: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit, autoStart: Boolean, onAutoStarted: () -> Unit, cancelledNotice: Boolean = false) {
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
internal val START_GRADIENT = listOf(Five.TintLight, Five.Tint, Five.TintSky)
