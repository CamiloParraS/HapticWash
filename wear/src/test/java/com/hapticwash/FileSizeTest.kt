package com.hapticwash

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Soft cap on a Kotlin file's length. Over it is fine only with a reason in [JUSTIFIED]. */
private const val MAX_LINES = 500

/**
 * Files allowed past [MAX_LINES], each with its reason. Path is relative to `wear/src`. Delete an entry once the
 * file is back under the cap; the test fails on stale entries too, so the list stays honest.
 */
private val JUSTIFIED = mapOf<String, String>(
    // "main/java/com/hapticwash/<path>.kt" to "why this file is one concept that would lose locality if split",
)

class FileSizeTest {
    @Test
    fun kotlinFilesStayUnderTheCapUnlessJustified() {
        val root = File("src")
        val sizes = root.walk().filter { it.isFile && it.extension == "kt" }
            .associate { it.relativeTo(root).invariantSeparatorsPath to it.readLines().size }
        assertTrue("no sources found under ${root.absolutePath}", sizes.isNotEmpty())

        val over = sizes.filter { (path, n) -> n > MAX_LINES && path !in JUSTIFIED }
        assertTrue(
            "Over $MAX_LINES lines with no reason in FileSizeTest.JUSTIFIED (split the file, or add it with a reason):\n" +
                over.entries.joinToString("\n") { "  ${it.key}: ${it.value} lines" },
            over.isEmpty(),
        )

        val stale = JUSTIFIED.keys.filter { (sizes[it] ?: 0) <= MAX_LINES }
        assertTrue("Back under $MAX_LINES lines (or gone); delete from JUSTIFIED: $stale", stale.isEmpty())
    }
}
