package com.hapticwash.session

import com.hapticwash.inference.StepClassifier
import com.hapticwash.pipeline.Preprocessor
import com.hapticwash.sensing.MotionSource

/** How a Wash ended. Each carries the vibrations its ending gets, for the caller to play once it has stored the Wash. */
sealed interface WashOutcome {
    val durationS: Float
    val cues: List<Buzz>

    /** A Wash with its Coverage (or Timer-only): stopped by the user, by Auto-end, or by the end of a replay. */
    data class Ended(val wash: WashSession, override val cues: List<Buzz>) : WashOutcome {
        override val durationS get() = wash.durationS
    }

    /** Auto-end saw no washing in time: the Wash never began, and is saved nowhere. */
    data class Cancelled(override val durationS: Float, override val cues: List<Buzz>) : WashOutcome
}

/**
 * One Wash, from its first sample to its [WashOutcome] (spec §8.6): motion -> preprocessor -> classifier -> smoother,
 * then each window to Coverage, the Target check and Auto-end, deciding every vibration on the way. Pure: no Android,
 * so a test drives it with a scripted [MotionSource] and [StepClassifier].
 *
 * [model] null is Timer-only mode. [kind] is the Wash's Source (`sensor` or `replay`). [targetCheck] and [wrist] are
 * read once, at Wash start; [hapticsOn] is asked at each vibration. [nowMs] is a monotonic clock in ms.
 *
 * Use: construct (fails if [source] and [model] disagree on sample rate), [play], then [finish] whatever ended
 * [play]: a normal return, cancellation (the user's Stop), or a failure.
 */
class WashRun(
    private val source: MotionSource,
    private val model: StepClassifier?,
    private val kind: String,
    wrist: Wrist,
    targetCheck: Boolean,
    private val hapticsOn: () -> Boolean,
    private val startedAtEpochMs: Long,
    private val nowMs: () -> Long,
    private val out: Out = object : Out {},
) {
    /** What a running Wash tells its host, as it happens. */
    interface Out {
        /** Vibrations to play now, replacing any still pending. */
        fun cues(buzzes: List<Buzz>) {}

        /** The Target check found these Steps (labels 1..5) not detected: show the card. */
        fun targetCheck(undetected: List<Int>) {}

        fun log(message: String) {}
    }

    private val pre = model?.let {
        require(source.nominalRateHz == it.meta.sampleRateHz) { "source rate != model rate" }
        Preprocessor(it.meta, mirror = wrist == Wrist.RIGHT)
    }
    private val smoother = model?.let { Smoother(it.meta) }
    private val coverage = CoverageStateMachine(model?.meta)
    private val autoEnd = AutoEnd(model?.meta)

    // Timer-only Washes have no Coverage, so no check: they keep the plain ticks.
    private val check = if (model != null && targetCheck) TargetCheck() else null
    private var cancelled = false
    private var windows = 0
    private var startMs = 0L
    private var first = -1L
    private var last = -1L

    /** Runs until the motion runs out or Auto-end ends the Wash; cancelling it is the user's Stop. */
    suspend fun play() {
        startMs = nowMs()
        out.cues(Haptics.onStart(kind, hapticsOn(), checkOn = check != null))
        try {
            source.stream().collect { s ->
                if (first < 0) first = s.timestampNs
                last = s.timestampNs
                val w = pre?.push(s) ?: return@collect
                val m = model ?: return@collect
                window(m, m.predict(w))
            }
        } catch (_: AutoEnded) {
            // Auto-end: the Wash ends like any other, in finish().
        }
    }

    private fun window(m: StepClassifier, p: FloatArray) {
        windows++
        val sm = smoother!!.push(p)
        coverage.window(sm)
        check?.poll(nowMs() - startMs, coverage.snapshot())?.let { undetected ->
            if (undetected.isNotEmpty()) out.targetCheck(undetected)
            out.cues(Haptics.onTarget(kind, hapticsOn(), undetected))
        }
        val top = p.indices.maxBy { p[it] }
        out.log("window %d t=%.1fs %s p=%.2f".format(windows, (last - first) / 1e9, m.meta.labels[top], p[top]))
        when (autoEnd.window(sm)) {
            AutoEndAction.NUDGE -> {
                val elapsedMs = (autoEnd.elapsedS * 1000).toLong()
                val atMs = nowMs() - startMs
                // Logged for every Wash with the check on, so post-nudge windows stay separable.
                if (check != null) { check.nudge(atMs, elapsedMs); out.log("Target check: keep-going nudge at $atMs ms") }
                out.cues(Haptics.onNudge(kind, hapticsOn(), elapsedMs, checkOn = check != null))
            }
            AutoEndAction.CANCEL -> { cancelled = true; throw AutoEnded() }
            AutoEndAction.END -> throw AutoEnded()
            AutoEndAction.NONE -> {}
        }
    }

    /** The outcome of what [play] saw. Duration comes from sample timestamps, never the wall clock. */
    fun finish(): WashOutcome {
        val durationS = (last - first).coerceAtLeast(0) / 1e9f
        if (cancelled) return WashOutcome.Cancelled(durationS, Haptics.onCancel(kind, hapticsOn()))
        val wash = coverage.finish(durationS, startedAtEpochMs, kind)
        return WashOutcome.Ended(wash, Haptics.onEnd(wash, hapticsOn()))
    }

    /** Thrown out of the collector to end the Wash on Auto-end. */
    private class AutoEnded : Exception()
}
