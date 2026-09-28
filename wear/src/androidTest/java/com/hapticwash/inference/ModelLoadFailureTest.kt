package com.hapticwash.inference

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** SPEC 4.2 R6: an unusable model means timer-only mode (null), never a crash. */
@RunWith(AndroidJUnit4::class)
class ModelLoadFailureTest {
    private val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
    private val model = assets.open("model/model.tflite").use { it.readBytes() }
    private val meta = assets.open("model/model_meta.json").use { it.readBytes().decodeToString() }

    @Test
    fun bundledModelLoads() {
        assertNotNull(StepModel.load(model, meta))
    }

    @Test
    fun corruptModelFallsBackToTimerOnly() {
        assertNull(StepModel.load(ByteArray(model.size) { (it * 31).toByte() }, meta))
        assertNull(StepModel.load(model.copyOf(model.size / 2), meta))
        assertNull(StepModel.load(ByteArray(0), meta))
    }

    @Test
    fun unknownSpecVersionFallsBackToTimerOnly() {
        assertNull(StepModel.load(model, meta.replace("\"spec_version\": \"1.0\"", "\"spec_version\": \"9.9\"")))
    }
}
