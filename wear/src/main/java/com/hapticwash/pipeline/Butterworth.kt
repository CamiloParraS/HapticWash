package com.hapticwash.pipeline

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Digital Butterworth design as second-order sections, following
 * `scipy.signal.butter(..., output="sos")`: analog prototype, lp2lp / lp2bp, bilinear.
 * Sections are paired differently from scipy's zpk2sos, but the cascade is the same filter;
 * the golden preprocessing test (SPEC 9.2) checks the result against Python.
 *
 * Each section is `[b0, b1, b2, a1, a2]` with `a0 = 1`.
 */
object Butterworth {
    fun lowpass(order: Int, cutoffHz: Double, fs: Double): List<DoubleArray> {
        val wo = warp(cutoffHz, fs)
        return toSos(prototype(order).map { it * wo }, emptyList(), wo.pow(order))
    }

    fun bandpass(order: Int, lowHz: Double, highHz: Double, fs: Double): List<DoubleArray> {
        val w1 = warp(lowHz, fs)
        val w2 = warp(highHz, fs)
        val bw = w2 - w1
        val poles = prototype(order).flatMap {
            val half = it * (bw / 2)
            val root = (half * half - C(w1 * w2)).sqrt()
            listOf(half + root, half - root)
        }
        return toSos(poles, List(order) { C(0.0) }, bw.pow(order))
    }

    /** Pre-warped analog frequency at scipy's internal fs = 2. */
    private fun warp(hz: Double, fs: Double) = 4 * tan(PI * (2 * hz / fs) / 2)

    private fun prototype(n: Int) = (-n + 1 until n step 2).map { m ->
        val a = PI * m / (2 * n)
        C(-cos(a), -sin(a))
    }

    /** Bilinear transform (fs = 2) of analog poles/zeros/gain, then pair into sections. */
    private fun toSos(pa: List<C>, za: List<C>, ka: Double): List<DoubleArray> {
        val four = C(4.0)
        val p = pa.map { (four + it) / (four - it) }
        val z = za.map { (four + it) / (four - it) } + List(pa.size - za.size) { C(-1.0) }
        var k = C(ka)
        za.forEach { k *= four - it }
        pa.forEach { k /= four - it }
        val pp = pairs(p)
        val zp = pairs(z)
        return pp.indices.map { i ->
            val b = poly(zp[i])
            val a = poly(pp[i])
            val g = if (i == 0) k.re else 1.0
            doubleArrayOf(b[0] * g, b[1] * g, b[2] * g, a[1], a[2])
        }
    }

    /** Complex roots with their conjugates, then reals two at a time (a lone one last). */
    private fun pairs(r: List<C>): List<List<C>> {
        val out = r.filter { it.im > EPS }.map { listOf(it, it.conj()) }.toMutableList()
        r.filter { abs(it.im) <= EPS }.map { C(it.re) }.chunked(2).forEach { out += it }
        return out
    }

    /** Monic polynomial `[1, c1, c2]` with one or two roots (real coefficients by pairing). */
    private fun poly(r: List<C>) =
        if (r.size == 1) doubleArrayOf(1.0, -r[0].re, 0.0)
        else doubleArrayOf(1.0, -(r[0] + r[1]).re, (r[0] * r[1]).re)

    private const val EPS = 1e-10

    private data class C(val re: Double, val im: Double = 0.0) {
        operator fun plus(o: C) = C(re + o.re, im + o.im)
        operator fun minus(o: C) = C(re - o.re, im - o.im)
        operator fun times(o: C) = C(re * o.re - im * o.im, re * o.im + im * o.re)
        operator fun times(d: Double) = C(re * d, im * d)
        operator fun div(o: C): C {
            val d = o.re * o.re + o.im * o.im
            return C((re * o.re + im * o.im) / d, (im * o.re - re * o.im) / d)
        }
        fun conj() = C(re, -im)
        fun sqrt(): C {
            val r = sqrt(hypot(re, im))
            val a = atan2(im, re) / 2
            return C(r * cos(a), r * sin(a))
        }
    }
}

/**
 * Causal SOS filter over [channels] channels, transposed direct form II like
 * `scipy.signal.sosfilt`, state starting at zero (SPEC 8.7). State carries across calls.
 */
class SosFilter(private val sos: List<DoubleArray>, private val channels: Int) {
    private val z = Array(sos.size) { DoubleArray(2 * channels) }

    /** Filters one sample ([channels] values) in place. */
    fun step(x: DoubleArray) {
        for (c in 0 until channels) {
            var v = x[c]
            for ((s, f) in sos.withIndex()) {
                val zs = z[s]
                val y = f[0] * v + zs[2 * c]
                zs[2 * c] = f[1] * v - f[3] * y + zs[2 * c + 1]
                zs[2 * c + 1] = f[2] * v - f[4] * y
                v = y
            }
            x[c] = v
        }
    }
}
