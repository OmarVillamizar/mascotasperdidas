package com.mascotasperdidas.app.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                observeCurrentUser().collect { user ->
                    val destination = when {
                        user == null -> null                      // mostrar botón Google
                        user.phoneVerified -> "feed"              // saltar directo a Feed
                        else -> "profile"                          // completar perfil + OTP
                    }
                    _uiState.value = SplashUiState(
                        isCheckingAuth = false,
                        navigateTo = destination,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SplashUiState(isCheckingAuth = false)
            }
        }
    }

    fun onEvent(event: SplashUiEvent) {
        when (event) {
            SplashUiEvent.ContinueWithGoogle -> {
                // Fase 14: aquí se llamará signInWithGoogle use case
            }
        }
    }
}
