package com.mascotasperdidas.app.app.ui.screens.profile

sealed class ProfileUiEvent {
    data class NameChanged(val name: String) : ProfileUiEvent()
    object SaveName : ProfileUiEvent()
    object ChangePhoneClicked : ProfileUiEvent()
}
