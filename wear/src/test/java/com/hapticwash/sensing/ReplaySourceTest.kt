package com.hapticwash.sensing

import java.io.File
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplaySourceTest {
    private val header = "timestamp_ns,ax,ay,az,gx,gy,gz,label,subject_id,session_id,wrist,source"

    private fun csv(vararg rows: String) =
        File.createTempFile("replay", ".csv").apply { writeText((listOf(header) + rows).joinToString("\n") + "\n"); deleteOnExit() }

    private fun rows(n: Int, stepNs: Long = 20_000_000) =
        Array(n) { "${it * stepNs},1,2,3,4,5,6,0,zhang_who_sub01,s1,left,zhang_who" }

    @Test
    fun emitsSamplesInFileOrderAtTheOriginalPace() = runBlocking {
        // 26 samples at 20 ms = 0.5 s of data; at 5x it should take about 100 ms.
        val src = ReplaySource(csv(*rows(26)), speed = 5f)
        val t0 = System.nanoTime()
        val out = src.stream().toList()
        val ms = (System.nanoTime() - t0) / 1e6

        assertEquals(26, out.size)
        assertEquals(MotionSample(40_000_000, 1f, 2f, 3f, 4f, 5f, 6f), out[2])
        assertTrue("took $ms ms", ms in 90.0..400.0)
    }

    @Test
    fun rejectsFilesThatAreNotCanonical() {
        val bad = listOf(
            File.createTempFile("bad", ".csv").apply { writeText("t,ax,ay,az,gx,gy,gz\n0,1,2,3,4,5,6\n") },
            csv("0,1,2,3,4,5,6,0,zhang_who_sub01,s1,left"), // missing column
            csv("0,1,2,x,4,5,6,0,zhang_who_sub01,s1,left,zhang_who"), // not a number
            csv(*rows(2).reversedArray()), // time goes backwards
        )
        for (f in bad) assertThrows(IllegalArgumentException::class.java) { runBlocking { ReplaySource(f, 100f).stream().toList() } }
    }
}
