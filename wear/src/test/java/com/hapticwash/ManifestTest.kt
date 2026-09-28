package com.hapticwash

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reads the merged manifests AGP packages (build.gradle.kts makes the unit tests depend on
 * both merge tasks). SPEC 4.2 R1: no INTERNET in any build. SPEC 8.5: the replay receiver is
 * in debug and never in release.
 */
class ManifestTest {
    private fun merged(variant: String) = File(
        "build/intermediates/merged_manifest/$variant/process${variant.replaceFirstChar(Char::uppercase)}MainManifest/AndroidManifest.xml",
    ).readText()

    @Test
    fun noBuildRequestsInternet() {
        for (v in listOf("debug", "release")) assertFalse(v, "android.permission.INTERNET" in merged(v))
    }

    @Test
    fun replayReceiverIsDebugOnly() {
        assertTrue("ReplayReceiver" in merged("debug"))
        assertFalse("ReplayReceiver" in merged("release"))
    }
}
