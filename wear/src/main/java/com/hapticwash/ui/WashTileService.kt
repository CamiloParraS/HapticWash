package com.hapticwash.ui

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders.booleanExtra
import androidx.wear.protolayout.ActionBuilders.launchAction
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

/** Start a Wash from the Tile carousel, with the last Wash's takeaway. WashService refreshes it when a Wash is saved. */
class WashTileService : Material3TileService() {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest) = tile(
        timeline(
            timelineEntry(
                primaryLayout(
                    titleSlot = { text(getString(R.string.app_name).layoutString) },
                    mainSlot = {
                        val last = History.get(this@WashTileService).read().firstOrNull()
                        text(
                            (last?.let { getString(R.string.last_wash, hint(resources, it)) } ?: getString(R.string.history_empty)).layoutString,
                            typography = Typography.BODY_MEDIUM,
                            maxLines = 2,
                        )
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
    )
}
