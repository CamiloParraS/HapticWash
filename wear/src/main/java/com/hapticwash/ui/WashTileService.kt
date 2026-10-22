package com.hapticwash.ui

import android.content.ComponentName
import android.text.format.DateUtils
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders.AndroidLongExtra
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import com.hapticwash.session.WashService
import androidx.wear.protolayout.ActionBuilders.booleanExtra
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.degrees
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.ARC_ANCHOR_START
import androidx.wear.protolayout.LayoutElementBuilders.Arc
import androidx.wear.protolayout.LayoutElementBuilders.ArcLine
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_START
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_ROUND
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.StrokeCapProp
import androidx.wear.protolayout.LayoutElementBuilders.TEXT_ALIGN_START
import androidx.wear.protolayout.types.LayoutColor
import com.hapticwash.session.StepCoverage
import com.hapticwash.session.StepStatus
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.tile
import androidx.wear.tiles.timeline
import androidx.wear.tiles.timelineEntry
import com.hapticwash.R
import com.hapticwash.session.History
import com.hapticwash.ui.theme.Five
import kotlin.time.Duration.Companion.hours

/** The Tile's rings, as on Start. */
private const val TILE_RINGS_DP = 62f

private fun color(c: androidx.compose.ui.graphics.Color) = LayoutColor(c.toArgb())

/**
 * Start a Wash from the Tile carousel, with today's count and the last Wash's takeaway; tapping the
 * takeaway opens its Summary. WashService refreshes it when a Wash starts and when one is saved.
 */
class WashTileService : Material3TileService() {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest) = tile(
        timeline(
            timelineEntry(
                primaryLayout(
                    // A daily habit number, like Apple Health's handwashing count, in the tint. No app-name title: it spent the
                    // top third of the Tile, so before the first Wash of the day the slot is dropped and the main slot grows.
                    titleSlot = History.get(this@WashTileService).read().count { DateUtils.isToday(it.startedAtEpochMs) }
                        // ponytail: History keeps 20 Washes, so the count tops out at 20 a day.
                        .takeIf { it > 0 }?.let { today -> val title: MaterialScope.() -> LayoutElement = { text(resources.getQuantityString(R.plurals.washes_today, today, today).layoutString, color = color(Five.Tint)) }; title },
                    // Start's hero: the rings beside the fraction, when it was underneath. No rings, the takeaway in words.
                    mainSlot = {
                        val last = History.get(this@WashTileService).read().firstOrNull()
                        if (last == null) text(getString(R.string.history_empty).layoutString, typography = Typography.BODY_SMALL, color = color(Five.TextSecondary), maxLines = 3)
                        else Column.Builder()
                            // Tapping the rings or the words opens that Wash's Summary, as History does.
                            .setWidth(expand())
                            .setModifiers(Modifiers.Builder().setClickable(clickable(launchAction(
                                ComponentName(this@WashTileService, MainActivity::class.java),
                                mapOf(MainActivity.EXTRA_OPEN to AndroidLongExtra.Builder().setValue(last.id).build()),
                            ))).build())
                            .addContent(
                                if (last.coverageShown && last.coverage.any { it.status == StepStatus.OK }) Row.Builder()
                                    .addContent(rings(last.coverage))
                                    .addContent(Spacer.Builder().setWidth(dp(10f)).build())
                                    .addContent(
                                        Column.Builder().setHorizontalAlignment(HORIZONTAL_ALIGN_START)
                                            .addContent(text("${last.coverage.count { it.status == StepStatus.OK }}/5".layoutString, typography = Typography.NUMERAL_SMALL, color = color(androidx.compose.ui.graphics.Color.White)))
                                            .addContent(text(getString(R.string.steps_covered).layoutString, typography = Typography.LABEL_SMALL, color = color(Five.TextSecondary), alignment = TEXT_ALIGN_START))
                                            .build(),
                                    ).build()
                                else text(hint(resources, last).layoutString, typography = Typography.TITLE_MEDIUM, color = color(androidx.compose.ui.graphics.Color.White), maxLines = 2),
                            )
                            .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                            .addContent(text(lastWashLabel(this@WashTileService, last).layoutString, typography = Typography.LABEL_SMALL, color = color(Five.TextSecondary)))
                            .build()
                    },
                    bottomSlot = {
                        val running = WashService.status.value.running
                        textEdgeButton(
                            onClick = clickable(
                                launchAction(
                                    ComponentName(this@WashTileService, MainActivity::class.java),
                                    mapOf(MainActivity.EXTRA_START to booleanExtra(true)),
                                ),
                            ),
                            // The tint, as Start's capsule; a Wash running, a quiet way back to it.
                            colors = if (running) ButtonColors(containerColor = color(Five.PlatterRaised), labelColor = color(Five.Tint))
                            else ButtonColors(containerColor = color(Five.Tint), labelColor = color(Five.OnTint)),
                            labelContent = { text(getString(if (running) R.string.tile_running else R.string.start_wash).layoutString) },
                        )
                    },
                ),
            ),
        ),
        // Hourly, so "7:15 AM" turns into a date once the Wash is not from today.
        1.hours,
    )

    /**
     * The app's Five Rings in protolayout: one Box per Step, each smaller by a stroke and a gap, holding a full-circle
     * track at 20 % and the fill from 12 o'clock. Same proportions as the app's fiveRings.
     */
    private fun rings(coverage: List<StepCoverage>): LayoutElement {
        val stroke = TILE_RINGS_DP * RING_STROKE_OF
        val gap = TILE_RINGS_DP * RING_GAP_OF
        val round = StrokeCapProp.Builder().setValue(STROKE_CAP_ROUND).build()
        fun line(deg: Float, c: androidx.compose.ui.graphics.Color) =
            ArcLine.Builder().setLength(degrees(deg)).setThickness(dp(stroke)).setColor(argb(c.toArgb())).setStrokeCap(round).build()
        fun arc(line: ArcLine) = Arc.Builder().setAnchorAngle(degrees(0f)).setAnchorType(ARC_ANCHOR_START).addContent(line).build()
        val all = Box.Builder().setWidth(dp(TILE_RINGS_DP)).setHeight(dp(TILE_RINGS_DP))
        for (c in coverage) {
            val k = c.label - 1
            val d = TILE_RINGS_DP - 2 * k * (stroke + gap)
            val ring = Box.Builder().setWidth(dp(d)).setHeight(dp(d)).addContent(arc(line(360f, Five.Steps[k].copy(alpha = 0.2f))))
            val filled = 360f * share(c.status)
            if (filled > 0f) ring.addContent(arc(line(filled, Five.Steps[k])))
            all.addContent(ring.build())
        }
        return all.build()
    }
}
