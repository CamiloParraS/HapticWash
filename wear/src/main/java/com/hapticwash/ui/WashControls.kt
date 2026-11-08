package com.hapticwash.ui

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.hapticwash.inference.StepModel
import com.hapticwash.session.History
import com.hapticwash.session.LatestOutcome
import com.hapticwash.session.Settings
import com.hapticwash.session.UserSettings
import com.hapticwash.session.WashService
import com.hapticwash.session.WashSession
import com.hapticwash.session.WashStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Everything a screen reads from or asks of the app: the running Wash, how the last one ended, History, the user's
 * settings. Screens take it from [LocalWashControls], never from WashService, History or Settings directly, so a
 * preview or a UI test can stand any of them in ([PreviewWashControls]).
 */
internal interface WashControls {
    val status: StateFlow<WashStatus>

    /** How the latest Wash ended; a new [LatestOutcome.n] is a new ending. */
    val outcome: StateFlow<LatestOutcome?>

    /** History, newest first: real Washes only. */
    val history: StateFlow<List<WashSession>>

    val settings: UserSettings

    /** A Wash by id: the one that just ended (replay Washes are never in History), else one from History. */
    fun wash(id: Long): WashSession?

    /** Whether Washes run Timer-only (no usable model). Loads the model, so call it off the first frame. */
    suspend fun timerOnly(): Boolean

    /** Starts a Wash, or joins the one already running. */
    fun start()

    /** Ends the running Wash; its outcome follows on [outcome]. */
    fun stop()

    /** Turns Twist start on or off, with the background listening that goes with it. */
    fun twistStart(on: Boolean)
}

internal val LocalWashControls = staticCompositionLocalOf<WashControls> { error("No WashControls: provide one around App") }

/** [WashControls] on the watch: WashService, History and Settings. */
internal class ServiceWashControls(context: Context) : WashControls {
    private val app = context.applicationContext
    override val status get() = WashService.status
    override val outcome get() = WashService.outcome
    override val history get() = History.get(app).washes
    override val settings = Settings(app)

    override fun wash(id: Long) = WashService.lastWash.value?.takeIf { it.id == id } ?: History.get(app).read().find { it.id == id }

    override suspend fun timerOnly() = withContext(Dispatchers.Default) { StepModel.bundled(app) == null }

    override fun start() {
        if (!status.value.running) WashService.start(app)
    }

    override fun stop() {
        WashService.stop(app)
    }

    override fun twistStart(on: Boolean) {
        settings.twistStart = on
        if (on) WashService.listen(app) else WashService.unlisten(app)
    }
}
