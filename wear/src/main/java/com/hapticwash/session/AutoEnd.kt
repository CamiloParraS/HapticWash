package com.hapticwash.session

import com.hapticwash.inference.ModelMeta

/** Auto-end calibration knobs (SPEC-APP M5), next to the Coverage thresholds. */
data class AutoEndConfig(
    /** All-NULL for this long from the start: the Wash was never begun, cancel it. */
    val idleS: Float = 10f,
    /** NULL for this long counts as "stopped washing". */
    val sustainedNullS: Float = 8f,
    val targetS: Float = Haptics.TARGET_MS / 1000f,
)

enum class AutoEndAction { NONE, NUDGE, CANCEL, END }

/**
 * Smoothed window vectors in, what to do about NULL out (label 0; Washing is not NULL).
 * Timer-only (null [meta]) never acts. Time is window time, not the wall clock.
 */
class AutoEnd(private val meta: ModelMeta?, private val config: AutoEndConfig = AutoEndConfig()) {
    private val strideS = meta?.let { it.windowStride.toFloat() / it.sampleRateHz } ?: 0f
    private val windowS = meta?.let { it.windowSize.toFloat() / it.sampleRateHz } ?: 0f
    private val nullLabel = meta?.labels?.indexOf("NULL") ?: -1
    private var n = 0
    private var washed = false
    private var nullRun = 0
    private var nudgedAtNullS = -1f // NULL-run length when this stretch got its nudge, else -1

    /** Seconds of Wash covered by the windows seen so far. */
    val elapsedS get() = if (n == 0) 0f else windowS + (n - 1) * strideS

    fun window(smoothed: FloatArray): AutoEndAction {
        if (meta == null) return AutoEndAction.NONE
        n++
        if (smoothed.indices.maxBy { smoothed[it] } == nullLabel) nullRun++ else { washed = true; nullRun = 0; nudgedAtNullS = -1f }
        val nullS = nullRun * strideS
        return when {
            !washed && elapsedS >= config.idleS -> AutoEndAction.CANCEL
            !washed || nullS < config.sustainedNullS -> AutoEndAction.NONE
            // A nudge told the user to keep going: give them another sustained stretch before ending.
            elapsedS >= config.targetS && (nudgedAtNullS < 0 || nullS >= nudgedAtNullS + config.sustainedNullS) -> AutoEndAction.END
            elapsedS < config.targetS && nudgedAtNullS < 0 -> { nudgedAtNullS = nullS; AutoEndAction.NUDGE }
            else -> AutoEndAction.NONE
        }
    }
}
