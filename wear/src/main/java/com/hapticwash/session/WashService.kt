package com.hapticwash.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.PowerManager
import android.util.Log
import com.hapticwash.R
import com.hapticwash.inference.StepModel
import com.hapticwash.pipeline.Preprocessor
import com.hapticwash.sensing.MotionSource
import com.hapticwash.sensing.RealSensorSource
import com.hapticwash.sensing.ReplaySource
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the UI shows. M3: running or not, plus the raw top-1 label of the latest window. */
data class WashStatus(
    val running: Boolean = false,
    val label: String? = null,
    val windows: Int = 0,
    val timerOnly: Boolean = false,
)

/**
 * One wash session as a foreground service (type dataSync): motion source -> preprocessor ->
 * step model, one prediction per window. Sensors run only while a session runs (§4.1 item 8);
 * a partial wake lock keeps them delivering with the screen off.
 */
class WashService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            job?.cancel() ?: stopSelf()
            return START_NOT_STICKY
        }
        startForeground(1, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        if (job?.isActive == true) return START_NOT_STICKY
        val replay = intent?.getStringExtra(EXTRA_REPLAY_PATH)
        val speed = intent?.getFloatExtra(EXTRA_SPEED, 1f) ?: 1f
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HapticWash:session")
            .apply { acquire(MAX_SESSION_MS) }
        job = scope.launch {
            try {
                if (replay != null) run(ReplaySource(File(replay), speed), "replay") else run(RealSensorSource(this@WashService), "sensor")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Session failed", e)
            } finally {
                wakeLock?.takeIf { it.isHeld }?.release()
                _status.value = WashStatus()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun run(source: MotionSource, kind: String) {
        val model = StepModel.bundled(this)
        val pre = model?.let {
            require(source.nominalRateHz == it.meta.sampleRateHz) { "source rate != model rate" }
            Preprocessor(it.meta)
        }
        _status.value = WashStatus(running = true, timerOnly = model == null)
        Log.i(TAG, "Session started: source=$kind model=${model?.meta?.modelVersion ?: "none (timer-only)"}")
        val latencyNs = ArrayList<Long>()
        var first = -1L
        var last = -1L
        try {
            source.stream().collect { s ->
                if (first < 0) first = s.timestampNs
                last = s.timestampNs
                val w = pre?.push(s) ?: return@collect
                val m = model ?: return@collect
                val t = System.nanoTime()
                val p = m.predict(w)
                latencyNs += System.nanoTime() - t
                val top = p.indices.maxBy { p[it] }
                val label = m.meta.labels[top]
                Log.i(TAG, "window %d t=%.1fs %s p=%.2f".format(latencyNs.size, (last - first) / 1e9, label, p[top]))
                _status.update { it.copy(label = label, windows = latencyNs.size) }
            }
        } finally {
            val sorted = latencyNs.sorted()
            fun pct(q: Double) = if (sorted.isEmpty()) 0.0 else sorted[(q * sorted.size).toInt().coerceAtMost(sorted.size - 1)] / 1e6
            Log.i(
                TAG,
                "Session ended: %.1f s, %d windows, inference median %.2f ms, p95 %.2f ms"
                    .format((last - first).coerceAtLeast(0) / 1e9, sorted.size, pct(0.5), pct(0.95)),
            )
        }
    }

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.channel_session), NotificationManager.IMPORTANCE_LOW),
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.splash_icon)
            .setContentTitle(getString(R.string.session_running))
            .setOngoing(true)
            .build()
    }

    companion object {
        const val TAG = "HapticWash"
        private const val ACTION_STOP = "com.hapticwash.STOP"
        private const val EXTRA_REPLAY_PATH = "replay_path"
        private const val EXTRA_SPEED = "speed"
        private const val CHANNEL = "session"
        // ponytail: wake lock capped at 10 min so a stuck session cannot hold it forever; longer
        // sessions run on without it. Revisit if D12 (is it needed at all?) says keep it.
        private const val MAX_SESSION_MS = 10 * 60 * 1000L

        private val _status = MutableStateFlow(WashStatus())
        val status: StateFlow<WashStatus> = _status

        fun start(context: Context) =
            context.startForegroundService(Intent(context, WashService::class.java))

        fun replay(context: Context, path: String, speed: Float) = context.startForegroundService(
            Intent(context, WashService::class.java).putExtra(EXTRA_REPLAY_PATH, path).putExtra(EXTRA_SPEED, speed),
        )

        fun stop(context: Context) =
            context.startService(Intent(context, WashService::class.java).setAction(ACTION_STOP))
    }
}
