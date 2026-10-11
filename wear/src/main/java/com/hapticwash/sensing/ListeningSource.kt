package com.hapticwash.sensing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.hapticwash.session.WashService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * Twist start's background listener (D32): the gyroscope alone, at a low rate, batched, so
 * [TwistDetector] sees roll (gy) with no accelerometer. Samples carry zero acceleration.
 * Prefers the wake-up gyroscope: a non-wake-up one overwrites its FIFO while the processor
 * sleeps, losing a Twist made with the screen off. Holds no wake lock.
 */
class ListeningSource(context: Context) : MotionSource {
    private val sm = context.getSystemService(SensorManager::class.java)

    override val nominalRateHz = 1_000_000 / PERIOD_US

    override fun stream(): Flow<MotionSample> = callbackFlow {
        val gyr = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE, true)
            ?: requireNotNull(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)) { "no gyroscope" }
        // ponytail: no wake-up gyroscope -> a wake-up accelerometer wakes the processor each latency and the
        // gyroscope's FIFO drains with it. Unverified on hardware; if its FIFO overflows, or ticket 06's battery
        // numbers say so, switch to D32's accelerometer trigger with the gyroscope on briefly to confirm.
        val waker = if (gyr.isWakeUpSensor) null else sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
        Log.i(WashService.TAG, "Listening: ${gyr.name} wakeUp=${gyr.isWakeUpSensor} fifo=${gyr.fifoMaxEventCount}, waker=${waker?.name}")
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_GYROSCOPE) trySend(MotionSample(e.timestamp, 0f, 0f, 0f, e.values[0], e.values[1], e.values[2]))
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sm.registerListener(listener, gyr, PERIOD_US, LATENCY_US)
        // Its samples are thrown away: only its flushes matter, so its slowest rate.
        waker?.let { sm.registerListener(listener, it, maxOf(it.maxDelay, PERIOD_US), LATENCY_US) }
        awaitClose { sm.unregisterListener(listener) }
    }.buffer(Channel.UNLIMITED) // a batch arrives at once; keep all of it

    private companion object {
        /**
         * Calibration knobs (D32), set from the ticket 04 probe. 25 Hz: half the Wash rate, inside the
         * emulator's 2-100 Hz, and a 0.25 s flick still gives ~6 samples (TwistDetectorTest). 1 s latency:
         * a Twist is seen up to 1 s late, 25 events per flush, which needs a FIFO of at least 25.
         */
        const val PERIOD_US = 40_000
        const val LATENCY_US = 1_000_000
    }
}
