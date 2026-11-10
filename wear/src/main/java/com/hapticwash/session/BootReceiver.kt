package com.hapticwash.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Twist start keeps working after a reboot or an app update (which kills the process), with no need to open
 * the app. With it off, nothing starts.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val resume = intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (resume && Settings(context).twistStart) WashService.listen(context)
    }
}
