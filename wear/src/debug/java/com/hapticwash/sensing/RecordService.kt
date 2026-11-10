package com.hapticwash.sensing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import com.hapticwash.R
import java.io.BufferedWriter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * Debug-only raw recorder for M6 (`own_watch`). Writes the same paired 50 Hz stream a Wash
 * feeds the model ([RealSensorSource]) as a canonical CSV (SPEC 8.1), so a recording replays
 * through `scripts/replay_demo.sh` unchanged. Driven from `scripts/watch_record.sh`:
 *
 *   am start-foreground-service -n com.hapticwash/.sensing.RecordService -a com.hapticwash.RECORD_START
 *     [--es session s01] [--es subject own_watch_1] [--es wrist left|right] [--es label NULL]
 *   ... -a com.hapticwash.RECORD_MARK --es label PALM_TO_PALM   (or a label id, or -1 = unlabelled)
 *   ... -a com.hapticwash.RECORD_STOP
 *
 * Files land in `files/recordings/`: `<session>.csv`, `<session>.marks.csv` (every mark, for
 * relabelling from video later) and `<session>.stats.json` (what the hardware actually delivered:
 * per-sensor rate, inter-sample gaps, pairing drops, sensor specs). A row's label is the last mark
 * at or before its timestamp. Start buzzes once (a sync cue for a video); each mark ticks.
 * Runs alongside a normal Wash: both register the sensors independently. Every sensor Wash in a debug build
 * also starts and stops one itself (`auto`, `wash_<start>`); see `WashService.debugRecord`.
 */
