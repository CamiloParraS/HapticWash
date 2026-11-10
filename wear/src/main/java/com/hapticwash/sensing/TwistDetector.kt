package com.hapticwash.sensing

import kotlin.math.abs

/**
 * Calibration knobs for [TwistDetector]. Set on a Galaxy Watch7 (2026-11-10) from one wearer's 18
 * natural Twists: 15 fire, and none of that wearer's walking or washing does. Public data: 8 fires in
 * 235 min of washing (which starts a Wash anyway), 21 in 77 min of non-washing; six rotations had 4 and
 * 2, chosen against because repeated six-rotation Twists strained the wearer's wrist. One wearer, tuned
 * on these same Twists: retune on more wearers at M6.
 */
data class TwistConfig(
    /** A rotation counts once gx passes this. Natural Twists peaked at 4-9 rad/s. */
    val thresholdRadPerS: Float = 3.5f,
    /** Sign changes that make a Twist: two out-and-back rotations. */
    val alternations: Int = 4,
    /** More than this many in one chain is continuous rotating, not a Twist; wearers often do 4-6 out-and-backs. */
    val maxAlternations: Int = 12,
    /** Consecutive rotations closer than this are washing (0.2-0.4 s), not a Twist (0.4-1.2 s). */
    val minGapNs: Long = 400_000_000,
    /** A longer pause ends the chain. */
    val maxGapNs: Long = 2_000_000_000,
    /** The Twist fires once gx stays below this for [quietNs]: washing never goes still. */
    val quietRadPerS: Float = 2.5f,
    val quietNs: Long = 700_000_000,
    /** Silence after a fire, so one Twist can't fire twice. */
    val refractoryNs: Long = 3_000_000_000,
)

/**
 * Reports a Twist: [TwistConfig.alternations] forearm rotations of alternating sign, each a natural
 * rhythm apart, then stillness. Roll is about the watch x axis (along the forearm): on the watch and
 * in the training data, forearm rotation lands on gx and barely moves gy. Sign-agnostic, so mirroring
 * needs no handling. One instance per listening run.
 */
class TwistDetector(private val config: TwistConfig = TwistConfig()) {
    private var count = 0
    private var lastSign = 0
    private var lastPeakNs = Long.MIN_VALUE
    private var movedNs = Long.MIN_VALUE
    private var silentUntil = Long.MIN_VALUE

    /** Feed samples in order; true on the sample that completes a Twist. */
    fun push(s: MotionSample): Boolean {
        val t = s.timestampNs
        if (t < silentUntil) return false
        val x = s.gx
        if (abs(x) >= config.quietRadPerS) {
            movedNs = t
        } else if (count >= config.alternations && count <= config.maxAlternations && t - movedNs >= config.quietNs) {
            reset()
            silentUntil = t + config.refractoryNs
            return true
        }
        if (count > 0 && t - lastPeakNs > config.maxGapNs) reset()
        if (abs(x) < config.thresholdRadPerS) return false
        val sign = if (x > 0) 1 else -1
        if (sign == lastSign) return false // still the same rotation
        if (count > 0 && t - lastPeakNs < config.minGapNs) reset() // too fast: washing
        lastSign = sign
        lastPeakNs = t
        count++
        return false
    }

    private fun reset() {
        count = 0
        lastSign = 0
    }
}
