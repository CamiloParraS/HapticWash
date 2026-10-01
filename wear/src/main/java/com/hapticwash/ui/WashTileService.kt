package com.hapticwash.ui

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders.booleanExtra
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.degrees
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.ARC_ANCHOR_START
import androidx.wear.protolayout.LayoutElementBuilders.Arc
import androidx.wear.protolayout.LayoutElementBuilders.ArcLine
import androidx.wear.protolayout.LayoutElementBuilders.ArcSpacer
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_ROUND
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

/** Start a Wash from the Tile carousel, with the last Wash's takeaway. WashService refreshes it when a Wash is saved. */
class WashTileService : Material3TileService() {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest) = tile(
        timeline(
            timelineEntry(
                primaryLayout(
                    titleSlot = { text(getString(R.string.app_name).layoutString) },
                    // Start's words and hierarchy: a small grey label over the takeaway, so the Tile and the app read as one.
                    mainSlot = {
                        val last = History.get(this@WashTileService).read().firstOrNull()
                        val takeaway = text((last?.let { hint(resources, it) } ?: getString(R.string.history_empty)).layoutString, typography = Typography.TITLE_MEDIUM, color = colorScheme.onSurface, maxLines = 2)
                        if (last == null) takeaway
                        else Column.Builder()
                            .apply { if (last.modelVersion != null && last.coverageShown) addContent(ring(last.coverage)) }
                            .addContent(text(lastWashLabel(this@WashTileService, last).layoutString, typography = Typography.LABEL_SMALL, color = colorScheme.onSurfaceVariant))
                            .addContent(takeaway)
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
                            labelContent = { text(getString(R.string.start_wash).layoutString) },
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
            if (filled > 0f) fills.addContent(line(filled, colorScheme.primary))
            fills.addContent(gap(sweep - filled + RING_GAP_DEG / 2))
        }
        return Box.Builder().setWidth(dp(40f)).setHeight(dp(40f)).addContent(tracks.build()).addContent(fills.build()).build()
    }
}
