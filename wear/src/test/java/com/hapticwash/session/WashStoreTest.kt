package com.hapticwash.session

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WashStoreTest {
    private val dir = Files.createTempDirectory("washes").toFile()

    private fun wash(id: Long, modelVersion: String? = "0.1.0") = WashSession(
        id = id,
        startedAtEpochMs = id,
        durationS = 61.5f,
        coverage = List(5) { StepCoverage(it + 1, it * 1.5f, StepStatus.entries[it % 3]) },
        washOtherS = 3f,
        modelVersion = modelVersion,
        source = "replay",
        coverageShown = true,
    )

    @Test
    fun roundTripsThroughANewInstance() {
        val f = File(dir, "w.json")
        WashStore(f, cap = 50).add(wash(1))
        WashStore(f, cap = 50).add(wash(2, modelVersion = null))
        val read = WashStore(f, cap = 50).read()
        assertEquals(listOf(wash(2, modelVersion = null), wash(1)), read) // newest first
        assertNull(read[0].modelVersion)
    }

    @Test
    fun missingFileIsEmpty() = assertEquals(emptyList<WashSession>(), WashStore(File(dir, "none.json"), 50).read())

    @Test
    fun keepsOnlyTheNewestCap() {
        val store = WashStore(File(dir, "w.json"), cap = 50)
        for (id in 1L..53L) store.add(wash(id))
        assertEquals((53L downTo 4L).toList(), store.read().map { it.id })
    }

    @Test
    fun wristExtraOverridesTheSettingOnlyWhenPresent() {
        assertEquals(Wrist.RIGHT, Wrist.resolve(null, Wrist.RIGHT))
        assertEquals(Wrist.LEFT, Wrist.resolve("left", Wrist.RIGHT))
        assertEquals(Wrist.RIGHT, Wrist.resolve("right", Wrist.LEFT))
        assertEquals(true, runCatching { Wrist.resolve("both", Wrist.LEFT) }.isFailure)
    }
}
