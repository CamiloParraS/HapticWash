package com.hapticwash

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

private val BANNED = listOf(
    "guarantee", "sanitized", "sterile", "germ-free", "perfect", "wrong", "incorrect",
    "you missed", "you failed", "missed step", "failed step",
    "clean hands", "hands are clean", "are clean", "is clean", "now clean",
    "garantizado", "garantizada", "estéril", "perfecto", "perfecta", "incorrecto", "incorrecta",
    "manos limpias", "están limpias", "has fallado", "paso omitido", "paso fallido",
)
private val BANNED_RE = Regex(BANNED.joinToString("|", "\\b(", ")\\b") { Regex.escape(it) }, RegexOption.IGNORE_CASE)

private fun bannedIn(text: String): String? = BANNED_RE.find(text)?.value?.lowercase()

/**
 * SPEC-APP §4.1.9: no health-result language in user-facing strings. Read as banning
 * result claims, not every technical use of a word, so "Failed to load model" passes (D29).
 */
class LanguageTest {
    @Test
    fun matcherFlagsHealthResultLanguageOnly() {
        assertEquals("hands are clean", bannedIn("Your hands are clean"))
        assertEquals("perfect", bannedIn("Perfect wash!"))
        assertNull(bannedIn("Failed to load model"))
        assertNull(bannedIn("Clean up history"))
        assertNull(bannedIn("Imperfections"))
    }

    @Test
    fun noUserFacingStringUsesHealthResultLanguage() {
        val files = File("src").walk().filter { it.name == "strings.xml" && it.parentFile.name.startsWith("values") }.toList()
        assertTrue("no strings.xml found", files.isNotEmpty())
        val hits = files.flatMap { file ->
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            listOf("string", "item").flatMap { tag ->
                val nodes = doc.getElementsByTagName(tag)
                (0 until nodes.length).map { nodes.item(it) as Element }
            }.mapNotNull { e ->
                val name = e.getAttribute("name").ifEmpty { (e.parentNode as Element).getAttribute("name") }
                bannedIn(e.textContent)?.let { "${file.parentFile.name}/$name: \"${e.textContent}\" ($it)" }
            }
        }
        assertTrue(hits.joinToString("\n"), hits.isEmpty())
    }
}
