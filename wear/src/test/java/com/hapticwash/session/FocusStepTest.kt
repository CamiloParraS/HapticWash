package com.hapticwash.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Coaching ticket 04: the Focus Step. Histories are newest first, like WashStore. */
class FocusStepTest {
    private val O = StepStatus.OK
    private val B = StepStatus.BRIEF
    private val N = StepStatus.NOT_DETECTED

    private fun wash(vararg s: StepStatus, source: String = "sensor", shown: Boolean = true) = WashSession(
        0, 0, 30f, s.mapIndexed { i, st -> StepCoverage(i + 1, 0f, st) }, 0f, if (shown) "m" else null, source, shown,
    )

    private fun times(n: Int, w: WashSession) = List(n) { w }
    private val thumbsN = wash(O, O, O, N, O)
    private val allOk = wash(O, O, O, O, O)

    @Test
    fun fewerThanFiveQualifyingWashesGivesNone() {
        assertNull(focusStep(times(4, thumbsN)))
        assertEquals(4, focusStep(times(5, thumbsN)))
    }

    @Test
    fun timerOnlyTooShortAndReplayWashesNeitherCountNorFillTheWindow() {
        val noise = times(10, wash(O, O, O, N, O, shown = false))
        assertNull(focusStep(noise + times(4, thumbsN)))
        assertEquals(4, focusStep(noise + times(5, thumbsN)))
        assertNull(focusStep(times(5, wash(O, O, O, N, O, source = "replay"))))
    }

    @Test
    fun sixtyPercentButUnderThirtyPercentNotDetectedGivesNone() {
        val brief = wash(O, O, O, B, O)
        assertNull(focusStep(times(6, brief) + times(4, allOk))) // 60 % brief, 0 % not detected
        assertNull(focusStep(times(2, thumbsN) + times(4, brief) + times(4, allOk))) // 60 %, 20 %
        assertEquals(4, focusStep(times(3, thumbsN) + times(3, brief) + times(4, allOk))) // 60 %, 30 %
        assertNull(focusStep(times(5, thumbsN) + times(5, allOk))) // 50 %
    }

    @Test
    fun higherNotDetectedRateWinsAndTiesGoToTheLaterStep() {
        val palmsAndThumbs = wash(N, O, O, N, O)
        val palmsThumbsBrief = wash(N, O, O, B, O)
        assertEquals(1, focusStep(times(4, palmsAndThumbs) + times(3, palmsThumbsBrief) + times(3, allOk))) // palms 70 % vs thumbs 40 %
        assertEquals(4, focusStep(times(6, palmsAndThumbs) + times(4, allOk))) // equal rates
    }

    @Test
    fun theFocusLineOpensItsStepsGuideFromSummary() {
        val label = focusStep(times(5, thumbsN))!!
        assertEquals("guide/4", com.hapticwash.ui.Routes.guide(label)) // same route as the Step's row
        assertEquals(true, com.hapticwash.ui.Routes.GUIDE in com.hapticwash.ui.Routes.links.getValue(com.hapticwash.ui.Routes.SUMMARY))
    }

    @Test
    fun onlyTheLastTenQualifyingWashesCountAndItClearsWhenTheyStopQualifying() {
        assertEquals(4, focusStep(times(10, thumbsN)))
        assertNull(focusStep(times(5, allOk) + times(5, thumbsN))) // 50 % of the window
        assertNull(focusStep(times(10, allOk) + times(10, thumbsN))) // older ones fall out
    }

    @Test
    fun countsAreNotDetectedWashesOutOfTheWindow() {
        assertEquals(5 to 5, focusCounts(times(5, thumbsN), 4))
        assertEquals(0 to 6, focusCounts(times(6, allOk), 4))
        assertEquals(3 to 5, focusCounts(times(3, thumbsN) + times(2, allOk), 4))
    }
}
