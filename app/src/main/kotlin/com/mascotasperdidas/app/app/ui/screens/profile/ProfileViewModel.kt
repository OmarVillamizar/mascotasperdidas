package com.mascotasperdidas.app.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.UpdateUserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val updateUserProfile: UpdateUserProfile,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentPhone = ""

    init {
        viewModelScope.launch {
            try {
                observeCurrentUser().collect { user ->
                    user?.let { u ->
                        currentPhone = u.phoneNumber
                        _uiState.update {
                            it.copy(
                                name = u.displayName,
                                phone = u.phoneNumber,
                                email = u.email,
                                photoUrl = u.photoUrl,
                                error = null,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar perfil") }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, error = null) }
            }
            ProfileUiEvent.SaveName -> saveName()
            ProfileUiEvent.ChangePhoneClicked -> {
                // Navegación manejada a nivel de Screen via onNavigateToOtp
            }
        }
    }

    private fun saveName() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                updateUserProfile(
                    name = _uiState.value.name.trim(),
                    phone = currentPhone,
                )
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Error al guardar. Inténtalo de nuevo.")
                }
            }
        }
    }
}
