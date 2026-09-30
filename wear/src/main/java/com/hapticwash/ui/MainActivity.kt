package com.hapticwash.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.hapticwash.R
import com.hapticwash.inference.StepModel
import com.hapticwash.session.History
import com.hapticwash.session.Settings
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashService
import com.hapticwash.session.WashSession
import com.hapticwash.session.Wrist
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Progress ring target. A UI constant: nothing in Coverage reads it. */
private const val RING_TARGET_S = 40f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HapticWashTheme { AppScaffold { App() } } }
    }
}

/** Max depth 2 (§4.1.5): Start → In-progress → Summary, Start → History → Summary, Start → Settings. */
@Composable
fun App() {
    val nav = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(nav, startDestination = "start") {
        composable("start") {
            StartScreen(onWash = { nav.navigate("wash") }, onHistory = { nav.navigate("history") }, onSettings = { nav.navigate("settings") })
        }
        composable("wash") { WashScreen(onEnd = { id -> nav.navigate("summary/$id") { popUpTo("start") } }) }
        composable("history") { HistoryScreen(onOpen = { nav.navigate("summary/$it") }) }
        composable("summary/{id}") { SummaryScreen(it.arguments?.getString("id")?.toLongOrNull()) }
        composable("settings") { SettingsScreen() }
    }
}

/** A scrollable, round-safe screen with [TimeText][AppScaffold] on top. */
@Composable
private fun ListScreen(content: TransformingLazyColumnScope.() -> Unit) {
    val state = rememberTransformingLazyColumnState()
    ScreenScaffold(state) { padding ->
        TransformingLazyColumn(state = state, contentPadding = padding, content = content)
    }
}

@Composable
private fun StartScreen(onWash: () -> Unit, onHistory: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    var timerOnly by remember { mutableStateOf(false) }
    fun begin() {
        if (!status.running) WashService.start(context)
        onWash()
    }
    // Asked on the first Start, not at launch; the Wash begins whatever the answer.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { begin() }
    LaunchedEffect(Unit) {
        // Loads the model off the main thread; also the release smoke check's log line.
        timerOnly = withContext(Dispatchers.Default) { StepModel.bundled(context) == null }
    }
    ListScreen {
        item { ListHeader { Text(stringResource(R.string.app_name)) } }
        item {
            Button(
                onClick = {
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) begin()
                    else askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(if (status.running) R.string.session_running else R.string.start_wash)) },
            )
        }
        if (timerOnly) item { Notice(stringResource(R.string.timer_only)) }
        item { FilledTonalButton(onHistory, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.history)) }) }
        item { FilledTonalButton(onSettings, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.settings)) }) }
    }
}

/** Elapsed time, optional progress ring, Stop. Never names a Step. */
@Composable
private fun WashScreen(onEnd: (Long) -> Unit) {
    val context = LocalContext.current
    val status by WashService.status.collectAsState()
    val last by WashService.lastWash.collectAsState()
    val before = remember { last?.id }
    LaunchedEffect(last) { last?.takeIf { it.id != before }?.let { onEnd(it.id) } }
    val ring = remember { Settings(context).progressRing }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(250)
        }
    }
    // Derived state, so the ring's progress lambda reads the live value at draw time.
    val elapsedS by remember { derivedStateOf { if (status.running) ((now - status.startedAtEpochMs) / 1000f).coerceAtLeast(0f) else 0f } }
    ScreenScaffold { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // A 60° gap centred on 12 o'clock leaves TimeText clear.
            if (ring) {
                CircularProgressIndicator(
                    progress = { (elapsedS / RING_TARGET_S).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding),
                    startAngle = 300f,
                    endAngle = 240f,
                )
            }
            Column(
                Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(duration(elapsedS), style = MaterialTheme.typography.displayMedium)
                if (status.timerOnly) Notice(stringResource(R.string.timer_only))
                // Wide: wet, soapy fingers miss small targets.
                Button(onClick = { WashService.stop(context) }, modifier = Modifier.fillMaxWidth(0.8f), label = { Text(stringResource(R.string.stop_wash), Modifier.fillMaxWidth(), textAlign = TextAlign.Center) })
            }
        }
    }
}

