package com.hapticwash.session

import android.content.Context
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

enum class Wrist {
    LEFT, RIGHT;

    companion object {
        /** A replay's `wrist` extra (`left|right`) overrides [setting] for that Wash only. */
        fun resolve(extra: String?, setting: Wrist): Wrist = when (extra) {
            null -> setting
            "left" -> LEFT
            "right" -> RIGHT
            else -> throw IllegalArgumentException("wrist must be left|right, got $extra")
        }
    }
}

/**
 * The newest [cap] Washes as a JSON array in [file], newest first. Writes go to a temp file,
 * then an atomic rename, so a crash mid-write never leaves a half file. A file that fails to
 * parse is renamed aside to `<name>.corrupt.json` and the store starts empty.
 */
class WashStore(private val file: File, private val cap: Int) {
    private val _washes by lazy { MutableStateFlow(load()) }

    /** For the History screen; follows [add]. */
    val washes: StateFlow<List<WashSession>> get() = _washes

    fun read(): List<WashSession> = washes.value

    @Synchronized
    fun add(wash: WashSession) {
        val all = (listOf(wash) + read()).take(cap)
        val tmp = File(file.path + ".tmp")
        tmp.writeText(JSONArray(all.map { it.toJson() }).toString())
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        _washes.value = all
    }

    private fun load(): List<WashSession> {
        if (!file.exists()) return emptyList()
        return try {
            val a = JSONArray(file.readText())
            List(a.length()) { a.getJSONObject(it).toWash() }
        } catch (e: Exception) {
            if (e !is JSONException && e !is IllegalArgumentException) throw e
            val aside = File(file.parentFile, file.nameWithoutExtension + ".corrupt.json")
            Files.move(file.toPath(), aside.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Logger.getLogger("HapticWash").warning("Unreadable ${file.name} moved to ${aside.name}: $e")
            emptyList()
        }
    }

    private fun WashSession.toJson() = JSONObject()
        .put("id", id)
        .put("startedAtEpochMs", startedAtEpochMs)
        .put("durationS", durationS.toDouble())
        .put("coverage", JSONArray(coverage.map { JSONObject().put("label", it.label).put("seconds", it.seconds.toDouble()).put("status", it.status.name) }))
        .put("washOtherS", washOtherS.toDouble())
        .put("modelVersion", modelVersion ?: JSONObject.NULL)
        .put("source", source)
        .put("coverageShown", coverageShown)

    private fun JSONObject.toWash(): WashSession {
        val c = getJSONArray("coverage")
        return WashSession(
            id = getLong("id"),
            startedAtEpochMs = getLong("startedAtEpochMs"),
            durationS = getDouble("durationS").toFloat(),
            coverage = List(c.length()) {
                val s = c.getJSONObject(it)
                StepCoverage(s.getInt("label"), s.getDouble("seconds").toFloat(), StepStatus.valueOf(s.getString("status")))
            },
            washOtherS = getDouble("washOtherS").toFloat(),
            modelVersion = if (isNull("modelVersion")) null else getString("modelVersion"),
            source = getString("source"),
            coverageShown = getBoolean("coverageShown"),
        )
    }
}

/** The user's last 20 real (`sensor`) Washes. One instance per process so [WashStore.washes] is shared. */
object History {
    @Volatile private var store: WashStore? = null

    fun get(context: Context): WashStore = store ?: synchronized(this) {
        store ?: WashStore(File(context.applicationContext.filesDir, "history.json"), 20).also { store = it }
    }
}
