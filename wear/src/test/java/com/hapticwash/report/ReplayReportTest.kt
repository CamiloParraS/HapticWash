package com.hapticwash.report

import com.hapticwash.session.CoverageConfig
import com.hapticwash.session.StepStatus
import com.hapticwash.session.WashSession
import com.hapticwash.session.WashStore
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Ground truth of one labelled canonical CSV (SPEC 8.1), scored with the app's own [CoverageConfig]. */
data class Truth(val subject: String, val session: String, val wrist: String, val durationS: Float, val stepS: List<Float>) {
    val statuses get() = stepS.map { CoverageConfig().status(it) }

    companion object {
        private const val HZ = 50f // SPEC 8.1 nominal rate

        fun of(csv: String): Truth {
            val rows = csv.lineSequence().drop(1).filter { it.isNotBlank() }.map { it.split(',') }.toList()
            val labels = rows.map { it[7].toInt() }
            val first = rows.first()
            return Truth(
                subject = first[8], session = first[9], wrist = first[10],
                durationS = (rows.last()[0].toLong() - first[0].toLong()) / 1e9f,
                stepS = (1..5).map { l -> labels.count { it == l } / HZ },
            )
        }
    }
}

/** Raw `NOT_DETECTED` confusion for one Step over a set of Washes (Target check gate, D33). */
data class GateCounts(val tp: Int, val fp: Int, val fn: Int) {
    /** Null when the app never said `NOT_DETECTED`: precision is undefined, not 1. */
    val precision get() = (tp + fp).takeIf { it > 0 }?.let { tp.toFloat() / it }
}

/** One entry per Step: app statuses vs ground-truth statuses, one pair per Wash. */
fun gateCounts(washes: List<Pair<List<StepStatus>, List<StepStatus>>>): List<GateCounts> =
    (0 until 5).map { i ->
        val nd = StepStatus.NOT_DETECTED
        GateCounts(
            tp = washes.count { (a, g) -> a[i] == nd && g[i] == nd },
            fp = washes.count { (a, g) -> a[i] == nd && g[i] != nd },
            fn = washes.count { (a, g) -> a[i] != nd && g[i] == nd },
        )
    }

/**
 * M4 replay report (SPEC 9.3). Always: ground-truth arithmetic. Gated: the report itself,
 * run by `scripts/replay_batch.sh` with `-Dreplay.dir=<results> -Dreplay.csv=<csv dir>`.
 */
class ReplayReportTest {
    @Test
    fun groundTruthCountsLabelledSamplesAtFiftyHertz() {
        val header = "timestamp_ns,ax,ay,az,gx,gy,gz,label,subject_id,session_id,wrist,source"
        // 100 THUMBS (2 s -> BRIEF), 200 PALM (4 s -> OK), 50 unlabelled, 50 Washing, 25 FINGERTIPS.
        val labels = List(100) { 4 } + List(200) { 1 } + List(50) { -1 } + List(50) { 6 } + List(25) { 5 }
        val csv = (listOf(header) + labels.mapIndexed { i, l -> "${i * 20_000_000L},0,0,9.8,0,0,0,$l,zhang_who_2,1_right_t0,right,zhang_who" })
            .joinToString("\n")
        val t = Truth.of(csv)
        assertEquals(listOf(4f, 0f, 0f, 2f, 0.5f), t.stepS)
        assertEquals(listOf(StepStatus.OK, StepStatus.NOT_DETECTED, StepStatus.NOT_DETECTED, StepStatus.BRIEF, StepStatus.NOT_DETECTED), t.statuses)
        assertEquals((labels.size - 1) * 0.02f, t.durationS, 1e-4f)
        assertEquals("right", t.wrist)
    }

    @Test
    fun gateCountsSplitNotDetectedIntoTpFpFn() {
        val nd = StepStatus.NOT_DETECTED
        val ok = StepStatus.OK
        val app = listOf(listOf(nd, ok, ok, nd, ok), listOf(nd, ok, ok, ok, ok))
        val truth = listOf(listOf(nd, nd, ok, ok, ok), listOf(ok, ok, ok, ok, ok))
        val c = gateCounts(app.zip(truth))
        assertEquals(GateCounts(tp = 1, fp = 1, fn = 0), c[0])
        assertEquals(0.5f, c[0].precision!!, 1e-6f)
        assertEquals(GateCounts(tp = 0, fp = 0, fn = 1), c[1])
        assertEquals(null, c[1].precision)
        assertEquals(GateCounts(tp = 0, fp = 1, fn = 0), c[3])
    }

    @Test
    fun writeReport() {
        val results = System.getProperty("replay.dir")
        val csvDir = System.getProperty("replay.csv")
        assumeTrue("needs -Dreplay.dir and -Dreplay.csv", results != null && csvDir != null)
        val dir = File(results!!)
        // runs.tsv: one "<csv name>\t<wrist passed>" line per replay, in replay order.
        val runs = File(dir, "runs.tsv").readLines().filter { it.isNotBlank() }.map { it.split('\t') }
        val washes = WashStore(File(dir, "replay_washes.json"), Int.MAX_VALUE).read().reversed() // oldest first
        check(washes.size == runs.size) { "${washes.size} replay Washes for ${runs.size} runs" }
        val rows = runs.zip(washes) { (csv, wrist), w -> Row(Truth.of(File(csvDir, csv).readText()), wrist, w) }
        File("../reports/M4_replay.md").writeText(report(rows, dir))
    }

    private class Row(val truth: Truth, val wristPassed: String, val wash: WashSession) {
        val mirrored get() = wristPassed == "right"
        val primary get() = wristPassed == truth.wrist
        val matches get() = wash.coverage.zip(truth.statuses).count { (a, g) -> a.status == g }
    }

