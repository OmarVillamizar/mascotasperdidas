package com.mascotasperdidas.app.app.ui.screens.settings

data class SettingsUiState(
    val lostPetsNearby: Boolean = false,
    val foundPetsNearby: Boolean = true,
    val sightingsOnMyReports: Boolean = true,
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null,
)
