package com.mascotasperdidas.app.app.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.domain.model.ReportType

fun createPinDrawable(context: Context, type: ReportType): Drawable {
    val base = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)!!.mutate()
    val colorRes = when (type) {
        ReportType.LOST -> R.color.pin_lost
        ReportType.FOUND_SIGHTING, ReportType.FOUND_IN_CARE -> R.color.pin_found
    }
    DrawableCompat.setTint(base, ContextCompat.getColor(context, colorRes))
    return base
}
