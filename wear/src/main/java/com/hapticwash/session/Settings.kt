package com.hapticwash.session

import android.content.Context
import androidx.core.content.edit

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

    private companion object {
        const val WRIST = "wrist"
        const val HAPTICS = "haptics"
        const val PROGRESS_RING = "progress_ring"
    }
}
