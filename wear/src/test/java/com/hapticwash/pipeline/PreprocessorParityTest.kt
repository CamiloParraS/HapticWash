package com.hapticwash.pipeline

import com.hapticwash.Npy
import com.hapticwash.inference.ModelMeta
import com.hapticwash.sensing.MotionSample
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** SPEC 9.2 item 4: golden raw stream through the Kotlin chain reproduces Python's windows. */
class PreprocessorParityTest {
    @Test
    fun goldenRawReproducesGoldenInputs() {
        val meta = ModelMeta.parse(File("src/main/assets/model/model_meta.json").readText())
        val raw = Npy.read(File("src/androidTest/assets/golden/raw.npy").readBytes())
        val expected = Npy.read(File("src/androidTest/assets/golden/inputs.npy").readBytes())

        val pre = Preprocessor(meta)
        val d = raw.data
        val windows = (0 until raw.shape[0]).mapNotNull { t ->
            val i = t * 6
            pre.push(MotionSample(t * 20_000_000L, d[i], d[i + 1], d[i + 2], d[i + 3], d[i + 4], d[i + 5]))
        }

        assertEquals(expected.shape[0], windows.size)
        val w = expected.shape[1] * expected.shape[2]
        var maxDiff = 0f
        windows.forEachIndexed { n, win ->
            for (k in win.indices) maxDiff = maxOf(maxDiff, abs(win[k] - expected.data[n * w + k]))
        }
        println("preprocessing parity: ${windows.size} windows, max |diff| = $maxDiff")
        assertTrue("max |diff| = $maxDiff", maxDiff <= 1e-3f)
    }
}
