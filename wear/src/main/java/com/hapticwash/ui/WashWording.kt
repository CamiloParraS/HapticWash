package com.hapticwash.ui

import android.content.Context
import android.content.res.Resources
import android.text.format.DateUtils
import com.hapticwash.R
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashSession

/*
 * How a Wash is put into words and arcs, for every surface that shows one: the app's screens, the Tile and the
 * complication. Step Status is an estimate, never a verdict (§4.1.9); the choices here are pure so unit tests pin
 * them, and the strings themselves are linted by LanguageTest.
 */

/** How much of a Step's arc is filled: shape carries the Status, not colour (§4.1.10). */
internal fun share(s: StepStatus) = when (s) {
    StepStatus.OK -> 1f
    StepStatus.BRIEF -> 0.5f
    StepStatus.NOT_DETECTED -> 0f
}

/** A Step Status's words on a Summary row. */
internal fun statusText(s: StepStatus) = when (s) {
    StepStatus.OK -> R.string.status_ok
    StepStatus.BRIEF -> R.string.status_brief
    StepStatus.NOT_DETECTED -> R.string.status_not_detected
}

/** Which few words sum up [w]: the string, and the Steps looking covered when the string counts them. */
internal fun hintOf(w: WashSession): Pair<Int, Int?> = when {
    w.modelVersion == null -> R.string.timer_only_short to null
    !w.coverageShown -> R.string.too_short_short to null
    w.coverage.none { it.status == StepStatus.OK } -> R.string.none_detected_short to null
    else -> R.string.history_hint to w.coverage.count { it.status == StepStatus.OK }
}

/** A Wash in a few words, for History rows, Start, the Tile and the complication. */
internal fun hint(res: Resources, w: WashSession): String =
    hintOf(w).let { (id, n) -> if (n == null) res.getString(id) else res.getString(id, n) }

/** "Last wash · 7:15 AM", or the date once it is not today. Start and the Tile. */
internal fun lastWashLabel(context: Context, w: WashSession) =
    "${context.getString(R.string.last_wash_title)} · ${DateUtils.getRelativeTimeSpanString(context, w.startedAtEpochMs)}"

/** Seconds as `m:ss`, whole seconds, rounded down. */
internal fun duration(s: Float): String {
    val t = s.toInt()
    return "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
}
