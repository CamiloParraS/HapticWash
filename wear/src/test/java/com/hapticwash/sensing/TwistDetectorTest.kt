package com.hapticwash.sensing

import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Test

class TwistDetectorTest {
    /** Roll-axis (gy) signal for 20 s at [hz]: each pulse is `(startS, durationS, peakRadPerS)`, a half sine, negative peak = opposite direction. */
    private fun fires(sign: Float, vararg pulses: Triple<Double, Double, Double>, hz: Int = 50): Int {
        val d = TwistDetector()
        val stepNs = 1_000_000_000L / hz
        var count = 0
        for (i in 0 until 20 * hz) {
            val t = i * stepNs / 1e9
            val gy = pulses.sumOf { (start, len, peak) ->
                if (t in start..start + len) peak * sin(PI * (t - start) / len) else 0.0
            }.toFloat() * sign
            if (d.push(MotionSample(i * stepNs, 0f, 0f, 0f, 0f, gy, 0f))) count++
        }
        return count
    }

    private fun twists(vararg starts: Double) = starts.withIndex().map { (i, s) -> Triple(s, 0.25, if (i % 2 == 0) 8.0 else -8.0) }.toTypedArray()

    @Test fun threeTwistsFireOnce() = assertEquals(1, fires(1f, *twists(1.0, 1.4, 1.8)))

    // Twist start listens at 25 Hz (ListeningSource).
    @Test fun threeTwistsFireOnceAtTheListeningRate() = assertEquals(1, fires(1f, *twists(1.0, 1.4, 1.8), hz = 25))

    @Test fun twoTwistsDoNotFire() = assertEquals(0, fires(1f, *twists(1.0, 1.4)))

    @Test fun threeTwistsWiderThanWindowDoNotFire() = assertEquals(0, fires(1f, *twists(1.0, 2.0, 3.0)))

    // One long turn of the wrist, well above threshold, that goes out and comes back.
    @Test fun oneBigSlowRollDoesNotFire() = assertEquals(0, fires(1f, Triple(1.0, 3.0, 8.0), Triple(4.0, 3.0, -8.0)))

    @Test fun strayFlickThenPauseThenThreeTwistsFiresOnTheThird() =
        assertEquals(1, fires(1f, Triple(1.0, 0.25, 8.0), *twists(3.0, 3.4, 3.8)))

    @Test fun mirroredRightWristFiresTheSame() = assertEquals(1, fires(-1f, *twists(1.0, 1.4, 1.8)))

    @Test fun silentDuringRefractoryThenFiresAgain() = assertEquals(2, fires(1f, *twists(1.0, 1.4, 1.8, 6.0, 6.4, 6.8)))

    // Held-out uwash segments with the highest roll (6 s each, 12 segments): hand washing is not a Twist.
    @Test fun washingSegmentsDoNotFire() = assertEquals(0, washingFires(1))

    // Every other row: the 25 Hz Twist start listens at.
    @Test fun washingSegmentsDoNotFireAtTheListeningRate() = assertEquals(0, washingFires(2))

    private fun washingFires(every: Int): Int {
        val rows = javaClass.getResourceAsStream("/uwash_high_roll.csv")!!.bufferedReader().readLines().drop(1)
        return rows.chunked(300).sumOf { segment ->
            val d = TwistDetector()
            segment.filterIndexed { i, _ -> i % every == 0 }.count {
                val (ts, gy) = it.split(',')
                d.push(MotionSample(ts.toLong(), 0f, 0f, 0f, 0f, gy.toFloat(), 0f))
            }
        }
    }
}
