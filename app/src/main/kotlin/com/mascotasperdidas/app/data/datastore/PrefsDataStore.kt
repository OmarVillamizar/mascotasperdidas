package com.mascotasperdidas.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private object Keys {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val UID = stringPreferencesKey("uid")
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val EMAIL = stringPreferencesKey("email")
    val PHONE_NUMBER = stringPreferencesKey("phone_number")
    val PHONE_VERIFIED = booleanPreferencesKey("phone_verified")
    val PHOTO_URL = stringPreferencesKey("photo_url")
    val NOTIF_LOST_NEARBY = booleanPreferencesKey("notif_lost_nearby")
    val NOTIF_FOUND_NEARBY = booleanPreferencesKey("notif_found_nearby")
    val NOTIF_SIGHTINGS = booleanPreferencesKey("notif_sightings")
    val CREATED_AT = longPreferencesKey("created_at_epoch_ms")
}

@Singleton
class PrefsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.UID] = user.uid
            prefs[Keys.DISPLAY_NAME] = user.displayName
            prefs[Keys.EMAIL] = user.email
            prefs[Keys.PHONE_NUMBER] = user.phoneNumber
            prefs[Keys.PHONE_VERIFIED] = user.phoneVerified
            prefs[Keys.PHOTO_URL] = user.photoUrl ?: ""
            prefs[Keys.NOTIF_LOST_NEARBY] = user.notificationPrefs.lostPetsNearby
            prefs[Keys.NOTIF_FOUND_NEARBY] = user.notificationPrefs.foundPetsNearby
            prefs[Keys.NOTIF_SIGHTINGS] = user.notificationPrefs.sightingsOnMyReports
            prefs[Keys.CREATED_AT] = user.createdAtEpochMs
        }
    }

    suspend fun loadUser(): User? {
        val prefs = context.dataStore.data.first()
        val loggedIn = prefs[Keys.IS_LOGGED_IN] ?: false
        if (!loggedIn) return null
        return User(
            uid = prefs[Keys.UID] ?: return null,
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
            phoneNumber = prefs[Keys.PHONE_NUMBER] ?: "",
            phoneVerified = prefs[Keys.PHONE_VERIFIED] ?: false,
            photoUrl = prefs[Keys.PHOTO_URL]?.takeIf { it.isNotBlank() },
            notificationPrefs = NotificationPrefs(
                lostPetsNearby = prefs[Keys.NOTIF_LOST_NEARBY] ?: false,
                foundPetsNearby = prefs[Keys.NOTIF_FOUND_NEARBY] ?: true,
                sightingsOnMyReports = prefs[Keys.NOTIF_SIGHTINGS] ?: true,
            ),
            createdAtEpochMs = prefs[Keys.CREATED_AT] ?: System.currentTimeMillis(),
        )
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}
