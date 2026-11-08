package com.hapticwash.ui

import com.hapticwash.session.LatestOutcome
import com.hapticwash.session.StepCoverage
import com.hapticwash.session.StepStatus
import com.hapticwash.session.UserSettings
import com.hapticwash.session.WashOutcome
import com.hapticwash.session.WashSession
import com.hapticwash.session.WashStatus
import com.hapticwash.session.Wrist
import kotlinx.coroutines.flow.MutableStateFlow

/** [UserSettings] in memory, defaulting as the real ones do. */
internal class MemorySettings(
    override var wrist: Wrist = Wrist.LEFT,
    override var haptics: Boolean = true,
    override var progressRing: Boolean = false,
    override var twistStart: Boolean = false,
    override var targetCheck: Boolean = true,
) : UserSettings

/**
 * [WashControls] with no service behind it, for previews and UI tests: state is set directly, and [end] plays the
 * service's part when a Wash ends. Counts [starts] and [stops] so a test can see what the screen asked for.
 */
internal class PreviewWashControls(
    status: WashStatus = WashStatus(),
    history: List<WashSession> = emptyList(),
    override val settings: MemorySettings = MemorySettings(),
    private val timerOnly: Boolean = false,
) : WashControls {
    override val status = MutableStateFlow(status)
    override val outcome = MutableStateFlow<LatestOutcome?>(null)
    override val history = MutableStateFlow(history)
    var starts = 0
        private set
    var stops = 0
        private set

    override fun wash(id: Long) = (outcome.value?.outcome as? WashOutcome.Ended)?.wash?.takeIf { it.id == id } ?: history.value.find { it.id == id }

    override suspend fun timerOnly() = timerOnly

    override fun start() {
        starts++
        if (!status.value.running) status.value = WashStatus(running = true, startedAtEpochMs = System.currentTimeMillis(), timerOnly = timerOnly)
    }

    override fun stop() {
        stops++
    }

    override fun twistStart(on: Boolean) {
        settings.twistStart = on
    }

    /** The running Wash ends with [o], as WashService would publish it. */
    fun end(o: WashOutcome) {
        outcome.value = LatestOutcome((outcome.value?.n ?: 0) + 1, o)
        status.value = WashStatus()
    }

    companion object {
        /** A Wash with the given Step Statuses, [ago] ms before now, for previews. */
        fun wash(vararg s: StepStatus, durationS: Float = 52f, ago: Long = 3_600_000L): WashSession {
            val at = System.currentTimeMillis() - ago
            return WashSession(
                id = at, startedAtEpochMs = at, durationS = durationS,
                coverage = s.mapIndexed { i, st -> StepCoverage(i + 1, if (st == StepStatus.OK) 8f else if (st == StepStatus.BRIEF) 3f else 0f, st) },
                washOtherS = 6f, modelVersion = "preview", source = "sensor", coverageShown = true,
            )
        }

        /** A Wash running for [elapsedS] seconds. */
        fun running(elapsedS: Float, timerOnly: Boolean = false) =
            WashStatus(running = true, startedAtEpochMs = System.currentTimeMillis() - (elapsedS * 1000).toLong(), timerOnly = timerOnly)
    }
}
