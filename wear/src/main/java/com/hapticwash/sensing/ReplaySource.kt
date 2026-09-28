package com.hapticwash.sensing

import java.io.File
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Replays a canonical sensor CSV (SPEC 8.1) with its original inter-sample timing divided by
 * [speed] (SPEC 8.4). A file that is not canonical fails the flow with
 * [IllegalArgumentException]; it is never coerced.
 */
class ReplaySource(private val file: File, private val speed: Float = 1f) : MotionSource {
    init {
        require(speed > 0f) { "speed must be > 0" }
    }

    override val nominalRateHz = 50

    override fun stream(): Flow<MotionSample> = flow {
        file.bufferedReader().useLines { lines ->
            val it = lines.iterator()
            require(it.hasNext() && it.next().trim() == HEADER) { "not a canonical CSV: bad header" }
            val start = TimeSource.Monotonic.markNow()
            var t0 = -1L
            var last = -1L
            for ((n, line) in it.withIndex()) {
                if (line.isBlank()) continue
                val f = line.split(',')
                require(f.size == COLUMNS) { "row ${n + 2}: expected $COLUMNS fields" }
                val v = try {
                    FloatArray(6) { i -> f[i + 1].toFloat() }
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("row ${n + 2}: ${e.message}")
                }
                val ts = f[0].toLongOrNull() ?: throw IllegalArgumentException("row ${n + 2}: bad timestamp")
                require(ts > last) { "row ${n + 2}: timestamps must increase" }
                last = ts
                if (t0 < 0) t0 = ts
                delay(((ts - t0) / speed).toLong().nanoseconds - start.elapsedNow())
                emit(MotionSample(ts, v[0], v[1], v[2], v[3], v[4], v[5]))
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val HEADER = "timestamp_ns,ax,ay,az,gx,gy,gz,label,subject_id,session_id,wrist,source"
        private const val COLUMNS = 12
    }
}
