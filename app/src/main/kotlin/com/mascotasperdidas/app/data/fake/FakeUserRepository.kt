package com.mascotasperdidas.app.data.fake

import com.mascotasperdidas.app.data.datastore.PrefsDataStore
import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.out.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepository @Inject constructor(
    private val prefsDataStore: PrefsDataStore,
) : UserRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val saved = prefsDataStore.loadUser()
            _currentUser.value = saved ?: createDemoUser().also {
                prefsDataStore.saveUser(it)
            }
        }
    }

    override fun observeCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    override suspend fun upsertUser(user: User) {
        _currentUser.value = user
        prefsDataStore.saveUser(user)
    }

    override suspend fun updateProfile(name: String, phone: String) {
        val updated = _currentUser.value?.copy(
            displayName = name,
            phoneNumber = phone,
            phoneVerified = phone.isNotBlank(),
        ) ?: return
        _currentUser.value = updated
        prefsDataStore.saveUser(updated)
    }

    override suspend fun updateNotificationPrefs(prefs: NotificationPrefs) {
        val updated = _currentUser.value?.copy(
            notificationPrefs = prefs,
        ) ?: return
        _currentUser.value = updated
        prefsDataStore.saveUser(updated)
    }

    override suspend fun deleteCurrentUserDocument() {
        _currentUser.value = null
        prefsDataStore.clearUser()
    }

    private fun createDemoUser() = User(
        uid = "fake-uid-001",
        displayName = "Usuario Demo",
        email = "demo@mascotasperdidas.app",
        phoneNumber = "",
        phoneVerified = false,
        photoUrl = null,
        notificationPrefs = NotificationPrefs(),
        createdAtEpochMs = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
    )
}
