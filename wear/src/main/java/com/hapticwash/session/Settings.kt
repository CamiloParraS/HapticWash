package com.hapticwash.session

import android.content.Context
import androidx.core.content.edit
import com.hapticwash.BuildConfig

/** User settings (M4 slice c). Readers fetch fresh values: Wrist side at Wash start, Haptics per vibration. */
class Settings(context: Context) {
    private val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var wrist: Wrist
        get() = Wrist.entries.find { it.name == p.getString(WRIST, null) } ?: Wrist.LEFT
        set(v) = p.edit { putString(WRIST, v.name) }
    var haptics: Boolean
        get() = p.getBoolean(HAPTICS, true)
        set(v) = p.edit { putBoolean(HAPTICS, v) }
    var progressRing: Boolean
        get() = p.getBoolean(PROGRESS_RING, false)
        set(v) = p.edit { putBoolean(PROGRESS_RING, v) }
    /** Twist start (M5): off by default; while on, WashService listens between Washes. */
    var twistStart: Boolean
        get() = p.getBoolean(TWIST_START, false)
        set(v) = p.edit { putBoolean(TWIST_START, v) }

    /** Target check (Coaching): on by default; the build flag ships it off with no UI when the M6 gate is missed. */
    var targetCheck: Boolean
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
