package com.hapticwash.session

import com.hapticwash.inference.ModelMeta

enum class StepStatus { NOT_DETECTED, BRIEF, OK }

/** [label] is the Step's §1.3 number, 1..5. */
data class StepCoverage(val label: Int, val seconds: Float, val status: StepStatus)

fun allCovered(coverage: List<StepCoverage>) = coverage.all { it.status == StepStatus.OK }

/**
 * SPEC 8.6, plus two fields the Summary needs so it never recomputes thresholds:
 * [modelVersion] is null in Timer-only mode, and [coverageShown] is false in Timer-only
 * mode or when the Wash is shorter than [CoverageConfig.minWashS].
 */
data class WashSession(
    val id: Long,
    val startedAtEpochMs: Long,
    val durationS: Float,
    val coverage: List<StepCoverage>, // labels 1..5, always length 5, always in §1.3 order
    val washOtherS: Float,
    val modelVersion: String?,
    val source: String, // "sensor" | "replay"
    val coverageShown: Boolean,
)

/** The only place Coverage thresholds live (SPEC-APP M4). Cumulative seconds per Step. */
data class CoverageConfig(
    val okS: Float = 4f,
    val briefS: Float = 1.5f,
    val minWashS: Float = 10f,
) {
    fun status(seconds: Float) = when {
        seconds >= okS -> StepStatus.OK
        seconds >= briefS -> StepStatus.BRIEF
        else -> StepStatus.NOT_DETECTED
    }
}

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
    private var n = 0
    private var washed = false
    private var nullRun = 0
    private var nudged = false

    /** Seconds of Wash covered by the windows seen so far. */
    val elapsedS get() = if (n == 0) 0f else windowS + (n - 1) * strideS

    fun window(smoothed: FloatArray): AutoEndAction {
        if (meta == null) return AutoEndAction.NONE
        n++
        if (smoothed.indices.maxBy { smoothed[it] } == 0) nullRun++ else { washed = true; nullRun = 0; nudged = false }
        val nullS = nullRun * strideS
        return when {
            !washed && elapsedS >= config.idleS -> AutoEndAction.CANCEL
            !washed || nullS < config.sustainedNullS -> AutoEndAction.NONE
            elapsedS >= config.targetS -> AutoEndAction.END
            !nudged -> { nudged = true; AutoEndAction.NUDGE }
            else -> AutoEndAction.NONE
        }
    }
}

/** Causal trailing mean of the last [ModelMeta.movingAverageK] vectors; averages what it has during warm-up. */
class Smoother(meta: ModelMeta) {
    private val k = meta.movingAverageK
    private val recent = ArrayDeque<FloatArray>()

    fun push(p: FloatArray): FloatArray {
        recent.addLast(p)
        if (recent.size > k) recent.removeFirst()
        return FloatArray(p.size) { i -> recent.sumOf { it[i].toDouble() }.toFloat() / recent.size }
    }
}

/**
 * Smoothed window vectors in, [WashSession] out. Each window credits one stride to its top-1
 * label: Steps get Coverage, Washing gets [WashSession.washOtherS], NULL gets nothing.
 * [meta] null = Timer-only mode.
 */
class CoverageStateMachine(private val meta: ModelMeta?, private val config: CoverageConfig = CoverageConfig()) {
    private val strideS = meta?.let { it.windowStride.toFloat() / it.sampleRateHz } ?: 0f
    private val steps = meta?.let { m -> STEPS.map { m.labels.indexOf(it).also { i -> require(i >= 0) { "meta lacks $it" } } } }
    private val washOther = meta?.labels?.indexOf(WASH_OTHER) ?: -1
    // Window counts, not summed seconds: exact at threshold edges.
    private val counts = IntArray(meta?.labels?.size ?: 0)

    fun window(smoothed: FloatArray) {
        counts[smoothed.indices.maxBy { smoothed[it] }]++
    }

    /** [durationS] comes from sample timestamps (last − first), never the wall clock. */
    fun finish(durationS: Float, startedAtEpochMs: Long, source: String): WashSession {
        val coverage = List(STEPS.size) { n ->
            val s = (steps?.let { counts[it[n]] } ?: 0) * strideS
            StepCoverage(n + 1, s, config.status(s))
        }
        return WashSession(
            id = startedAtEpochMs,
            startedAtEpochMs = startedAtEpochMs,
            durationS = durationS,
            coverage = coverage,
            washOtherS = if (washOther >= 0) counts[washOther] * strideS else 0f,
            modelVersion = meta?.modelVersion,
            source = source,
            coverageShown = meta != null && durationS >= config.minWashS,
        )
    }

    companion object {
        /** The five Steps, §1.3 order. */
        val STEPS = listOf("PALM_TO_PALM", "BACK_OF_HAND", "INTERLACED_FINGERS", "THUMBS", "FINGERTIPS")
        const val WASH_OTHER = "WASH_OTHER"
    }
}
