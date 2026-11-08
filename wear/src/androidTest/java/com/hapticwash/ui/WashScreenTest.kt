package com.hapticwash.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hapticwash.R
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Wash screen over [PreviewWashControls]: how it leaves when the Wash ends, and what it asks of the app.
 * No service, no sensors, no model.
 */
@RunWith(AndroidJUnit4::class)
class WashScreenTest {
    @get:Rule val compose = createComposeRule()

    private val res = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val controls = PreviewWashControls(PreviewWashControls.running(12f))
    private var cancelled = 0

    /** The live Wash ticks and breathes forever, so the test clock is driven by hand: [settle] lets a second pass. */
    private fun show() {
        compose.mainClock.autoAdvance = false
        compose.setContent { PreviewFrame(controls) { WashScreen(onGuide = {}, onCancelled = { cancelled++ }) } }
        settle()
    }

    private fun settle() = compose.mainClock.advanceTimeBy(1_000)

    @Test
    fun aCancelledWashLeavesForStart() {
        show()
        compose.runOnUiThread { controls.end(WashOutcome.Cancelled(30f, emptyList())) }
        settle()
        assertEquals(1, cancelled)
    }

    @Test
    fun anEndedWashShowsItsSummaryInPlace() {
        show()
        val wash = PreviewWashControls.wash(*Array(5) { StepStatus.OK }, durationS = 7f, ago = 0).copy(coverageShown = false) // as WashService saves a Wash too short to estimate
        compose.runOnUiThread { controls.end(WashOutcome.Ended(wash, emptyList())) }
        repeat(3) { settle() }
        compose.onNodeWithText(res.getString(R.string.too_short)).assertExists()
        assertEquals(0, cancelled)
    }

    @Test
    fun anEndingFromBeforeTheScreenOpenedIsNotThisWashs() {
        controls.end(WashOutcome.Cancelled(30f, emptyList()))
        controls.status.value = PreviewWashControls.running(3f)
        show()
        settle()
        assertEquals(0, cancelled)
    }

    @Test
    fun theStopActionAsksTheAppToStopOnce() {
        show()
        repeat(2) { compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick)).onFirst().performSemanticsAction(SemanticsActions.OnLongClick) }
        settle()
        assertEquals(1, controls.stops)
        assertTrue(controls.status.value.running) // the screen asks; ending is the app's to publish
        assertFalse(cancelled > 0)
    }
}
