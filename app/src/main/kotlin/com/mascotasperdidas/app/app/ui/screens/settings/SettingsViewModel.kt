package com.mascotasperdidas.app.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.port.`in`.DeleteAccount
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.SignOut
import com.mascotasperdidas.app.domain.port.`in`.UpdateNotificationPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val updateNotificationPrefs: UpdateNotificationPrefs,
    private val signOut: SignOut,
    private val deleteAccount: DeleteAccount,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                observeCurrentUser().collect { user ->
                    user?.notificationPrefs?.let { prefs ->
                        _uiState.update {
                            it.copy(
                                lostPetsNearby = prefs.lostPetsNearby,
                                foundPetsNearby = prefs.foundPetsNearby,
                                sightingsOnMyReports = prefs.sightingsOnMyReports,
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Los valores por defecto del state son suficientes
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.ToggleLostNearby -> toggleLostNearby()
            SettingsUiEvent.ToggleFoundNearby -> toggleFoundNearby()
            SettingsUiEvent.ToggleSightings -> toggleSightings()
            SettingsUiEvent.SignOut -> performSignOut()
            SettingsUiEvent.ShowDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
            SettingsUiEvent.DismissDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
            }
            SettingsUiEvent.DeleteAccountConfirmed -> performDeleteAccount()
        }
    }

    private fun toggleLostNearby() {
        val newValue = !_uiState.value.lostPetsNearby
        _uiState.update { it.copy(lostPetsNearby = newValue) }
        persistPrefs()
    }

    private fun toggleFoundNearby() {
        val newValue = !_uiState.value.foundPetsNearby
        _uiState.update { it.copy(foundPetsNearby = newValue) }
        persistPrefs()
    }

    private fun toggleSightings() {
        val newValue = !_uiState.value.sightingsOnMyReports
        _uiState.update { it.copy(sightingsOnMyReports = newValue) }
        persistPrefs()
    }

    private fun persistPrefs() {
        val s = _uiState.value
        val prefs = NotificationPrefs(
            lostPetsNearby = s.lostPetsNearby,
            foundPetsNearby = s.foundPetsNearby,
            sightingsOnMyReports = s.sightingsOnMyReports,
        )
        viewModelScope.launch {
            try {
                updateNotificationPrefs(prefs)
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Error al guardar preferencias") }
            }
        }
    }

    private fun performSignOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                signOut()
                _uiState.update { it.copy(isLoading = false, isSignedOut = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cerrar sesión") }
            }
        }
    }

    private fun performDeleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            try {
                deleteAccount()
                _uiState.update { it.copy(isLoading = false, isAccountDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al eliminar cuenta") }
            }
        }
    }
}
