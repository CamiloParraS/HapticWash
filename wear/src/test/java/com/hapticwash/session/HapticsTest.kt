package com.hapticwash.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticsTest {
    private fun wash(vararg statuses: StepStatus, shown: Boolean = true, model: String? = "0.1.0") = WashSession(
        id = 1, startedAtEpochMs = 1, durationS = 30f,
        coverage = statuses.mapIndexed { i, s -> StepCoverage(i + 1, 0f, s) },
        washOtherS = 0f, modelVersion = model, source = "sensor", coverageShown = shown,
    )

    private val missed = wash(StepStatus.OK, StepStatus.OK, StepStatus.BRIEF, StepStatus.OK, StepStatus.NOT_DETECTED)
    private val allOk = wash(*Array(5) { StepStatus.OK })

    @Test
    fun missedStepAddsTapsAtLeast3sAfterTheEndPulses() {
        val b = Haptics.onEnd(missed, hapticsOn = true)
        assertEquals(listOf(Pulse.END, Pulse.MISSED), b.map { it.pulse })
        assertTrue(b[1].atMs >= b[0].atMs + Pulse.END.durationMs + 3000)
    }

    @Test
    fun noMissedTapsWhenAllCoveredTooShortOrTimerOnly() {
        assertEquals(listOf(Buzz(0, Pulse.SUCCESS)), Haptics.onEnd(allOk, hapticsOn = true))
        assertEquals(listOf(Buzz(0, Pulse.END)), Haptics.onEnd(missed.copy(coverageShown = false), hapticsOn = true))
        val timerOnly = wash(*Array(5) { StepStatus.NOT_DETECTED }, shown = false, model = null)
        assertEquals(listOf(Buzz(0, Pulse.END)), Haptics.onEnd(timerOnly, hapticsOn = true))
    }

    @Test
    fun hapticsOffOrReplayIsSilent() {
        assertEquals(emptyList<Buzz>(), Haptics.onEnd(missed, hapticsOn = false))
        assertEquals(emptyList<Buzz>(), Haptics.onEnd(missed.copy(source = "replay"), hapticsOn = true))
        assertEquals(emptyList<Buzz>(), Haptics.onStart("sensor", hapticsOn = false))
        assertEquals(emptyList<Buzz>(), Haptics.onStart("replay", hapticsOn = true))
        assertEquals(listOf(Pulse.START, Pulse.TARGET), Haptics.onStart("sensor", hapticsOn = true).map { it.pulse })
    }

    @Test
    fun timeReachedTicksAtTheTargetAndIsItsOwnPattern() {
        val (start, target) = Haptics.onStart("sensor", hapticsOn = true)
        assertEquals(Haptics.TARGET_MS, target.atMs)
        assertTrue(target.atMs >= start.atMs + Pulse.START.durationMs + Haptics.GAP_MS)
        assertEquals(Pulse.entries.size, Pulse.entries.map { it.timings.toList() }.toSet().size)
    }
}
