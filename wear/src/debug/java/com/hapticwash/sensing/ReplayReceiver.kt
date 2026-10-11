package com.hapticwash.sensing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hapticwash.session.WashService

/**
 * Debug-only replay trigger (SPEC 8.5), so a replay can run from the command line:
 * `adb shell am broadcast -a com.hapticwash.REPLAY --es path <csv> --ef speed 1.0
 * [--es wrist left|right] com.hapticwash/.sensing.ReplayReceiver`. `wrist` overrides the
 * Wrist side setting for this replay only. The app must be in the foreground to start the
 * service; `scripts/replay_demo.sh` launches it first.
 *
 * `-a com.hapticwash.TWIST_REPLAY --es path <csv>` feeds the CSV to the Twist start listener
 * instead (M5): a Twist in it starts a real Wash, as a Twist on the wrist would.
 */
class ReplayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra("path") ?: run {
            Log.w(WashService.TAG, "REPLAY without --es path")
            return
        }
        if (intent.action == "com.hapticwash.TWIST_REPLAY") WashService.listen(context, path)
        else WashService.replay(context, path, intent.getFloatExtra("speed", 1f), intent.getStringExtra("wrist"), intent.getBooleanExtra("persist", false))
    }
}
