package com.mascotasperdidas.app.app.ui.screens.splash

data class SplashUiState(
    val isCheckingAuth: Boolean = true,
    val isSigningIn: Boolean = false,
    val navigateTo: String? = null, // "profile" o "feed"
)
