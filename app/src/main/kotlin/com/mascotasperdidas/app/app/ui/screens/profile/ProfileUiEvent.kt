package com.mascotasperdidas.app.app.ui.screens.profile

sealed class ProfileUiEvent {
    data class NameChanged(val name: String) : ProfileUiEvent()
    object SaveName : ProfileUiEvent()
    data class PhoneChanged(val phone: String) : ProfileUiEvent()
    object SavePhone : ProfileUiEvent()
}
