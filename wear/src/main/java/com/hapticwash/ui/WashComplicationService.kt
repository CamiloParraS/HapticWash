package com.hapticwash.ui

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.hapticwash.R

/** A static watch-face shortcut: one tap starts a Wash (see [MainActivity.startIntent]). */
class WashComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest) = data(request.complicationType)

    override fun getPreviewData(type: ComplicationType) = data(type)

    private fun data(type: ComplicationType): ComplicationData? {
        val icon = MonochromaticImage.Builder(Icon.createWithResource(this, R.drawable.ic_wash)).build()
        val description = PlainComplicationText.Builder(getString(R.string.start_wash)).build()
        val tap = MainActivity.startIntent(this)
        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(PlainComplicationText.Builder(getString(R.string.complication_label)).build(), description)
                    .setMonochromaticImage(icon).setTapAction(tap).build()
            ComplicationType.MONOCHROMATIC_IMAGE ->
                MonochromaticImageComplicationData.Builder(icon, description).setTapAction(tap).build()
            else -> null
        }
    }
}
