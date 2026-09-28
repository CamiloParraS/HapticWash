package com.hapticwash.pipeline

import com.hapticwash.inference.ModelMeta
import com.hapticwash.sensing.MotionSample
import kotlin.math.sqrt

/**
 * Streaming mirror of `haptic_ai.preprocess` + `windows` (SPEC 8.7): gravity-align, bandpass,
 * window, z-score; all causal, filter state from zero at session start. Feed one sample at a
 * time; a z-scored window (`W x 6`, row-major) comes back every stride once the first `W`
 * samples are in. One instance per session.
 *
 * ponytail: assumes samples arrive at meta's rate; no on-device resampling. Add it if M6
 * shows the real sensor rate drifting from 50 Hz.
 */
class Preprocessor(private val meta: ModelMeta) {
    private val fs = meta.sampleRateHz.toDouble()
    private val gravity = SosFilter(Butterworth.lowpass(2, meta.gravityLowpassHz, fs), 3)
    private val band = SosFilter(Butterworth.bandpass(meta.bandOrder, meta.bandLowHz, meta.bandHighHz, fs), C)
    private val w = meta.windowSize
    private val ring = FloatArray(w * C)
    private var count = 0L
    private val g = DoubleArray(3)
    private val x = DoubleArray(C)

    fun push(s: MotionSample): FloatArray? {
        x[0] = s.ax.toDouble(); x[1] = s.ay.toDouble(); x[2] = s.az.toDouble()
        x[3] = s.gx.toDouble(); x[4] = s.gy.toDouble(); x[5] = s.gz.toDouble()
        if (meta.gravityAlign) {
            g[0] = x[0]; g[1] = x[1]; g[2] = x[2]
            gravity.step(g)
            align(g, x)
        }
        band.step(x)
        val row = (count % w).toInt() * C
        for (c in 0 until C) ring[row + c] = x[c].toFloat()
        count++
        if (count < w || (count - w) % meta.windowStride != 0L) return null
        val oldest = (count % w).toInt()
        return FloatArray(w * C) { i ->
            val c = i % C
            (ring[((oldest + i / C) % w) * C + c] - meta.normMean[c]) / meta.normStd[c]
        }
    }

    companion object {
        private val C = ModelMeta.CHANNELS.size

        /**
         * Rotates acc (`x[0..2]`) and gyro (`x[3..5]`) by the minimal rotation taking gravity
         * [g] to +z: Rodrigues, R = I + [v]x + [v]x² / (1 + c), v = u x z, c = u · z. Gravity
         * exactly on -z uses 180° about x, as the Python side does.
         */
        private fun align(g: DoubleArray, x: DoubleArray) {
            val n = sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2])
            val ok = n > 1e-9
            val vx = if (ok) g[1] / n else 0.0 // v = u x z = (uy, -ux, 0)
            val vy = if (ok) -g[0] / n else 0.0
            val c = if (ok) g[2] / n else 1.0
            val r = if (c < -1 + 1e-9) {
                doubleArrayOf(1.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, -1.0)
            } else {
                val k = 1 / (1 + c)
                doubleArrayOf(
                    1 - k * vy * vy, k * vx * vy, vy,
                    k * vx * vy, 1 - k * vx * vx, -vx,
                    -vy, vx, 1 - k * (vx * vx + vy * vy),
                )
            }
            for (o in intArrayOf(0, 3)) {
                val a = x[o]
                val b = x[o + 1]
                val d = x[o + 2]
                x[o] = r[0] * a + r[1] * b + r[2] * d
                x[o + 1] = r[3] * a + r[4] * b + r[5] * d
                x[o + 2] = r[6] * a + r[7] * b + r[8] * d
            }
        }
    }
}
