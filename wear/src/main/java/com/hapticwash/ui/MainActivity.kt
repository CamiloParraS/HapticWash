package com.hapticwash.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material3.AppScaffold
import com.hapticwash.session.Settings
import com.hapticwash.session.WashService
import com.hapticwash.ui.theme.HapticWashTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    /** Counts Tile and complication taps; each one is answered once, as a tap on Start wash. */
    private var startRequests by mutableIntStateOf(0)

    /** A Wash the Tile asked to show, until the Summary is open. */
    private var openRequest by mutableStateOf<Long?>(null)

    /** Null while the screen is interactive. */
    private var ambient by mutableStateOf<Ambient?>(null)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) { ambient = Ambient(ambientDetails.burnInProtectionRequired) }

        override fun onUpdateAmbient() { ambient = ambient?.let { it.copy(updates = it.updates + 1) } }

        override fun onExitAmbient() { ambient = null }
    }
    private var ambientObserver: AmbientLifecycleObserver? = null

    /**
     * Ambient-aware only while a Wash runs: that is when the app has something to show in ambient mode. At any
     * other time the system handles ambient for it, as for any app, rather than leaving a bright Start on the display.
     */
    private fun watchAmbient(washing: Boolean) {
        if (washing == (ambientObserver != null)) return
        if (washing) {
            ambientObserver = AmbientLifecycleObserver(this, ambientCallback).also { lifecycle.addObserver(it) }
            return
        }
        val observer = ambientObserver ?: return
        lifecycle.removeObserver(observer)
        // Removing it skips the lifecycle's remaining steps, which are how it lets go of ambient mode.
        val state = lifecycle.currentState
        if (state.isAtLeast(Lifecycle.State.RESUMED)) observer.onPause(this)
        if (state.isAtLeast(Lifecycle.State.STARTED)) observer.onStop(this)
        observer.onDestroy(this)
        ambientObserver = null
        ambient = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) takeStart(intent)
        // ponytail: opening the app restarts Twist start's listening after a process kill or a sensor failure;
        // reboot and kill with the app closed are ticket 05.
        if (Settings(this).twistStart) WashService.listen(this)
        addOnNewIntentListener(::takeStart)
        lifecycleScope.launch { WashService.status.map { it.running }.distinctUntilChanged().collect(::watchAmbient) }
        setContent { HapticWashTheme { AppScaffold { App({ startRequests }, { openRequest }, { openRequest = null }, { ambient }) } } }
    }

    private fun takeStart(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_START, false)) startRequests++
        intent.getLongExtra(EXTRA_OPEN, -1).takeIf { it >= 0 }?.let { openRequest = it }
    }

    companion object {
        const val EXTRA_START = "start"

        /** A Wash id: opens that Wash's Summary (the Tile's ring and words). */
        const val EXTRA_OPEN = "open"

        /** Opens the app and starts a Wash, from outside it (the complication; the Tile builds its own launch action). */
        fun startIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_START, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}

/**
 * Ambient mode: [burnIn] is whether the display needs its image moved about, [updates] counts the once-a-minute
 * redraws the system allows, which is what moves it.
 */
data class Ambient(val burnIn: Boolean, val updates: Int = 0)
