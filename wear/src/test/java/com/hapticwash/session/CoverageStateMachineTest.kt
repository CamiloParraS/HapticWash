package com.hapticwash.session

import com.hapticwash.inference.ModelMeta
import com.hapticwash.session.StepStatus.BRIEF
import com.hapticwash.session.StepStatus.NOT_DETECTED
import com.hapticwash.session.StepStatus.OK
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SPEC-APP M4 acceptance: state machine cases, plus the smoother (M4 slice a). */
class CoverageStateMachineTest {
    private val json = File("src/main/assets/model/model_meta.json").readText()
    private val meta = ModelMeta.parse(json) // stride 1.5 s, labels NULL, 5 Steps, WASH_OTHER

    private fun meta(strideS: Double = 1.5, k: Int = 2) = ModelMeta.parse(
        JSONObject(json).apply {
            put("window_stride_s", strideS)
            getJSONObject("postprocessing").put("moving_average_k", k)
        }.toString(),
    )

    /** One-hot vector for [label] in [ModelMeta.labels] order. */
    private fun ModelMeta.hot(label: String) = FloatArray(labels.size).also { it[labels.indexOf(label)] = 1f }

    private fun session(meta: ModelMeta?, durationS: Float, vararg windows: Pair<String, Int>): WashSession {
        val m = CoverageStateMachine(meta)
        for ((label, n) in windows) repeat(n) { m.window(meta!!.hot(label)) }
        return m.finish(durationS, startedAtEpochMs = 1000L, source = "replay")
    }

    private fun WashSession.statuses() = coverage.map { it.status }

    @Test
    fun allStepsCovered() {
        val s = session(meta, 60f, "PALM_TO_PALM" to 3, "BACK_OF_HAND" to 3, "INTERLACED_FINGERS" to 3, "THUMBS" to 3, "FINGERTIPS" to 3)
        assertEquals(List(5) { OK }, s.statuses())
        assertTrue(s.coverageShown)
        assertEquals(4.5f, s.coverage[0].seconds, 1e-6f)
    }

    @Test
    fun oneStepEntirelyMissing() {
        val s = session(meta, 60f, "PALM_TO_PALM" to 3, "BACK_OF_HAND" to 3, "THUMBS" to 3, "FINGERTIPS" to 3)
        assertEquals(listOf(OK, OK, NOT_DETECTED, OK, OK), s.statuses())
        assertEquals(0f, s.coverage[2].seconds)
    }

    @Test
    fun nonContiguousBurstsSum() {
        val s = session(meta, 30f, "THUMBS" to 2, "NULL" to 4, "PALM_TO_PALM" to 2, "THUMBS" to 1)
        assertEquals(OK, s.coverage[3].status) // 3 windows = 4.5 s
        assertEquals(4.5f, s.coverage[3].seconds, 1e-6f)
    }

    @Test
    fun fourSecondWashShowsNoCoverage() {
        val s = session(meta, 4f, "PALM_TO_PALM" to 3)
        assertFalse(s.coverageShown)
        assertEquals("0.1.0", s.modelVersion)
        assertEquals(4.5f, s.coverage[0].seconds, 1e-6f) // seconds still stored
    }

    @Test
    fun washingCountsOnlyAsWashOther() {
        // 10 windows, 4 of them Washing (40 %).
        val s = session(meta, 15f, "WASH_OTHER" to 4, "PALM_TO_PALM" to 3, "BACK_OF_HAND" to 3)
        assertEquals(6f, s.washOtherS, 1e-6f)
        assertEquals(listOf(OK, OK, NOT_DETECTED, NOT_DETECTED, NOT_DETECTED), s.statuses())
    }

    @Test
    fun emptyStream() {
        val s = session(meta, 0f)
        assertEquals((1..5).toList(), s.coverage.map { it.label })
        assertEquals(List(5) { NOT_DETECTED }, s.statuses())
        assertEquals(0f, s.washOtherS)
        assertFalse(s.coverageShown)
    }

    @Test
    fun thresholdEdges() {
        val half = meta(strideS = 0.5) // 3 windows = 1.5 s, 8 windows = 4.0 s
        val s = session(half, 30f, "PALM_TO_PALM" to 2, "BACK_OF_HAND" to 3, "INTERLACED_FINGERS" to 7, "THUMBS" to 8)
        assertEquals(listOf(NOT_DETECTED, BRIEF, BRIEF, OK, NOT_DETECTED), s.statuses())
    }

    @Test
    fun minimumDurationEdge() {
        assertTrue(session(meta, 10f).coverageShown)
        assertFalse(session(meta, 9.99f).coverageShown)
    }

    @Test
    fun coverageFollowsCanonicalOrderNotMetaOrder() {
        // Same labels, shuffled in the meta: lookup is by name, output order is fixed.
        val o = JSONObject(json)
        o.put("labels", org.json.JSONArray(listOf("FINGERTIPS", "NULL", "WASH_OTHER", "THUMBS", "BACK_OF_HAND", "PALM_TO_PALM", "INTERLACED_FINGERS")))
        val shuffled = ModelMeta.parse(o.toString())
        val s = session(shuffled, 30f, "FINGERTIPS" to 3, "WASH_OTHER" to 2)
        assertEquals((1..5).toList(), s.coverage.map { it.label })
        assertEquals(listOf(NOT_DETECTED, NOT_DETECTED, NOT_DETECTED, NOT_DETECTED, OK), s.statuses())
        assertEquals(3f, s.washOtherS, 1e-6f)
    }

    @Test
    fun timerOnlyHasDurationButNoCoverage() {
        val s = session(null, 30f)
        assertNull(s.modelVersion)
        assertFalse(s.coverageShown)
        assertEquals(30f, s.durationS)
        assertEquals(5, s.coverage.size)
        assertEquals("replay", s.source)
        assertEquals(1000L, s.startedAtEpochMs)
    }

    @Test
    fun smootherWithKOneIsIdentity() {
        val sm = Smoother(meta(k = 1))
        val p = floatArrayOf(0.1f, 0.2f, 0.7f)
        assertArrayEquals(p, sm.push(p), 0f)
        val q = floatArrayOf(0.5f, 0.5f, 0f)
        assertArrayEquals(q, sm.push(q), 0f)
    }

    @Test
    fun smootherWarmsUpThenTrails() {
        val sm = Smoother(meta(k = 3))
        assertArrayEquals(floatArrayOf(3f, 0f), sm.push(floatArrayOf(3f, 0f)), 1e-6f) // 1 vector
        assertArrayEquals(floatArrayOf(1.5f, 1.5f), sm.push(floatArrayOf(0f, 3f)), 1e-6f) // 2
        assertArrayEquals(floatArrayOf(1f, 2f), sm.push(floatArrayOf(0f, 3f)), 1e-6f) // 3
        assertArrayEquals(floatArrayOf(0f, 3f), sm.push(floatArrayOf(0f, 3f)), 1e-6f) // first dropped
    }
}
