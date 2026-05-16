package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.port.`in`.UpdateNotificationPrefs
import com.mascotasperdidas.app.domain.port.out.UserRepository
import javax.inject.Inject

class UpdateNotificationPrefsImpl @Inject constructor(
    private val userRepository: UserRepository,
) : UpdateNotificationPrefs {
    override suspend fun invoke(prefs: NotificationPrefs) =
        userRepository.updateNotificationPrefs(prefs)
}
