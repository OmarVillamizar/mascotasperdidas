package com.mascotasperdidas.app.data.fake

import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.out.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    // ── Usuario demo pre-cargado para desarrollo ──────────────────
    // Se auto-crea la primera vez que alguien observa el usuario,
    // simulando que ya se hizo login previo.
    private var initialized = false

    override fun observeCurrentUser(): Flow<User?> {
        if (!initialized) {
            _currentUser.value = User(
                uid = "fake-uid-001",
                displayName = "Usuario Demo",
                email = "demo@mascotasperdidas.app",
                phoneNumber = "",
                phoneVerified = false,
                photoUrl = null,
                notificationPrefs = NotificationPrefs(),
                createdAtEpochMs = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
            )
            initialized = true
        }
        return _currentUser.asStateFlow()
    }

    override suspend fun upsertUser(user: User) {
        _currentUser.value = user
    }

    override suspend fun updateProfile(name: String, phone: String) {
        _currentUser.value = _currentUser.value?.copy(
            displayName = name,
            phoneNumber = phone,
        )
    }

    override suspend fun updateNotificationPrefs(prefs: NotificationPrefs) {
        _currentUser.value = _currentUser.value?.copy(
            notificationPrefs = prefs,
        )
    }

    override suspend fun deleteCurrentUserDocument() {
        _currentUser.value = null
        initialized = false
    }
}
