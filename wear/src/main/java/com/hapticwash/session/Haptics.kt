package com.hapticwash.session

import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibrationEffect.Composition.PRIMITIVE_CLICK
import android.os.VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
import android.os.VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
import android.os.VibrationEffect.Composition.PRIMITIVE_TICK
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * §4.1.6 patterns as crisp haptic primitives (id to delay ms), and as off/on waveform timings (ms) for
 * watches without them.
 */
enum class Pulse(val primitives: List<Pair<Int, Int>>, vararg val timings: Long) {
    START(listOf(PRIMITIVE_CLICK to 0), 0, 80),
    END(listOf(PRIMITIVE_CLICK to 0, PRIMITIVE_CLICK to 120), 0, 80, 120, 80),
    /** END when all five Steps look covered: still two pulses, the second one rising. */
    SUCCESS(listOf(PRIMITIVE_CLICK to 0, PRIMITIVE_QUICK_RISE to 120), 0, 80, 120, 160),
    /** Three taps, the last one falling: a nudge, not an alarm (C4). */
    MISSED(listOf(PRIMITIVE_CLICK to 0, PRIMITIVE_CLICK to 160, PRIMITIVE_QUICK_FALL to 160), 0, 60, 160, 60, 160, 60),
    TARGET(listOf(PRIMITIVE_TICK to 0, PRIMITIVE_TICK to 80, PRIMITIVE_TICK to 80), 0, 40, 80, 40, 80, 40);

    val durationMs get() = timings.sum()
}

/** Play [pulse] [atMs] after the Wash event. */
data class Buzz(val atMs: Long, val pulse: Pulse)

/** Which vibrations a Wash event gets. Pure, so the rules are unit-tested. */
object Haptics {
    /** Minimum spacing between vibrations (§4.1.6). */
    const val GAP_MS = 3000L

    /** Time-reached target (WHO: wash for 40–60 s). In-progress counts down to it. */
    const val TARGET_MS = 40_000L

    /** The start pulse, then the time-reached ticks; a Wash that ends first cancels the ticks. */
    fun onStart(source: String, hapticsOn: Boolean) =
        if (hapticsOn && source == "sensor") listOf(Buzz(0, Pulse.START), Buzz(TARGET_MS, Pulse.TARGET)) else emptyList()

    fun onEnd(wash: WashSession, hapticsOn: Boolean): List<Buzz> {
        if (!hapticsOn || wash.source != "sensor") return emptyList()
        val allOk = wash.coverageShown && allCovered(wash.coverage)
        val end = Buzz(0, if (allOk) Pulse.SUCCESS else Pulse.END)
        val missed = wash.coverageShown && wash.coverage.any { it.status == StepStatus.NOT_DETECTED }
        return if (missed) listOf(end, Buzz(Pulse.END.durationMs + GAP_MS, Pulse.MISSED)) else listOf(end)
    }
}

/**
 * Plays [Buzz] lists. Process-wide so a pending missed-step pulse outlives the service and a
 * new Wash cancels it; the end of a Wash likewise cancels its pending time-reached ticks. If a
 * Wash ends within 3 s of the ticks, the end pulses wait for the gap. Haptics setting re-read at
 * fire time.
 */
object Buzzer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pending: Job? = null
    @Volatile private var lastAt = -Haptics.GAP_MS

    // ponytail: the delayed pulse dies with the process; a WorkManager job if that proves common.
    @Synchronized
    fun play(context: Context, buzzes: List<Buzz>) {
        pending?.cancel()
        if (buzzes.isEmpty()) return
        val app = context.applicationContext
        val vibrator = app.getSystemService(Vibrator::class.java)
        val t0 = SystemClock.elapsedRealtime()
        pending = scope.launch {
            for (b in buzzes) {
                delay(maxOf(t0 + b.atMs, lastAt + Haptics.GAP_MS) - SystemClock.elapsedRealtime())
                if (!Settings(app).haptics) return@launch
                vibrator.vibrate(effect(vibrator, b.pulse))
                lastAt = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun effect(vibrator: Vibrator, pulse: Pulse): VibrationEffect {
        val ids = pulse.primitives.map { it.first }.toIntArray()
        if (ids.isEmpty() || !vibrator.areAllPrimitivesSupported(*ids)) return VibrationEffect.createWaveform(pulse.timings, -1)
        return VibrationEffect.startComposition().apply { pulse.primitives.forEach { (id, delay) -> addPrimitive(id, 1f, delay) } }.compose()
    }
}
