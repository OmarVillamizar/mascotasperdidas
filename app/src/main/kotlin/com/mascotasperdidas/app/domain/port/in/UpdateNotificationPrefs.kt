package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.NotificationPrefs

fun interface UpdateNotificationPrefs {
    suspend operator fun invoke(prefs: NotificationPrefs)
}
