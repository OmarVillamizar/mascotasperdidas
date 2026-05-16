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
                    // Fase 13: si user != null y phoneVerified, navegar directo a Feed
                    _uiState.value = _uiState.value.copy(isCheckingAuth = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCheckingAuth = false)
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
