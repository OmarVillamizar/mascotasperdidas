package com.mascotasperdidas.app.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mascotasperdidas.app.R

/**
 * Relative recency label computed at display time from [createdAtEpochMs].
 *
 * The persisted `recencyLabel` is intentionally ignored — a frozen string would
 * say "RECIENTE" forever. This derives the label from the creation timestamp so
 * cards and detail screens always show an accurate value.
 */
@Composable
fun petRecencyLabel(createdAtEpochMs: Long): String {
    val nowMs = System.currentTimeMillis()
    val deltaMs = (nowMs - createdAtEpochMs).coerceAtLeast(0L)

    val minutes = deltaMs / 60_000L
    val hours = deltaMs / 3_600_000L
    val days = deltaMs / 86_400_000L

    return when {
        minutes < 60 -> stringResource(R.string.recency_now)
        hours < 24 -> stringResource(R.string.recency_today)
        days < 2 -> stringResource(R.string.recency_yesterday)
        days < 7 -> stringResource(R.string.recency_days_ago, days.toInt())
        days < 30 -> stringResource(R.string.recency_weeks_ago, (days / 7).toInt())
        else -> stringResource(R.string.recency_months_ago, (days / 30).toInt())
    }
}
