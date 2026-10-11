package com.hapticwash.session

import com.hapticwash.inference.ModelMeta
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

/** SPEC-APP M5 Auto-end: what a stream of smoothed windows makes the Wash do. */
class AutoEndTest {
    private val meta = ModelMeta.parse(File("src/main/assets/model/model_meta.json").readText()) // stride 1.5 s
    private fun hot(label: String) = FloatArray(meta.labels.size).also { it[meta.labels.indexOf(label)] = 1f }

    /** Non-NONE actions for the windows in order, stopping where the Wash would end. */
    private fun run(autoEnd: AutoEnd, vararg legs: Pair<String, Int>): List<AutoEndAction> {
        val out = mutableListOf<AutoEndAction>()
        for ((label, n) in legs) repeat(n) {
            val a = autoEnd.window(hot(label))
            if (a != AutoEndAction.NONE) out += a
            if (a == AutoEndAction.CANCEL || a == AutoEndAction.END) return out
        }
        return out
    }

    @Test
    fun allNullFirst10sCancels() {
        assertEquals(listOf(AutoEndAction.CANCEL), run(AutoEnd(meta), "NULL" to 20))
    }

    @Test
    fun washingThenNullGapBeforeTargetNudgesOnceAndContinues() {
        // 4 windows of washing, 10 NULL windows (15 s): still short of 40 s.
        assertEquals(listOf(AutoEndAction.NUDGE), run(AutoEnd(meta), "THUMBS" to 4, "NULL" to 10))
    }

    @Test
    fun washingPastTargetThenSustainedNullEnds() {
        // 30 windows is about 45 s of washing, then NULL.
        assertEquals(listOf(AutoEndAction.END), run(AutoEnd(meta), "WASH_OTHER" to 30, "NULL" to 10))
    }

    @Test
    fun briefNullBlipPastTargetDoesNotStop() {
        assertEquals(emptyList<AutoEndAction>(), run(AutoEnd(meta), "WASH_OTHER" to 30, "NULL" to 2, "WASH_OTHER" to 5, "NULL" to 2))
    }

    @Test
    fun doesNotStopAtExactlyTheTargetWhileStillWashing() {
        assertEquals(emptyList<AutoEndAction>(), run(AutoEnd(meta), "WASH_OTHER" to 40))
    }

    @Test
    fun timerOnlyNeverActs() {
        assertEquals(emptyList<AutoEndAction>(), run(AutoEnd(null), "NULL" to 50))
    }
}
