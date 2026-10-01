package com.hapticwash.ui

import android.content.ComponentName
import android.text.format.DateUtils
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
import androidx.wear.protolayout.LayoutElementBuilders.ArcSpacer
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_ROUND
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.StrokeCapProp
import androidx.wear.protolayout.types.LayoutColor
import com.hapticwash.session.StepCoverage
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
import kotlin.time.Duration.Companion.hours

/**
 * Start a Wash from the Tile carousel, with today's count and the last Wash's takeaway; tapping the
 * takeaway opens its Summary. WashService refreshes it when a Wash starts and when one is saved.
 */
class WashTileService : Material3TileService() {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest) = tile(
        timeline(
            timelineEntry(
                primaryLayout(
                    // A daily habit number, like Apple Health's handwashing count. The app's name until the first Wash of the day.
                    titleSlot = {
                        // ponytail: History keeps 20 Washes, so the count tops out at 20 a day.
                        val today = History.get(this@WashTileService).read().count { DateUtils.isToday(it.startedAtEpochMs) }
                        text((if (today > 0) resources.getQuantityString(R.plurals.washes_today, today, today) else getString(R.string.app_name)).layoutString)
                    },
                    // Start's words and hierarchy: a small grey label over the takeaway, so the Tile and the app read as one.
                    mainSlot = {
                        val last = History.get(this@WashTileService).read().firstOrNull()
                        val takeaway = text((last?.let { hint(resources, it) } ?: getString(R.string.history_empty)).layoutString, typography = Typography.TITLE_MEDIUM, color = colorScheme.onSurface, maxLines = 2)
                        if (last == null) takeaway
                        else Column.Builder()
                            // Tapping the ring or the words opens that Wash's Summary, as History does.
                            .setWidth(expand())
                            .setModifiers(Modifiers.Builder().setClickable(clickable(launchAction(
                                ComponentName(this@WashTileService, MainActivity::class.java),
                                mapOf(MainActivity.EXTRA_OPEN to AndroidLongExtra.Builder().setValue(last.id).build()),
                            ))).build())
                            .apply {
                                // The ring takes the slot's spare height and centres in it: midway between the title and the words.
                                if (last.modelVersion != null && last.coverageShown) setHeight(expand()).addContent(
                                    Box.Builder().setWidth(expand()).setHeight(expand()).addContent(ring(last.coverage)).build(),
                                )
                            }
                            .addContent(text(lastWashLabel(this@WashTileService, last).layoutString, typography = Typography.LABEL_SMALL, color = colorScheme.onSurfaceVariant))
                            .addContent(takeaway)
                            // Breathing room above the EdgeButton once the ring pushes the words down.
                            .apply { if (last.modelVersion != null && last.coverageShown) addContent(Spacer.Builder().setHeight(dp(10f)).build()) }
                            .build()
                    },
                    bottomSlot = {
                        textEdgeButton(
                            onClick = clickable(
                                launchAction(
                                    ComponentName(this@WashTileService, MainActivity::class.java),
                                    mapOf(MainActivity.EXTRA_START to booleanExtra(true)),
                                ),
                            ),
                            labelContent = { text(getString(if (WashService.status.value.running) R.string.tile_running else R.string.start_wash).layoutString) },
                        )
                    },
                ),
            ),
        ),
        // Hourly, so "7:15 AM" turns into a date once the Wash is not from today.
        1.hours,
    )

    /**
     * Start's mini ring in protolayout: one Arc for the five tracks, one on top for the fills, each laid
     * out as spacers and lines clockwise from 12 o'clock. Same geometry as the app's coverageArcs.
     */
    private fun MaterialScope.ring(coverage: List<StepCoverage>): LayoutElement {
        val slot = 360f / coverage.size
        val sweep = slot - RING_GAP_DEG
        val thickness = dp(4f)
        val round = StrokeCapProp.Builder().setValue(STROKE_CAP_ROUND).build()
        fun line(deg: Float, color: LayoutColor) =
            ArcLine.Builder().setLength(degrees(deg)).setThickness(thickness).setColor(argb(color.staticArgb)).setStrokeCap(round).build()
        fun gap(deg: Float) = ArcSpacer.Builder().setLength(degrees(deg)).setThickness(thickness).build()
        val tracks = Arc.Builder().setAnchorAngle(degrees(0f)).setAnchorType(ARC_ANCHOR_START)
        val fills = Arc.Builder().setAnchorAngle(degrees(0f)).setAnchorType(ARC_ANCHOR_START)
        for (c in coverage) {
            tracks.addContent(gap(RING_GAP_DEG / 2)).addContent(line(sweep, colorScheme.surfaceContainer)).addContent(gap(RING_GAP_DEG / 2))
            val filled = sweep * share(c.status)
            fills.addContent(gap(RING_GAP_DEG / 2))
            if (filled > 0f) fills.addContent(line(filled, if (allCovered(coverage)) colorScheme.tertiary else colorScheme.primary))
            fills.addContent(gap(sweep - filled + RING_GAP_DEG / 2))
        }
        return Box.Builder().setWidth(dp(40f)).setHeight(dp(40f)).addContent(tracks.build()).addContent(fills.build()).build()
    }
}
