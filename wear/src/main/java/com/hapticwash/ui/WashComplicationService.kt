package com.hapticwash.ui

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import com.hapticwash.session.History
import com.hapticwash.session.StepStatus
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.hapticwash.R

/**
 * A watch-face shortcut: one tap starts a Wash (see [MainActivity.startIntent]). As a ranged value it also fills with
 * the last Wash's covered Steps, out of five, around the drop and with no words, so no label has to fit a small circle;
 * the long-text slot carries the last result in words. WashService refreshes it when a Wash is saved.
 */
class WashComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest) = data(request.complicationType)

    override fun getPreviewData(type: ComplicationType) = data(type)

    private fun data(type: ComplicationType): ComplicationData? {
        val icon = MonochromaticImage.Builder(Icon.createWithResource(this, R.drawable.ic_wash)).build()
        val description = PlainComplicationText.Builder(getString(R.string.start_wash)).build()
        val tap = MainActivity.startIntent(this)
        // Timer-only, too short or no Wash yet: an empty gauge, and the plain label where there are words.
        val last = History.get(this).read().firstOrNull()?.takeIf { it.coverageShown }
        val ok = last?.coverage?.count { it.status == StepStatus.OK } ?: 0
        val said = last?.let { PlainComplicationText.Builder(hint(resources, it)).build() } ?: description
        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(PlainComplicationText.Builder(getString(R.string.complication_label)).build(), description)
                    .setMonochromaticImage(icon).setTapAction(tap).build()
            ComplicationType.RANGED_VALUE ->
                RangedValueComplicationData.Builder(ok.toFloat(), 0f, 5f, said)
                    .setMonochromaticImage(icon).setTapAction(tap).build()
            ComplicationType.LONG_TEXT ->
                LongTextComplicationData.Builder(PlainComplicationText.Builder(if (last == null) getString(R.string.start_wash) else getString(R.string.complication_long, ok)).build(), said)
                    .setMonochromaticImage(icon).setTapAction(tap).build()
            ComplicationType.MONOCHROMATIC_IMAGE ->
                MonochromaticImageComplicationData.Builder(icon, description).setTapAction(tap).build()
            else -> null
        }
    }
}
