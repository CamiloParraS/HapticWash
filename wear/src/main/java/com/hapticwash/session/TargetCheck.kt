package com.hapticwash.session

/**
 * The Target check's clock (SPEC-APP Coaching): fires once, at the 40 s target, which a keep-going nudge
 * moves the way it moves the ticks. Times are ms since the Wash started.
 */
class TargetCheck {
    private var dueMs = Haptics.TARGET_MS
    private var fired = false

    /** The nudge came [nowMs] into the Wash with [elapsedMs] of washing seen: the target is what is left. */
    fun nudge(nowMs: Long, elapsedMs: Long) {
        dueMs = nowMs + (Haptics.TARGET_MS - elapsedMs).coerceAtLeast(0)
    }

    /** Null until due, and after firing. Then the labels of the NOT_DETECTED Steps; empty means no card, plain ticks. */
    fun poll(nowMs: Long, coverage: List<StepCoverage>): List<Int>? {
        if (fired || nowMs < dueMs) return null
        fired = true
        return coverage.filter { it.status == StepStatus.NOT_DETECTED }.map { it.label }
    }
}
