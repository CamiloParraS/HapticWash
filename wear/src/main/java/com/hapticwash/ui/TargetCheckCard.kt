package com.hapticwash.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.hapticwash.R
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import kotlinx.coroutines.delay

/**
 * Target check: at the target, names the Steps not detected so far (name + icon). Closes after 6 s or on tap; the Wash
 * runs on behind it. The service clears the Steps when the Wash ends, which closes it too.
 */
@Composable
internal fun TargetCheckCard(onGuide: (Int) -> Unit) {
    val undetected = LocalWashControls.current.status.collectAsState().value.undetected
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
