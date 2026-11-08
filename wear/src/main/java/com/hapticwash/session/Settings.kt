package com.hapticwash.session

import android.content.Context
import androidx.core.content.edit
import com.hapticwash.BuildConfig

/** The user's settings, as screens read and write them. Readers fetch fresh values: Wrist side at Wash start, Haptics per vibration. */
interface UserSettings {
    var wrist: Wrist
    var haptics: Boolean
    var progressRing: Boolean
    /** Twist start (M5): off by default; while on, WashService listens between Washes. Read-only here: WashControls turns it on and off, with the listening. */
    val twistStart: Boolean
    /** Target check (Coaching): on by default; the build flag ships it off with no UI when the M6 gate is missed. */
    var targetCheck: Boolean
}

/** [UserSettings] in SharedPreferences (M4 slice c). */
class Settings(context: Context) : UserSettings {
    private val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override var wrist: Wrist
        get() = Wrist.entries.find { it.name == p.getString(WRIST, null) } ?: Wrist.LEFT
        set(v) = p.edit { putString(WRIST, v.name) }
    override var haptics: Boolean
        get() = p.getBoolean(HAPTICS, true)
        set(v) = p.edit { putBoolean(HAPTICS, v) }
    override var progressRing: Boolean
        get() = p.getBoolean(PROGRESS_RING, false)
        set(v) = p.edit { putBoolean(PROGRESS_RING, v) }
    override var twistStart: Boolean
        get() = p.getBoolean(TWIST_START, false)
        set(v) = p.edit { putBoolean(TWIST_START, v) }

    override var targetCheck: Boolean
        get() = BuildConfig.TARGET_CHECK && p.getBoolean(TARGET_CHECK, true)
        set(v) = p.edit { putBoolean(TARGET_CHECK, v) }

    private companion object {
        const val TARGET_CHECK = "target_check"
        const val WRIST = "wrist"
        const val HAPTICS = "haptics"
        const val PROGRESS_RING = "progress_ring"
        const val TWIST_START = "twist_start"
    }
}
