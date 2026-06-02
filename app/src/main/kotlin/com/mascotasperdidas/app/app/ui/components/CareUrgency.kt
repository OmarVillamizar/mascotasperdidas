package com.mascotasperdidas.app.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.mascotasperdidas.app.R

enum class CareUrgency(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
) {
    INDEFINITE(
        titleRes = R.string.urgency_indefinite_title,
        subtitleRes = R.string.urgency_indefinite_subtitle,
        icon = Icons.Outlined.Schedule,
    ),
    FEW_DAYS(
        titleRes = R.string.urgency_few_days_title,
        subtitleRes = R.string.urgency_few_days_subtitle,
        icon = Icons.Outlined.DateRange,
    ),
    TODAY_ONLY(
        titleRes = R.string.urgency_today_title,
        subtitleRes = R.string.urgency_today_subtitle,
        icon = Icons.Outlined.Warning,
    ),
    URGENT_NOW(
        titleRes = R.string.urgency_now_title,
        subtitleRes = R.string.urgency_now_subtitle,
        icon = Icons.Outlined.PriorityHigh,
    ),
}
