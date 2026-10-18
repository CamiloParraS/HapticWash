package com.hapticwash.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/** Coaching ticket 01: the Step guide page behind each Summary row (SPEC-APP Coaching, deliverables 1-2). */
class StepGuideTest {
    private fun array(name: String, res: String = "values"): List<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/$res/strings.xml"))
        val arrays = doc.getElementsByTagName("string-array")
        val a = (0 until arrays.length).map { arrays.item(it) as Element }.first { it.getAttribute("name") == name }
        val items = a.getElementsByTagName("item")
        return (0 until items.length).map { items.item(it).textContent }
    }

    @Test
    fun eachOfTheFiveStepsHasAGuideRoute() {
        assertEquals((1..5).map { "guide/$it" }, (1..5).map(Routes::guide))
        assertTrue(Routes.GUIDE in Routes.links.getValue(Routes.SUMMARY))
        assertEquals(5, array("steps").size) // no sixth row for WHO's extra motion
    }

    @Test
    fun onlySummaryLeadsToTheGuide() {
        val holders = Routes.links.filterValues { Routes.GUIDE in it }.keys
        assertEquals(setOf(Routes.SUMMARY, Routes.WASH), holders) // Wash embeds its own Summary
        assertTrue(Routes.links.getValue(Routes.GUIDE).isEmpty())
    }

    @Test
    fun noScreenIsMoreThanTwoLevelsBelowItsList() {
        fun depth(r: String): Int = 1 + (Routes.links.getValue(r).maxOfOrNull { depth(it) } ?: 0)
        assertEquals(2, depth(Routes.SUMMARY)) // Summary -> Guide, nothing under the Guide
        assertEquals(2, depth(Routes.SETTINGS)) // Settings -> Credits
    }

    @Test
    fun whyLinesAreShortEvidenceLanguage() {
        val lines = array("step_whys")
        assertEquals(5, lines.size)
        lines.forEach { assertTrue("too long: $it", it.length <= 60) }
        lines.drop(1).forEach { assertTrue("no study named: $it", "study" in it) }
        assertFalse("Fingertips is contested (Szilagyi 2013)", lines[4].contains("most", ignoreCase = true))
        assertTrue(lines[3].startsWith("Thumbs"))
    }

    @Test
    fun spanishWhyLinesMatchTheAgreedWording() {
        assertEquals(
            listOf(
                "Palmas: casi nunca se omiten, pero es solo el primer paso",
                "Dorso: en un estudio, más de la mitad omitió una zona",
                "Entre los dedos: cerca de 1 de cada 4 la omitió en un estudio",
                "Pulgares: la zona más omitida en un estudio de 705 personas",
                "Puntas: cerca de la mitad omitió alguna en un estudio",
            ),
            array("step_whys", "values-es"),
        )
    }
}
