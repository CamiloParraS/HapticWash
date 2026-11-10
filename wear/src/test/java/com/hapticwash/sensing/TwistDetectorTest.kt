package com.hapticwash.sensing

import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Test

class TwistDetectorTest {
    /** A pulse: `(startS, durationS, peakRadPerS)`, a half sine; negative peak = the other direction. */
    private data class Pulse(val start: Double, val len: Double, val peak: Double)

    /** Feeds 20 s at [hz] with the pulses on [axis] ("gx" roll, or "gy"), times [sign]; returns how many Twists fired. */
    private fun fires(pulses: List<Pulse>, hz: Int = 50, sign: Float = 1f, axis: String = "gx"): Int {
        val d = TwistDetector()
        val stepNs = 1_000_000_000L / hz
        var count = 0
        for (i in 0 until 20 * hz) {
            val t = i * stepNs / 1e9
            val v = pulses.sumOf { (start, len, peak) -> if (t in start..start + len) peak * sin(PI * (t - start) / len) else 0.0 }.toFloat() * sign
            val s = if (axis == "gx") MotionSample(i * stepNs, 0f, 0f, 0f, v, 0f, 0f) else MotionSample(i * stepNs, 0f, 0f, 0f, 0f, v, 0f)
            if (d.push(s)) count++
        }
        return count
    }

    /**
     * [n] rotations of alternating direction, [everyS] apart from [startS]: the rhythm of a natural Twist on
     * the watch (each rotation ~0.5-0.9 s apart, peaks 4-9 rad/s). A Twist is two out-and-backs = 4.
     */
    private fun rotations(n: Int, startS: Double = 1.0, everyS: Double = 0.9, peak: Double = 6.0, lenS: Double = 0.5) =
        List(n) { Pulse(startS + it * everyS, lenS, if (it % 2 == 0) peak else -peak) }

    @Test fun naturalTwistFiresOnce() = assertEquals(1, fires(rotations(4)))

    @Test fun threeOutAndBacksFireOnce() = assertEquals(1, fires(rotations(6)))

    // Twist start listens at 25 Hz (ListeningSource).
    @Test fun naturalTwistFiresOnceAtTheListeningRate() = assertEquals(1, fires(rotations(4), hz = 25))

    @Test fun mirroredRightWristFiresTheSame() = assertEquals(1, fires(rotations(4), sign = -1f))

    @Test fun threeRotationsDoNotFire() = assertEquals(0, fires(rotations(3)))

    // Washing alternates every 0.2-0.4 s, and does not stop after a few.
    @Test fun fastAlternationThenStillDoesNotFire() = assertEquals(0, fires(rotations(14, everyS = 0.3, lenS = 0.25, peak = 10.0)))

    // Wearers often keep going past three out-and-backs.
    @Test fun sixOutAndBacksFire() = assertEquals(1, fires(rotations(12, everyS = 0.6, lenS = 0.4)))

    @Test fun briskTwistFires() = assertEquals(1, fires(rotations(4, everyS = 0.45, lenS = 0.35)))

    @Test fun longChainOfRotationsDoesNotFire() = assertEquals(0, fires(rotations(14, everyS = 0.6, lenS = 0.4)))

    @Test fun rotationsTooFarApartDoNotFire() = assertEquals(0, fires(rotations(4, everyS = 2.5)))

    @Test fun oneBigSlowRollDoesNotFire() = assertEquals(0, fires(listOf(Pulse(1.0, 3.0, 8.0), Pulse(4.0, 3.0, -8.0))))

    // Wrist flexion lands on gy: not a forearm rotation.
    @Test fun theSameMotionOnGyDoesNotFire() = assertEquals(0, fires(rotations(4), axis = "gy"))

    @Test fun twoTwistsAfterRefractoryFireTwice() = assertEquals(2, fires(rotations(4) + rotations(4, startS = 11.0)))

    // Held-out uwash segments (uwash_library_2) with the strongest roll, 6 s each, 12 segments: washing is not a Twist.
    @Test fun washingSegmentsDoNotFire() = assertEquals(0, washingFires(1))

    // Every other row: the 25 Hz Twist start listens at.
    @Test fun washingSegmentsDoNotFireAtTheListeningRate() = assertEquals(0, washingFires(2))

    private fun washingFires(every: Int): Int {
        val rows = javaClass.getResourceAsStream("/uwash_high_gx.csv")!!.bufferedReader().readLines().drop(1)
        return rows.chunked(300).sumOf { segment ->
            val d = TwistDetector()
            segment.filterIndexed { i, _ -> i % every == 0 }.count {
                val (ts, gx) = it.split(',')
                d.push(MotionSample(ts.toLong(), 0f, 0f, 0f, gx.toFloat(), 0f, 0f))
            }
        }
    }
}
