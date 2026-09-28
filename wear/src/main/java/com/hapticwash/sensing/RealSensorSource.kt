package com.hapticwash.sensing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * Accelerometer + gyroscope at 20 ms (SPEC 8.4). Each reading is paired with the other
 * sensor's latest one if they are within half a period; unpaired readings are dropped.
 */
class RealSensorSource(context: Context) : MotionSource {
    private val sm = context.getSystemService(SensorManager::class.java)

    override val nominalRateHz = 50

    override fun stream(): Flow<MotionSample> = callbackFlow {
        val acc = requireNotNull(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) { "no accelerometer" }
        val gyr = requireNotNull(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)) { "no gyroscope" }
        var a: Reading? = null
        var g: Reading? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                // The framework reuses SensorEvent, so copy the values out.
                val r = Reading(e.timestamp, e.values[0], e.values[1], e.values[2])
                if (e.sensor.type == Sensor.TYPE_ACCELEROMETER) a = r else g = r
                val pa = a ?: return
                val pg = g ?: return
                if (abs(pa.ts - pg.ts) <= PAIR_WINDOW_NS) {
                    trySend(MotionSample(pa.ts, pa.x, pa.y, pa.z, pg.x, pg.y, pg.z))
                    a = null
                    g = null
                } else if (pa.ts < pg.ts) a = null else g = null
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sm.registerListener(listener, acc, PERIOD_US)
        sm.registerListener(listener, gyr, PERIOD_US)
        awaitClose { sm.unregisterListener(listener) }
    }.buffer(Channel.UNLIMITED) // never drop samples: a gap would shift every later window

    private class Reading(val ts: Long, val x: Float, val y: Float, val z: Float)

    private companion object {
        const val PERIOD_US = 20_000
        const val PAIR_WINDOW_NS = PERIOD_US * 1_000L / 2
    }
}
