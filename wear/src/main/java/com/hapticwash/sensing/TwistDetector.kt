package com.hapticwash.sensing

import kotlin.math.abs

/**
 * Calibration knobs for [TwistDetector]; all three need tuning on real hardware (M6).
 * Defaults: 4 rad/s (~230 deg/s) is well above a casual wrist turn but easy for a deliberate
 * flick; three flicks take about a second, so 1.5 s; 3 s of silence covers the tail of the
 * gesture and keeps one Twist from firing twice.
 */
data class TwistConfig(
    val thresholdRadPerS: Float = 4f,
    val windowNs: Long = 1_500_000_000,
    val refractoryNs: Long = 3_000_000_000,
)

/**
 * Reports a Twist: three forearm rotations (gyro roll peaks of alternating sign) inside
 * [TwistConfig.windowNs]. Roll is about the watch y axis. Sign-agnostic, so right-wrist
 * mirroring (gy negated) needs no handling. One instance per Wash.
 */
class TwistDetector(private val config: TwistConfig = TwistConfig()) {
    private val peaks = LongArray(3)
    private var n = 0
    private var lastSign = 0
    private var silentUntil = Long.MIN_VALUE

    /** Feed samples in order; true on the sample that completes a Twist. */
    fun push(s: MotionSample): Boolean {
        if (s.timestampNs < silentUntil) return false
        if (abs(s.gy) < config.thresholdRadPerS) return false
        val sign = if (s.gy > 0) 1 else -1
        if (sign == lastSign) return false // still the same rotation
        lastSign = sign
        if (n == 3) { peaks[0] = peaks[1]; peaks[1] = peaks[2]; n = 2 }
        peaks[n++] = s.timestampNs
        if (n < 3 || s.timestampNs - peaks[0] > config.windowNs) return false
        n = 0; lastSign = 0
        silentUntil = s.timestampNs + config.refractoryNs
        return true
    }
}
