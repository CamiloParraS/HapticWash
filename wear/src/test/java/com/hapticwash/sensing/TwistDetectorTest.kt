package com.hapticwash.sensing

import org.junit.Assert.assertEquals
import org.junit.Test

class TwistDetectorTest {
    private val stepNs = 20_000_000L // 50 Hz

    /** Roll-axis (gy) signal for 20 s: each pulse is `(startS, durationS, peakRadPerS)`, a half sine of alternating sign. */
    private fun fires(sign: Float, vararg pulses: Triple<Double, Double, Double>): Int {
        val d = TwistDetector()
        var count = 0
        for (i in 0 until 1000) {
            val t = i * stepNs / 1e9
            val gy = pulses.withIndex().sumOf { (k, p) ->
                val (start, len, peak) = p
                val dir = if (k % 2 == 0) 1 else -1
                if (t in start..start + len) dir * peak * Math.sin(Math.PI * (t - start) / len) else 0.0
            }.toFloat() * sign
            if (d.push(MotionSample(i * stepNs, 0f, 0f, 0f, 0f, gy, 0f))) count++
        }
        return count
    }

    private fun twists(vararg starts: Double) = starts.map { Triple(it, 0.25, 8.0) }.toTypedArray()

    @Test fun threeTwistsFireOnce() = assertEquals(1, fires(1f, *twists(1.0, 1.4, 1.8)))

    @Test fun twoTwistsDoNotFire() = assertEquals(0, fires(1f, *twists(1.0, 1.4)))

    @Test fun threeTwistsWiderThanWindowDoNotFire() = assertEquals(0, fires(1f, *twists(1.0, 2.0, 3.0)))

    @Test fun oneSlowRollDoesNotFire() = assertEquals(0, fires(1f, Triple(1.0, 2.0, 2.0), Triple(3.5, 2.0, 2.0)))

    @Test fun mirroredRightWristFiresTheSame() = assertEquals(1, fires(-1f, *twists(1.0, 1.4, 1.8)))

    @Test fun silentDuringRefractoryThenFiresAgain() = assertEquals(2, fires(1f, *twists(1.0, 1.4, 1.8, 6.0, 6.4, 6.8)))
}
