package com.mascotasperdidas.app.domain.port.out

import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(): Flow<User?>
    suspend fun upsertUser(user: User)
    suspend fun updateProfile(name: String, phone: String)
    suspend fun updateNotificationPrefs(prefs: NotificationPrefs)
    suspend fun deleteCurrentUserDocument()
}
