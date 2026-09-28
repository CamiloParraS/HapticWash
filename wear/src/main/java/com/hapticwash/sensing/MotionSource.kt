package com.hapticwash.sensing

import kotlinx.coroutines.flow.Flow

/** One paired accelerometer (m/s², with gravity) + gyroscope (rad/s) reading (SPEC 8.4). */
data class MotionSample(
    val timestampNs: Long,
    val ax: Float, val ay: Float, val az: Float,
    val gx: Float, val gy: Float, val gz: Float,
)

interface MotionSource {
    /** Cold flow. Collecting starts acquisition; cancelling stops it. */
    fun stream(): Flow<MotionSample>
    val nominalRateHz: Int
}