/** A Wash's Summary: the one just ended, or one from History. */
@Composable
private fun SummaryScreen(id: Long?) {
    val context = LocalContext.current
    val wash = remember(id) {
        WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(context).read().find { it.id == id }
    }
    val steps = stringArrayResource(R.array.steps)
    ListScreen {
        if (wash == null) return@ListScreen
        val shown = wash.modelVersion != null && wash.coverageShown
        // The takeaway first, so the answer is on screen before any scrolling.
        item { ListHeader { Text(if (shown) stringResource(R.string.history_hint, wash.coverage.count { it.status == StepStatus.OK }) else duration(wash.durationS)) } }
        when {
            wash.modelVersion == null -> item { Notice(stringResource(R.string.timer_only)) }
            !wash.coverageShown -> item { Notice(stringResource(R.string.too_short)) }
            else -> {
                item { Notice(duration(wash.durationS)) }
                items(wash.coverage) { c ->
                    // Icon and text, never colour alone (§4.1.10); the glyph is read as the text.
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(glyph(c.status), Modifier.width(20.dp).clearAndSetSemantics {}, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Column {
                            Text(steps[c.label - 1], style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(statusText(c.status)), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(onOpen: (Long) -> Unit) {
    val context = LocalContext.current
    val washes by remember { History.get(context).washes }.collectAsState()
    ListScreen {
        item { ListHeader { Text(stringResource(R.string.history)) } }
        if (washes.isEmpty()) item { Notice(stringResource(R.string.history_empty)) }
        items(washes) { w ->
            FilledTonalButton(
                onClick = { onOpen(w.id) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(DateUtils.formatDateTime(context, w.startedAtEpochMs, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH)) },
                secondaryLabel = { Text("${duration(w.durationS)} · ${hint(w)}") },
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    var wrist by remember { mutableStateOf(settings.wrist) }
    var haptics by remember { mutableStateOf(settings.haptics) }
    var ring by remember { mutableStateOf(settings.progressRing) }
    ListScreen {
        item { ListHeader { Text(stringResource(R.string.settings)) } }
        // Two visible options, no dialog (§4.1.5). Read at Wash start, so a change applies from the next Wash.
        item { ListSubHeader { Text(stringResource(R.string.wrist_side)) } }
        for ((side, name) in listOf(Wrist.LEFT to R.string.wrist_left, Wrist.RIGHT to R.string.wrist_right)) {
            item {
                RadioButton(
                    selected = wrist == side,
                    onSelect = { wrist = side; settings.wrist = side },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(name)) },
                )
            }
        }
        item { Notice(stringResource(R.string.applies_next_wash)) }
        item {
            SwitchButton(haptics, { haptics = it; settings.haptics = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.haptics)) })
        }
        item {
            SwitchButton(ring, { ring = it; settings.progressRing = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.progress_ring)) })
        }
        item {
            SwitchButton(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                label = { Text(stringResource(R.string.always_on)) },
                secondaryLabel = { Text(stringResource(R.string.coming_later)) },
            )
        }
    }
}

@Composable
private fun Notice(text: String) =
    Text(text, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

@Composable
private fun hint(w: WashSession): String = when {
    w.modelVersion == null -> stringResource(R.string.timer_only_short)
    !w.coverageShown -> stringResource(R.string.too_short_short)
    else -> stringResource(R.string.history_hint, w.coverage.count { it.status == StepStatus.OK })
}

private fun duration(s: Float): String {
    val t = s.toInt()
    return "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
}

private fun glyph(s: StepStatus) = when (s) {
    StepStatus.OK -> "✓"
    StepStatus.BRIEF -> "◐"
    StepStatus.NOT_DETECTED -> "○"
}

private fun statusText(s: StepStatus) = when (s) {
    StepStatus.OK -> R.string.status_ok
    StepStatus.BRIEF -> R.string.status_brief
    StepStatus.NOT_DETECTED -> R.string.status_not_detected
}
