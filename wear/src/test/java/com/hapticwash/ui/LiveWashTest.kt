package com.hapticwash.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** What the live Wash screen shows at each second: time-driven, the same for everyone, never a Step Status. */
class LiveWashTest {
    private val cues = Array(8) { "cue$it" }
    private val cheers = Array(3) { "cheer$it" }

    @Test
    fun countsDownToTheTargetThenUpFromIt() {
        assertEquals(40f, LiveWash.shownS(0f))
        assertEquals(40f, LiveWash.shownS(0.4f)) // a partial second still shows the whole one left
        assertEquals(1f, LiveWash.shownS(39.2f))
        assertFalse(LiveWash.full(39.99f))
        assertTrue(LiveWash.full(40f))
        assertEquals(40f, LiveWash.shownS(40f))
        assertEquals(41f, LiveWash.shownS(41.9f))
    }

    @Test
    fun theRingFillsToTheTargetThenLapsItself() {
        assertEquals(0.5f, LiveWash.ringFill(20f), 1e-6f)
        assertEquals(1f, LiveWash.ringFill(55f))
        assertEquals(0.25f, LiveWash.overFill(50f), 1e-6f)
        assertEquals(0.25f, LiveWash.overFill(90f), 1e-6f) // second lap past the target
    }

    @Test
    fun fourLapsOnTheWayAndNoneAfter() {
        assertEquals(listOf(0, 0, 1, 2, 3, 0), listOf(0f, 9.9f, 10f, 25f, 39.9f, 45f).map(LiveWash::lap))
    }

    @Test
    fun theGlowSwellsOnlyOverTheLastSecondsBeforeTheTarget() {
        assertEquals(0f, LiveWash.glowRamp(30f))
        assertEquals(0.5f, LiveWash.glowRamp(RING_TARGET_S - GLOW_RAMP_S / 2), 1e-6f)
        assertEquals(0f, LiveWash.glowRamp(40f))
    }

    @Test
    fun cuesChangeEveryFiveSecondsHoldOnTheLastThenCheersTakeTurns() {
        assertEquals("cue0", LiveWash.cue(0f, cues, cheers))
        assertEquals("cue1", LiveWash.cue(5f, cues, cheers))
        assertEquals("cue7", LiveWash.cue(39f, cues, cheers))
        assertEquals("cheer0", LiveWash.cue(40f, cues, cheers))
        assertEquals("cheer1", LiveWash.cue(46f, cues, cheers))
        assertEquals("cheer0", LiveWash.cue(58f, cues, cheers)) // wraps round
    }
}
