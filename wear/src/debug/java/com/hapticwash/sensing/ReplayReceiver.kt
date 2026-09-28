package com.hapticwash.sensing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hapticwash.session.WashService

/**
 * Debug-only replay trigger (SPEC 8.5), so a replay can run from the command line:
 * `adb shell am broadcast -a com.hapticwash.REPLAY --es path <csv> --ef speed 1.0
 * com.hapticwash/.sensing.ReplayReceiver`. The app must be in the foreground to start the
 * service; `scripts/replay_demo.sh` launches it first.
 */
class ReplayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra("path") ?: run {
            Log.w(WashService.TAG, "REPLAY without --es path")
            return
        }
        WashService.replay(context, path, intent.getFloatExtra("speed", 1f))
    }
}
