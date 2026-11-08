package com.hapticwash.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Twist start keeps working after a reboot, with no need to open the app. With it off, nothing starts. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Settings(context).twistStart) WashService.listen(context)
    }
}
