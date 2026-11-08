package com.hapticwash.session

import com.hapticwash.inference.ModelMeta
import com.hapticwash.inference.StepClassifier
import com.hapticwash.sensing.MotionSample
import com.hapticwash.sensing.MotionSource
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Whole Washes through [WashRun] on the JVM: a scripted 50 Hz source, a classifier that says whatever the script
 * says for the current second, and a clock that follows the samples. The pieces have their own tests; these pin
 * how they are wired together (spec §8.6).
 */
class WashRunTest {
    private val meta = ModelMeta.parse(File("src/main/assets/model/model_meta.json").readText())
    private val nullLabel = meta.labels.indexOf("NULL")
    private val palms = meta.labels.indexOf("PALM_TO_PALM")

    /** Label index the classifier reports at second t of the Wash. */
    private class Script(val seconds: Int, val at: (Float) -> Int)

    private inner class Harness(
        script: Script,
        timerOnly: Boolean = false,
        kind: String = "sensor",
        targetCheck: Boolean = true,
        haptics: Boolean = true,
        rateHz: Int = 50,
    ) {
        var nowNs = 0L
        val cues = mutableListOf<List<Buzz>>()
        val checks = mutableListOf<List<Int>>()
        var samples = 0

        private val source = object : MotionSource {
            override val nominalRateHz = rateHz
            override fun stream(): Flow<MotionSample> = flow {
                for (n in 0 until script.seconds * 50) {
                    nowNs = n * 20_000_000L
                    samples++
                    emit(MotionSample(nowNs, 0f, 0f, 9.81f, 0f, 0f, 0f))
                }
            }
        }

        private val classifier = object : StepClassifier {
            override val meta = this@WashRunTest.meta
            override fun predict(window: FloatArray) = FloatArray(meta.labels.size) { if (it == script.at(nowNs / 1e9f)) 1f else 0f }
        }

        val run = WashRun(
            source, if (timerOnly) null else classifier, kind, Wrist.LEFT, targetCheck,
            hapticsOn = { haptics }, startedAtEpochMs = 1_000L, nowMs = { nowNs / 1_000_000 },
            out = object : WashRun.Out {
                override fun cues(buzzes: List<Buzz>) { cues += buzzes }
                override fun targetCheck(undetected: List<Int>) { checks += undetected }
            },
        )

        fun wash(): WashOutcome = runBlocking { run.play(); run.finish() }
        val pulses get() = cues.flatten().map { it.pulse }
    }

    @Test
    fun palmsOnlyUntilStoppedEndsWithPalmsCoveredAndTheCheckNamingTheRest() {
        val h = Harness(Script(45) { palms })
        val o = h.wash() as WashOutcome.Ended

        assertEquals(44.98f, o.wash.durationS, 0.01f)
        assertEquals(StepStatus.OK, o.wash.coverage[0].status)
        assertTrue(o.wash.coverage.drop(1).all { it.status == StepStatus.NOT_DETECTED })
        assertTrue(o.wash.coverageShown)
        assertEquals(listOf(listOf(2, 3, 4, 5)), h.checks) // once, at the 40 s target
        assertEquals(listOf(Pulse.START, Pulse.MISSED), h.pulses) // the check owns 40 s: no ticks scheduled at start
        assertEquals(listOf(Pulse.END, Pulse.MISSED), o.cues.map { it.pulse })
    }

    @Test
    fun noWashingSeenIsCancelledAtThirtySecondsAndNeverChecked() {
        val h = Harness(Script(120) { nullLabel })
        val o = h.wash()

        assertTrue(o is WashOutcome.Cancelled)
        assertEquals(30f, o.durationS, 1.6f) // within one window stride of the give-up
        assertTrue("stopped reading the source", h.samples < 120 * 50)
        assertTrue(h.checks.isEmpty())
        assertEquals(listOf(Pulse.END), o.cues.map { it.pulse })
    }

    @Test
    fun washingThatStopsAfterTheTargetAutoEndsAfterSustainedNull() {
        val h = Harness(Script(180) { t -> if (t < 45f) palms else nullLabel })
        val o = h.wash() as WashOutcome.Ended

        assertEquals(60f, o.wash.durationS, 3f) // 45 s of washing + 15 s of confident NULL
        assertTrue(h.samples < 180 * 50)
        assertEquals(StepStatus.OK, o.wash.coverage[0].status)
    }

    @Test
    fun aPauseBeforeTheTargetGetsAKeepGoingNudgeAndTheWashCarriesOn() {
        val h = Harness(Script(50) { t -> if (t < 10f || t >= 28f) palms else nullLabel })
        val o = h.wash() as WashOutcome.Ended

        assertEquals(50f, o.durationS, 0.1f) // ran to the end of the samples
        assertEquals(1, h.pulses.count { it == Pulse.KEEP_GOING })
    }

    @Test
    fun aPauseThatNeverResumesEndsAfterASecondSustainedStretch() {
        val h = Harness(Script(120) { t -> if (t < 10f) palms else nullLabel })
        val o = h.wash()

        assertTrue("washing was seen, so not cancelled", o is WashOutcome.Ended)
        assertEquals(40f, o.durationS, 3f) // 10 s washing + 15 s to the nudge + 15 s more
        assertTrue(Pulse.KEEP_GOING in h.pulses)
    }

    @Test
    fun timerOnlyCountsAndTimesButHasNoCoverageNoCheckAndNoAutoEnd() {
        val h = Harness(Script(70) { nullLabel }, timerOnly = true)
        val o = h.wash() as WashOutcome.Ended

        assertNull(o.wash.modelVersion)
        assertFalse(o.wash.coverageShown)
        assertEquals(70f, o.wash.durationS, 0.1f) // no Auto-end without the model
        assertTrue(h.checks.isEmpty())
        assertEquals(listOf(Buzz(0, Pulse.START), Buzz(Haptics.TARGET_MS, Pulse.TARGET)), h.cues.single())
    }

    @Test
    fun replayWashesAndHapticsOffNeverVibrate() {
        for (h in listOf(Harness(Script(45) { palms }, kind = "replay"), Harness(Script(45) { palms }, haptics = false))) {
            val o = h.wash() as WashOutcome.Ended
            assertTrue(h.pulses.isEmpty())
            assertTrue(o.cues.isEmpty())
            assertEquals(StepStatus.OK, o.wash.coverage[0].status)
        }
    }

    @Test
    fun replayWashesKeepTheirSource() {
        val o = Harness(Script(45) { palms }, kind = "replay").wash() as WashOutcome.Ended
        assertEquals("replay", o.wash.source)
        assertEquals(1_000L, o.wash.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun aSourceAtTheWrongRateIsRefusedBeforeTheWashStarts() {
        Harness(Script(1) { palms }, rateHz = 100)
    }
}
