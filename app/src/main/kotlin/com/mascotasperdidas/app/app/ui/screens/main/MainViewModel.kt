package com.mascotasperdidas.app.app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.port.`in`.SignOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del shell principal (drawer + bottom nav).
 *
 * Solo conoce el puerto in [SignOut]; la navegación tras cerrar sesión la
 * resuelve [AppNavHost] observando [MainUiState.isSignedOut].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val signOut: SignOut,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onSignOut() {
        viewModelScope.launch {
            try {
                signOut()
                _uiState.update { it.copy(isSignedOut = true) }
            } catch (_: Exception) {
                // Si falla el sign out, el usuario permanece en el shell.
            }
        }
    }
}

data class MainUiState(
    val isSignedOut: Boolean = false,
)
