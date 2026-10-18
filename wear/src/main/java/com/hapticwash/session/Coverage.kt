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

/** A Wash shorter than [CoverageConfig.minWashS] is an accidental start or a test: shown once, never kept in History. */
fun WashSession.worthSaving(config: CoverageConfig = CoverageConfig()) = durationS >= config.minWashS

/** The only place Coverage thresholds live (SPEC-APP M4). Cumulative seconds per Step. */
data class CoverageConfig(
    val okS: Float = 4f,
    val briefS: Float = 1.5f,
    val minWashS: Float = 10f,
    // Focus Step (SPEC-APP Coaching): both rates are calibration knobs.
    val focusWindow: Int = 10,
    val focusMinWashes: Int = 5,
    val focusMissedRate: Float = 0.6f,
    val focusNotDetectedRate: Float = 0.3f,
) {
    fun status(seconds: Float) = when {
        seconds >= okS -> StepStatus.OK
        seconds >= briefS -> StepStatus.BRIEF
        else -> StepStatus.NOT_DETECTED
    }
}

/**
 * The Step the user habitually leaves undetected, as its label, or null. [washes] newest first. Looks at the last
 * [CoverageConfig.focusWindow] `sensor` Washes that show Coverage; no stored state.
 */
fun focusStep(washes: List<WashSession>, config: CoverageConfig = CoverageConfig()): Int? {
    val window = focusWindow(washes, config)
    if (window.size < config.focusMinWashes) return null
    fun rate(label: Int, vararg statuses: StepStatus) = window.count { w -> w.coverage[label - 1].status in statuses }.toFloat() / window.size
    return (1..5).filter {
        rate(it, StepStatus.NOT_DETECTED, StepStatus.BRIEF) >= config.focusMissedRate &&
            rate(it, StepStatus.NOT_DETECTED) >= config.focusNotDetectedRate
    } // Highest not-detected rate wins; a tie goes to the later Step (larger label).
        .maxWithOrNull(compareBy({ rate(it, StepStatus.NOT_DETECTED) }, { it }))
}

private fun focusWindow(washes: List<WashSession>, config: CoverageConfig) =
    washes.filter { it.source == "sensor" && it.coverageShown }.take(config.focusWindow)

/** What backs the Focus Step: in how many (first) of the last (second) qualifying Washes [label] was NOT_DETECTED. */
fun focusCounts(washes: List<WashSession>, label: Int, config: CoverageConfig = CoverageConfig()): Pair<Int, Int> {
    val window = focusWindow(washes, config)
    return window.count { it.coverage[label - 1].status == StepStatus.NOT_DETECTED } to window.size
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

    /** Coverage so far, in §1.3 order. */
    fun snapshot() = List(STEPS.size) { n ->
        val s = (steps?.let { counts[it[n]] } ?: 0) * strideS
        StepCoverage(n + 1, s, config.status(s))
    }

    /** [durationS] comes from sample timestamps (last − first), never the wall clock. */
    fun finish(durationS: Float, startedAtEpochMs: Long, source: String): WashSession {
        val coverage = snapshot()
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
