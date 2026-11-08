package com.hapticwash.ui

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.hapticwash.R
import com.hapticwash.session.StepStatus
import com.hapticwash.ui.theme.Five
import com.hapticwash.ui.theme.FiveType
import kotlinx.coroutines.flow.first

@Composable
internal fun HistoryScreen(onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val washes by LocalWashControls.current.history.collectAsState()
    ListScreen { spec ->
        item { Header(spec, stringResource(R.string.history)) }
        // Nothing yet: the glyph and the one sentence.
        if (washes.isEmpty()) item {
            Column(morphContent(spec).padding(horizontal = 28.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlyphDisc(Five.Tint.copy(alpha = 0.16f), 46.dp) { Icon(painterResource(R.drawable.ic_history), null, Modifier.size(24.dp), tint = Five.Tint) }
                Text(stringResource(R.string.history_empty), color = Five.TextSecondary, style = FiveType.label.copy(lineHeight = 1.3.em), textAlign = TextAlign.Center)
            }
        }
        // Newest first, one small day label per run of Washes so the rows don't repeat the date.
        val zone = java.time.ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        washes.groupBy { java.time.Instant.ofEpochMilli(it.startedAtEpochMs).atZone(zone).toLocalDate() }.forEach { (_, day) ->
            item {
                Text(
                    DateUtils.getRelativeTimeSpanString(day.first().startedAtEpochMs, now, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString(),
                    morphContent(spec).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 2.dp).semantics { heading() },
                    color = Five.TextSecondary,
                    style = FiveType.label,
                )
            }
            items(day) { w ->
                // The outcome leads (tiny rings, then the fraction); the time and length follow. Washes with no Coverage are
                // dashed and dimmed.
                FilledTonalButton(
                    onClick = { onOpen(w.id) },
                    modifier = morph(spec).then(if (w.coverageShown) Modifier else Modifier.graphicsLayer { alpha = 0.85f }),
                    transformation = SurfaceTransformation(spec),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Five.Platter, contentColor = Color.White, secondaryContentColor = Five.TextSecondary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    // Empty rings would read as a score of zero: no Step looked covered gets the dashed circle too.
                    icon = { if (w.coverageShown && w.coverage.any { it.status == StepStatus.OK }) FiveRings(w.coverage, HISTORY_RING_SIZE) else DashedDisc() },
                    label = { Text(hint(LocalContext.current.resources, w), maxLines = 2, style = FiveType.bodySmall.copy(fontWeight = FontWeight.SemiBold)) },
                    secondaryLabel = { Text("${DateUtils.formatDateTime(context, w.startedAtEpochMs, DateUtils.FORMAT_SHOW_TIME)} · ${duration(w.durationS)}", maxLines = 1, style = FiveType.label) },
                )
            }
        }
    }
}

@Composable
internal fun CreditsScreen() {
    ListScreen(wash = null) { spec ->
        item { Header(spec, stringResource(R.string.credits), Color.White) }
        // The five illustrations as a strip, each ringed in its Step's colour.
        item {
            Row(morphContent(spec).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
                STEP_ART.forEachIndexed { i, art ->
                    Box(Modifier.size(30.dp).clip(CircleShape).background(Five.Steps[i]), contentAlignment = Alignment.Center) {
                        Image(painterResource(art), null, Modifier.size(26.dp).clip(CircleShape))
                    }
                }
            }
        }
        // The URL has no spaces; a zero-width space after "/" and "_" lets it wrap on the round edge instead of mid-word.
        item {
            Column(morphContent(spec).padding(horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.credits_illustrations), color = Five.TextSecondary, style = FiveType.labelSmall)
                Text(stringResource(R.string.credits_author), style = FiveType.bodySmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.credits_licence), color = Five.TextSecondary, style = FiveType.label, textAlign = TextAlign.Center)
                Text(stringResource(R.string.credits_body).substringAfter("cropped. ").substringAfter("recortadas. ").replace("/", "/​").replace("_", "_​"), Modifier.padding(top = 6.dp), color = Five.TextTertiary, style = FiveType.labelSmall.copy(lineHeight = 1.3.em), textAlign = TextAlign.Center)
            }
        }
    }
}

@WearPreviewDevices
@Composable
private fun HistoryPreview() = PreviewFrame { HistoryScreen(onOpen = {}) }
