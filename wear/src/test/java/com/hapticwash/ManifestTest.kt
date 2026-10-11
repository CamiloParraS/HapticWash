package com.hapticwash

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reads the merged manifests AGP packages (build.gradle.kts makes the unit tests depend on
 * both merge tasks). SPEC 4.2 R1: no INTERNET in any build; §2.1: no BODY_SENSORS or
 * ACTIVITY_RECOGNITION, and the service is type health (D32). SPEC 8.5: the replay receiver
 * and its Twist replay are in debug and never in release.
 */
class ManifestTest {
    private fun merged(variant: String) = File(
        "build/intermediates/merged_manifest/$variant/process${variant.replaceFirstChar(Char::uppercase)}MainManifest/AndroidManifest.xml",
    ).readText()

    @Test
    fun noBuildRequestsAForbiddenPermission() {
        for (v in listOf("debug", "release")) for (p in listOf("INTERNET", "BODY_SENSORS", "ACTIVITY_RECOGNITION", "FOREGROUND_SERVICE_DATA_SYNC")) {
            assertFalse("$v $p", "android.permission.$p\"" in merged(v))
        }
    }

    @Test
    fun serviceIsTypeHealth() {
        val m = merged("release")
        assertTrue("android.permission.FOREGROUND_SERVICE_HEALTH\"" in m)
        assertTrue("android.permission.HIGH_SAMPLING_RATE_SENSORS\"" in m)
        assertTrue("android:foregroundServiceType=\"health\"" in m)
        assertFalse("dataSync" in m)
    }

    @Test
    fun replayReceiverIsDebugOnly() {
        assertTrue("ReplayReceiver" in merged("debug"))
        assertFalse("ReplayReceiver" in merged("release"))
        assertTrue("com.hapticwash.TWIST_REPLAY" in merged("debug"))
        assertFalse("com.hapticwash.TWIST_REPLAY" in merged("release"))
    }
}
