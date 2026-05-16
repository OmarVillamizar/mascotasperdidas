package com.mascotasperdidas.app.app.ui.screens.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.RequestPhoneOtp
import com.mascotasperdidas.app.domain.port.`in`.UpdateUserProfile
import com.mascotasperdidas.app.domain.port.`in`.VerifyPhoneOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val requestPhoneOtp: RequestPhoneOtp,
    private val verifyPhoneOtp: VerifyPhoneOtp,
    private val updateUserProfile: UpdateUserProfile,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private var currentName = ""

    init {
        viewModelScope.launch {
            try {
                val user = observeCurrentUser().first { it != null }
                user?.let { u ->
                    currentName = u.displayName
                    if (u.phoneNumber.isNotBlank()) {
                        _uiState.update { it.copy(phoneInput = u.phoneNumber) }
                    }
                }
            } catch (_: Exception) {
                // fallo silencioso — usuario puede escribir su teléfono manualmente
            }
        }
    }

    fun onEvent(event: OtpUiEvent) {
        when (event) {
            is OtpUiEvent.PhoneChanged -> {
                _uiState.update { it.copy(phoneInput = event.phone, error = null) }
            }
            OtpUiEvent.SendCode -> sendCode()
            is OtpUiEvent.DigitChanged -> {
                val newDigits = _uiState.value.digits.toMutableList()
                newDigits[event.index] = event.value
                _uiState.update { it.copy(digits = newDigits, error = null) }
            }
            OtpUiEvent.Confirm -> confirmOtp()
        }
    }

    private fun sendCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            try {
                val phone = _uiState.value.phoneInput.trim()
                val verificationId = requestPhoneOtp(phone)
                _uiState.update {
                    it.copy(verificationId = verificationId, isVerifying = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isVerifying = false, error = "Error al enviar código. Verifica el número.")
                }
            }
        }
    }

    private fun confirmOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            try {
                val code = _uiState.value.digits.joinToString("")
                val vid = _uiState.value.verificationId!!
                verifyPhoneOtp(vid, code)
                // Actualizar teléfono del usuario tras verificación exitosa
                val phone = _uiState.value.phoneInput.trim()
                updateUserProfile(currentName, phone)
                _uiState.update { it.copy(isVerifying = false, isVerified = true) }
                // Navegación manejada vía LaunchedEffect en AppNavHost
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isVerifying = false, error = "Código inválido. Inténtalo de nuevo.")
                }
            }
        }
    }
}
