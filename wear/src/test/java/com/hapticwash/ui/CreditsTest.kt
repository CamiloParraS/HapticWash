package com.hapticwash.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Coaching ticket 03: Hegasy crops ship as separate files, with the CC BY-SA notices (tickets 12, 13). */
class CreditsTest {
    private val steps = listOf("palms", "backs", "between", "thumbs", "tips")

    @Test
    fun eachStepHasItsOwnCropFile() {
        steps.forEach { assertTrue("missing crop $it", File("src/main/res/drawable-nodpi/step_art_$it.png").isFile) }
    }

    @Test
    fun cropsNeverEnterCode() { // ShareAlike reaches the images only while they stay separate resources
        val code = File("src/main/java").walk().filter { it.extension == "kt" }.joinToString { it.readText() }
        assertTrue(steps.all { "step_art_$it" in code })
        assertTrue("base64" !in code.lowercase())
    }

    @Test
    fun repoCarriesNoticeAndAssetLicence() {
        assertTrue("CC BY-SA 4.0" in File("../NOTICE").readText())
        assertTrue("CC BY-SA 4.0" in File("art/LICENSE.txt").readText())
    }

    @Test
    fun creditsRowOpensOneLeafScreen() {
        assertEquals(setOf(Routes.CREDITS), Routes.links.getValue(Routes.SETTINGS))
        assertTrue(Routes.links.getValue(Routes.CREDITS).isEmpty())
    }

    @Test
    fun creditsNameAuthorLicenceCropAndUrlInBothLanguages() {
        listOf("values", "values-es").forEach {
            val s = File("src/main/res/$it/strings.xml").readText()
            val body = Regex("""<string name="credits_body">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL).find(s)!!.groupValues[1]
            listOf("Hegasy", "CC BY-SA 4.0", "commons.wikimedia.org/wiki/File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png").forEach { k -> assertTrue("$it: $k", k in body) }
            assertTrue("$it credits title", "name=\"credits\"" in s)
        }
    }
}
