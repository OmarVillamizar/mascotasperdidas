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

    init {
        viewModelScope.launch {
            try {
                observeCurrentUser().collect { user ->
                    user?.let { u ->
                        _uiState.update {
                            it.copy(
                                name = u.displayName,
                                // Stored as E.164 (+57...); edit only national digits.
                                phone = u.phoneNumber.filter { c -> c.isDigit() }.takeLast(10),
                                email = u.email,
                                photoUrl = u.photoUrl,
                                phoneVerified = u.phoneVerified,
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
            is ProfileUiEvent.NameChanged ->
                _uiState.update { it.copy(name = event.name, error = null) }
            ProfileUiEvent.SaveName -> persist()
            is ProfileUiEvent.PhoneChanged -> {
                val digits = event.phone.filter { it.isDigit() }.take(10)
                _uiState.update { it.copy(phone = digits, error = null) }
            }
            ProfileUiEvent.SavePhone -> persist()
        }
    }

    /**
     * Persists name + phone together. Phone is stored as a plain E.164 field
     * (no OTP verification while OTP is disabled). updateProfile does not mark
     * phoneVerified — that stays false until real OTP confirms the number.
     */
    private fun persist() {
        val s = _uiState.value
        if (!s.isPhoneValid) {
            _uiState.update { it.copy(error = "Número inválido. 10 dígitos, empieza por 3.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                updateUserProfile(name = s.name.trim(), phone = s.e164Phone)
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Error al guardar. Inténtalo de nuevo.")
                }
            }
        }
    }
}
