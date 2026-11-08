package com.hapticwash.ui

import com.hapticwash.R
import com.hapticwash.session.StepCoverage
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Wash wording module: what every surface (screens, Tile, complication) says about a Wash. */
class WashWordingTest {
    private fun wash(vararg s: StepStatus, model: String? = "v1", shown: Boolean = true) = WashSession(
        id = 1, startedAtEpochMs = 0, durationS = 45f,
        coverage = s.mapIndexed { i, st -> StepCoverage(i + 1, 5f, st) },
        washOtherS = 0f, modelVersion = model, source = "sensor", coverageShown = shown,
    )

    private val ok = StepStatus.OK
    private val brief = StepStatus.BRIEF
    private val none = StepStatus.NOT_DETECTED

    @Test
    fun hintSaysTimerOnlyBeforeAnythingAboutCoverage() {
        assertEquals(R.string.timer_only_short to null, hintOf(wash(ok, ok, ok, ok, ok, model = null, shown = false)))
    }

    @Test
    fun hintSaysTooShortWhenCoverageWasNotShown() {
        assertEquals(R.string.too_short_short to null, hintOf(wash(ok, ok, ok, ok, ok, shown = false)))
    }

    @Test
    fun hintNeverCountsZeroSteps() {
        assertEquals(R.string.none_detected_short to null, hintOf(wash(brief, none, brief, none, none)))
    }

    @Test
    fun hintCountsOnlyStepsThatLookCovered() {
        assertEquals(R.string.history_hint to 2, hintOf(wash(ok, brief, ok, none, brief)))
        assertEquals(R.string.history_hint to 5, hintOf(wash(ok, ok, ok, ok, ok)))
    }

    @Test
    fun arcShareGrowsWithStatusAndEveryStatusHasItsOwnWords() {
        assertTrue(share(none) < share(brief) && share(brief) < share(ok))
        assertEquals(0f, share(none))
        assertEquals(1f, share(ok))
        assertEquals(StepStatus.entries.size, StepStatus.entries.map(::statusText).toSet().size)
    }

    @Test
    fun durationIsMinutesAndPaddedWholeSeconds() {
        assertEquals("0:00", duration(0f))
        assertEquals("0:45", duration(45.9f))
        assertEquals("1:05", duration(65f))
        assertEquals("10:00", duration(600f))
    }
}
