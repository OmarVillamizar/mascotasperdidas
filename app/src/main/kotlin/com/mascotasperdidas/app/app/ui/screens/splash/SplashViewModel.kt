package com.mascotasperdidas.app.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.SignInWithGoogle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val signInWithGoogle: SignInWithGoogle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                observeCurrentUser().collect { user ->
                    val destination = when {
                        user == null -> null // mostrar botón Google
                        else -> "feed" // usuario autenticado → directo a Feed
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
                // El GoogleSignInClient se lanza desde AppNavHost.
                // El ViewModel marca isSigningIn = true mientras espera el resultado.
                _uiState.value = _uiState.value.copy(isSigningIn = true)
            }
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            try {
                signInWithGoogle(idToken)
                // No navegamos aquí — observeCurrentUser emitirá el nuevo usuario
                // y el init lo detectará, seteando navigateTo = "feed"
                _uiState.value = _uiState.value.copy(isSigningIn = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSigningIn = false,
                    isCheckingAuth = false,
                )
            }
        }
    }
}