class RecordService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var rec: Recording? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification("Recording"), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        when (intent?.action) {
            ACTION_START -> if (rec == null) start(intent) else Log.w(TAG, "Record: already recording ${rec?.session}")
            ACTION_MARK -> rec?.mark(intent.getStringExtra("label")) ?: Log.w(TAG, "Record: MARK while not recording")
            // A Wash ending stops only the recording it started, never one started by hand from the script.
            ACTION_STOP -> if (!intent.getBooleanExtra("auto", false) || rec?.auto != false) stop()
            else -> if (rec == null) stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stop()
        scope.cancel()
    }

    private fun start(intent: Intent) {
        val session = intent.getStringExtra("session") ?: SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val subject = intent.getStringExtra("subject") ?: "own_watch_1"
        require(subject.startsWith("own_watch_")) { "subject must be own_watch_<id> (SPEC 8.1)" }
        val wrist = intent.getStringExtra("wrist")?.takeIf { it in setOf("left", "right", "unknown") } ?: "unknown"
        val dir = File(filesDir, "recordings").apply { mkdirs() }
        rec = Recording(session, subject, wrist, dir, auto = intent.getBooleanExtra("auto", false)).also {
            it.begin()
            it.mark(intent.getStringExtra("label") ?: "-1")
        }
        buzz(START_BUZZ_MS)
    }

    private fun stop() {
        rec?.finish()
        rec = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private inner class Recording(val session: String, val subject: String, val wrist: String, dir: File, val auto: Boolean) {
        private val sm = getSystemService(SensorManager::class.java)
        private val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val gyr = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        private val csvFile = File(dir, "$session.csv")
        private val marksFile = File(dir, "$session.marks.csv")
        private val statsFile = File(dir, "$session.stats.json")
        private val wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HapticWash:record")

        /** (boot ns, label) in arrival order; a row takes the last one at or before its timestamp. */
        private val marks = ArrayList<Pair<Long, Int>>()
        private var t0 = -1L
        private var job: Job? = null
        private lateinit var csv: BufferedWriter
        private val raw = mapOf(Sensor.TYPE_ACCELEROMETER to RawStats(), Sensor.TYPE_GYROSCOPE to RawStats())
        private val paired = RawStats()
        private val startedMs = SystemClock.elapsedRealtime()

        private val counter = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                synchronized(raw) { raw[e.sensor.type]?.add(e.timestamp) }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        fun begin() {
            wakeLock.acquire(MAX_RECORD_MS)
            csv = csvFile.bufferedWriter().apply { write(ReplaySource.HEADER); write("\n") }
            marksFile.writeText("timestamp_ns,boot_ns,label\n")
            // Counts what each sensor really delivers; the CSV rows come from the app's own pairing.
            sm.registerListener(counter, acc, PERIOD_US)
            sm.registerListener(counter, gyr, PERIOD_US)
            job = scope.launch {
                RealSensorSource(this@RecordService).stream().collect { s ->
                    if (t0 < 0) t0 = s.timestampNs
                    synchronized(paired) { paired.add(s.timestampNs) }
                    csv.write("${s.timestampNs - t0},${s.ax},${s.ay},${s.az},${s.gx},${s.gy},${s.gz},${labelAt(s.timestampNs)},$subject,$session,$wrist,own_watch\n")
                }
            }
            scope.launch {
                while (isActive && job?.isActive != false) {
                    delay(LOG_EVERY_MS)
                    Log.i(TAG, "Record $session: ${rateLine()}")
                }
            }
            Log.i(TAG, "Record started: $session subject=$subject wrist=$wrist -> ${csvFile.path}")
        }

        fun mark(label: String?) {
            val id = parseLabel(label) ?: run { Log.w(TAG, "Record: unknown label '$label'"); return }
            val now = SystemClock.elapsedRealtimeNanos()
            synchronized(marks) { marks += now to id }
            if (marks.size > 1) buzz(MARK_BUZZ_MS)
            marksFile.appendText("${if (t0 < 0) 0 else now - t0},$now,$id\n")
            Log.i(TAG, "Record mark: ${LABELS.getOrElse(id) { "UNLABELLED" }} ($id)")
        }

        private fun labelAt(ts: Long): Int = synchronized(marks) { marks.lastOrNull { it.first <= ts }?.second ?: marks.firstOrNull()?.second ?: -1 }

        private fun rateLine(): String {
            val a = synchronized(raw) { raw.getValue(Sensor.TYPE_ACCELEROMETER).hz() }
            val g = synchronized(raw) { raw.getValue(Sensor.TYPE_GYROSCOPE).hz() }
            val p = synchronized(paired) { paired.hz() }
            return "acc %.1f Hz, gyro %.1f Hz, paired %.1f Hz (%d rows)".format(a, g, p, paired.n)
        }

        fun finish() {
            sm.unregisterListener(counter)
            job?.cancel()
            runBlocking { job?.join() } // the last rows are written before the file closes
            csv.close()
            wakeLock.takeIf { it.isHeld }?.release()
            statsFile.writeText(stats().toString(2))
            Log.i(TAG, "Record stopped: $session, ${rateLine()} -> ${statsFile.path}")
        }

        private fun stats() = JSONObject().apply {
            put("session", session)
            put("subject_id", subject)
            put("wrist", wrist)
            put("wall_s", (SystemClock.elapsedRealtime() - startedMs) / 1000.0)
            put("requested_period_us", PERIOD_US)
            put("paired", paired.json())
            put("accelerometer", raw.getValue(Sensor.TYPE_ACCELEROMETER).json().put("sensor", acc?.json() ?: JSONObject.NULL))
            put("gyroscope", raw.getValue(Sensor.TYPE_GYROSCOPE).json().put("sensor", gyr?.json() ?: JSONObject.NULL))
            val acceptedRatio = paired.n.toDouble() / maxOf(1, minOf(raw.getValue(Sensor.TYPE_ACCELEROMETER).n, raw.getValue(Sensor.TYPE_GYROSCOPE).n))
            put("paired_over_slower_sensor", acceptedRatio)
            put("device", JSONObject().put("model", android.os.Build.MODEL).put("manufacturer", android.os.Build.MANUFACTURER).put("sdk", android.os.Build.VERSION.SDK_INT))
            put("wake_up_variants", JSONObject()
                .put("accelerometer", sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)?.json() ?: JSONObject.NULL)
                .put("gyroscope", sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE, true)?.json() ?: JSONObject.NULL))
        }
    }

    /** Timestamps of one stream: rate and the inter-sample gap distribution (ms). */
    private class RawStats {
        var n = 0
        private var first = 0L
        private var last = 0L
        private val gaps = ArrayList<Long>()

        fun add(ts: Long) {
            if (n == 0) first = ts else gaps += ts - last
            last = ts
            n++
        }

        fun hz() = if (n < 2) 0.0 else (n - 1) * 1e9 / (last - first)

        fun json(): JSONObject {
            val s = gaps.sorted()
            fun q(p: Double) = if (s.isEmpty()) 0.0 else s[((s.size - 1) * p).toInt()] / 1e6
            return JSONObject()
                .put("events", n)
                .put("rate_hz", hz())
                .put("gap_ms", JSONObject().put("p05", q(0.05)).put("median", q(0.5)).put("p95", q(0.95)).put("max", q(1.0)))
                .put("gaps_over_40ms", s.count { it > 40_000_000L })
        }
    }

    private fun Sensor.json() = JSONObject()
        .put("name", name).put("vendor", vendor).put("version", version)
        .put("min_delay_us", minDelay).put("max_delay_us", maxDelay)
        .put("fifo_max", fifoMaxEventCount).put("fifo_reserved", fifoReservedEventCount)
        .put("wake_up", isWakeUpSensor).put("resolution", resolution.toDouble()).put("max_range", maximumRange.toDouble())
        .put("power_ma", power.toDouble())

    private fun buzz(ms: Long) = runCatching {
        getSystemService(VibratorManager::class.java).defaultVibrator
            .vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun notification(text: String): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Debug recording", NotificationManager.IMPORTANCE_LOW))
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_wash)
            .setContentTitle(text)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "HapticWash"
        const val ACTION_START = "com.hapticwash.RECORD_START"
        const val ACTION_MARK = "com.hapticwash.RECORD_MARK"
        const val ACTION_STOP = "com.hapticwash.RECORD_STOP"
        private const val NOTIFICATION_ID = 7
        private const val CHANNEL = "debug_record"
        private const val PERIOD_US = 20_000
        private const val LOG_EVERY_MS = 5_000L
        private const val START_BUZZ_MS = 300L
        private const val MARK_BUZZ_MS = 25L
        /** A forgotten recording can't hold the CPU awake past this; rows may thin out after it. */
        private const val MAX_RECORD_MS = 3 * 60 * 60 * 1000L

        /** Frozen label order (Core SPEC 1.3, `haptic_ai/schema.py` LABELS); the index is the CSV label id. */
        val LABELS = listOf("NULL", "PALM_TO_PALM", "BACK_OF_HAND", "INTERLACED_FINGERS", "THUMBS", "FINGERTIPS", "WASH_OTHER")

        /** A label name (any case) or id; -1 is unlabelled. Null when neither. */
        fun parseLabel(s: String?): Int? {
            val t = s?.trim() ?: return null
            t.toIntOrNull()?.let { return it.takeIf { id -> id == -1 || id in LABELS.indices } }
            return LABELS.indexOf(t.uppercase()).takeIf { it >= 0 }
        }
    }
}