    private fun report(rows: List<Row>, dir: File): String = buildString {
        val primary = rows.filter { it.primary }
        val passing = primary.count { it.matches >= 4 }
        val falseBrief = primary.sumOf { r -> r.wash.coverage.zip(r.truth.statuses).count { (a, g) -> a.status == StepStatus.BRIEF && g == StepStatus.NOT_DETECTED } }
        fun f(x: Float) = "%.1f".format(x)

        appendLine("# M4 replay report")
        appendLine()
        appendLine("Generated by `scripts/replay_batch.sh` on ${LocalDate.now()}. Model ${primary.firstOrNull()?.wash?.modelVersion ?: "?"}, " +
            "device `${File(dir, "device.txt").takeIf { it.exists() }?.readText()?.trim() ?: "?"}`. " +
            "zhang_who held-out sessions (external test set, D15): reported as measured, thresholds not tuned on it.")
        appendLine()
        appendLine("## Result")
        appendLine()
        appendLine("**$passing of ${primary.size} sessions** match ground truth on ≥ 4 of 5 Steps. " +
            "Criterion ≥ 8 of 10: **${if (passing >= 8 && primary.size >= 10) "met" else "not met"}**.")
        appendLine()
        appendLine("False `BRIEF`s (app `BRIEF`, ground truth `NOT_DETECTED`): $falseBrief " +
            "across ${primary.size * 5} Step results. Many would be the trigger to revisit hard credit.")
        appendLine()
        appendLine("## Per session")
        appendLine()
        appendLine("Each Step cell: app seconds and Status · ground-truth seconds and Status; ✗ marks a Status mismatch. " +
            "Ground truth = labelled samples ÷ 50 Hz, scored with the app's `CoverageConfig`.")
        appendLine()
        appendLine("| subject | session | wrist | Palms | Back of hands | Between fingers | Thumbs | Fingertips | match | pass |")
        appendLine("|---|---|---|---|---|---|---|---|---|---|")
        for (r in primary) {
            val cells = r.wash.coverage.zip(r.truth.stepS.zip(r.truth.statuses)) { a, (gs, g) ->
                "${f(a.seconds)} ${a.status} · ${f(gs)} $g${if (a.status != g) " ✗" else ""}"
            }
            appendLine("| ${r.truth.subject} | ${r.truth.session} | ${r.truth.wrist} | ${cells.joinToString(" | ")} | ${r.matches}/5 | ${if (r.matches >= 4) "yes" else "no"} |")
        }
        appendLine()
        appendLine("## Right wrist: mirrored vs unmirrored")
        appendLine()
        appendLine("The same right-wrist sessions replayed with `wrist=right` (mirrored into the left-wrist frame) and forced to `wrist=left`.")
        appendLine()
        appendLine("| subject | session | mirrored match | unmirrored match |")
        appendLine("|---|---|---|---|")
        for (r in primary.filter { it.mirrored }) {
            val un = rows.firstOrNull { !it.primary && !it.mirrored && it.truth.session == r.truth.session && it.truth.subject == r.truth.subject }
            appendLine("| ${r.truth.subject} | ${r.truth.session} | ${r.matches}/5 | ${un?.let { "${it.matches}/5" } ?: "–"} |")
        }
        appendLine()
        appendLine("## Target check gate numbers (D33)")
        appendLine()
        appendLine("Per-Step `NOT_DETECTED` precision over the primary-wrist Washes above, with raw counts " +
            "(tiny sample: read the counts, not just the ratio). Gate: precision ≥ 0.80. " +
            "Step macro-F1 ≥ 0.70 is sample-level and comes from `make eval-holdout --dataset own_watch` " +
            "in HapticWash-AI, not from here. Gate Washes must be recorded with the Target check off.")
        appendLine()
        appendLine("| Step | TP | FP | FN | precision | ≥ 0.80 |")
        appendLine("|---|---|---|---|---|---|")
        val steps = listOf("Palms", "Back of hands", "Between fingers", "Thumbs", "Fingertips")
        gateCounts(primary.map { r -> r.wash.coverage.map { it.status } to r.truth.statuses }).forEachIndexed { i, c ->
            val p = c.precision
            appendLine("| ${steps[i]} | ${c.tp} | ${c.fp} | ${c.fn} | ${p?.let { "%.2f".format(it) } ?: "n/a"} | ${if (p == null) "n/a" else if (p >= 0.8f) "yes" else "no"} |")
        }
        appendLine()
        appendLine("## Budgets (§4.3, indicative: emulator)")
        appendLine()
        val cold = File(dir, "coldstart.txt").takeIf { it.exists() }?.readLines()?.mapNotNull { it.trim().toIntOrNull() }?.sorted().orEmpty()
        appendLine("- Cold start to Start tappable (budget ≤ 1.5 s): " +
            if (cold.isEmpty()) "not measured" else "median ${cold[cold.size / 2]} ms of ${cold.size}, debug build (`am start -W` TotalTime: $cold)")
        val heap = File(dir, "meminfo.txt").takeIf { it.exists() }?.readLines()
            ?.filter { Regex("^\\s*(Java Heap|Native Heap|TOTAL PSS):").containsMatchIn(it) }?.map { it.trim() }.orEmpty()
        appendLine("- Heap mid-replay (budget ≤ 40 MB), `dumpsys meminfo` App Summary, KB, columns PSS then RSS:")
        appendLine()
        appendLine("```")
        heap.forEach { appendLine(it) }
        appendLine("```")
    }
}
