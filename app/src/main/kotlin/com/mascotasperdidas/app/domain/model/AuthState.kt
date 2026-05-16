package com.mascotasperdidas.app.domain.model

sealed class AuthState {
    object SignedOut : AuthState()
    data class SignedIn(val uid: String, val phoneVerified: Boolean) : AuthState()
}
