package com.hapticwash.ui

/**
 * Nav destinations (§4.1.5: nothing deeper than two levels). [links] is the intended graph, which the
 * navigation test checks: the Step guide is reached from a Summary row and nowhere else.
 */
internal object Routes {
    const val START = "start"
    const val WASH = "wash"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SUMMARY = "summary/{id}"
    const val GUIDE = "guide/{label}"

    fun summary(id: Long) = "summary/$id"

    /** [label] is the Step's 1..5 label. */
    fun guide(label: Int) = "guide/$label"

    val links: Map<String, Set<String>> = mapOf(
        START to setOf(WASH, HISTORY, SETTINGS),
        WASH to setOf(GUIDE), // the Wash shows its own Summary when it ends
        HISTORY to setOf(SUMMARY),
        SUMMARY to setOf(GUIDE),
        SETTINGS to emptySet(),
        GUIDE to emptySet(),
    )
}
