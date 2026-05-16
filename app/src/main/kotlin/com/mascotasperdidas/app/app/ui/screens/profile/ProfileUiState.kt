package com.mascotasperdidas.app.app.ui.screens.profile

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
) {
    val canChangePhone: Boolean get() = phone.isBlank()
    val userInitial: String get() = name.firstOrNull()?.uppercase() ?: "?"
}
