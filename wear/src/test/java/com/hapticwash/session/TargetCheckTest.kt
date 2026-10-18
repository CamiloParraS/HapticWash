package com.hapticwash.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetCheckTest {
    private fun cov(vararg s: StepStatus) = s.mapIndexed { i, st -> StepCoverage(i + 1, 0f, st) }
    private val ok = cov(*Array(5) { StepStatus.OK })
    private val gaps = cov(StepStatus.OK, StepStatus.OK, StepStatus.BRIEF, StepStatus.NOT_DETECTED, StepStatus.NOT_DETECTED)
    private val t = Haptics.TARGET_MS

    @Test
    fun nothingBeforeTheTarget() {
        assertNull(TargetCheck().poll(t - 1, gaps))
    }

    @Test
    fun nothingUndetectedMeansNoCardAndTargetTicks() {
        val undetected = TargetCheck().poll(t, ok)!!
        assertEquals(emptyList<Int>(), undetected)
        assertEquals(listOf(Pulse.TARGET), Haptics.onTarget("sensor", true, undetected).map { it.pulse })
    }

    @Test
    fun undetectedStepsGetTheCardAndMissedInsteadOfTarget() {
        val undetected = TargetCheck().poll(t, gaps)!!
        assertEquals(listOf(4, 5), undetected)
        assertEquals(listOf(Pulse.MISSED), Haptics.onTarget("sensor", true, undetected).map { it.pulse })
    }

    @Test
    fun firesOncePerWash() {
        val c = TargetCheck()
        assertEquals(listOf(4, 5), c.poll(t, gaps))
        assertNull(c.poll(t + 1_000, gaps))
    }

    @Test
    fun nudgeShiftsTheCheckWithTheTarget() {
        val c = TargetCheck()
        c.nudge(nowMs = 45_000, elapsedMs = 30_000) // 10 s of washing left
        assertNull(c.poll(t, gaps))
        assertNull(c.poll(54_999, gaps))
        assertEquals(listOf(4, 5), c.poll(55_000, gaps))
    }

    @Test
    fun silentWhenHapticsOffOrReplay() {
        assertEquals(emptyList<Buzz>(), Haptics.onTarget("sensor", false, listOf(4)))
        assertEquals(emptyList<Buzz>(), Haptics.onTarget("replay", true, listOf(4)))
    }

    @Test
    fun endPulsesAfterTheCheckWaitForTheGap() {
        val times = Haptics.playTimes(Haptics.onEnd(wash(), true), t0 = t + 500, lastAt = t)
        assertTrue(times.first() >= t + Haptics.GAP_MS)
    }

    @Test
    fun checkOnLeavesTheTargetTicksToTheCheck() {
        assertEquals(listOf(Pulse.START), Haptics.onStart("sensor", true, checkOn = true).map { it.pulse })
        assertEquals(listOf(Pulse.KEEP_GOING), Haptics.onNudge("sensor", true, 30_000, checkOn = true).map { it.pulse })
    }

    @Test
    fun cardLivesInTheStatusSoWashEndClearsIt() {
        assertEquals(emptyList<Int>(), WashStatus().undetected)
        assertEquals(listOf(4), WashStatus(undetected = listOf(4)).undetected)
    }

    @Test
    fun liveCoverageSnapshotMatchesFinish() {
        val m = CoverageStateMachine(null)
        assertEquals(m.finish(0f, 1, "sensor").coverage, m.snapshot())
    }

    private fun wash() = WashSession(1, 1, 45f, gaps, 0f, "0.1.0", "sensor", true)
}
