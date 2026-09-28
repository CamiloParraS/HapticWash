package com.hapticwash.inference

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hapticwash.Npy
import kotlin.math.abs
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SPEC 9.2 items 1-3: the bundled model on the device's LiteRT runtime matches the Python
 * TFLite outputs on the golden windows. Also logs inference latency (SPEC 4.3, M3).
 */
@RunWith(AndroidJUnit4::class)
class GoldenParityTest {
    @Test
    fun deviceOutputsMatchPythonOutputs() {
        val inst = InstrumentationRegistry.getInstrumentation()
        val model = StepModel.bundled(inst.targetContext)
        assertNotNull("bundled model failed to load", model)
        val assets = inst.context.assets
        val inputs = Npy.read(assets.open("golden/inputs.npy").use { it.readBytes() })
        val outputs = Npy.read(assets.open("golden/outputs.npy").use { it.readBytes() })
        val n = inputs.shape[0]
        val w = inputs.shape[1] * inputs.shape[2]
        val k = outputs.shape[1]
        assertTrue("SPEC 9.2 needs >= 50 golden windows, got $n", n >= 50)

        var maxDiff = 0f
        var top1 = 0
        val latencyNs = LongArray(n)
        for (i in 0 until n) {
            val t = System.nanoTime()
            val p = model!!.predict(inputs.data.copyOfRange(i * w, (i + 1) * w))
            latencyNs[i] = System.nanoTime() - t
            val expected = outputs.data.copyOfRange(i * k, (i + 1) * k)
            for (c in 0 until k) maxDiff = maxOf(maxDiff, abs(p[c] - expected[c]))
            if (p.indices.maxBy { p[it] } == expected.indices.maxBy { expected[it] }) top1++
        }
        latencyNs.sort()
        Log.i(
            "HapticWash",
            "Golden parity: $n windows, max |dp| = $maxDiff, top-1 = $top1/$n, inference median " +
                "${latencyNs[n / 2] / 1e6} ms, p95 ${latencyNs[(n * 95) / 100] / 1e6} ms",
        )
        assertTrue("max |dp| = $maxDiff", maxDiff <= 1e-2f)
        assertTrue("top-1 agreement $top1/$n", top1 >= 0.98 * n)
    }
}
