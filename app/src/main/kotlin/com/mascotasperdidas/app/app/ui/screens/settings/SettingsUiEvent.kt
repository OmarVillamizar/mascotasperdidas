package com.mascotasperdidas.app.app.ui.screens.settings

sealed class SettingsUiEvent {
    object ToggleLostNearby : SettingsUiEvent()
    object ToggleFoundNearby : SettingsUiEvent()
    object ToggleSightings : SettingsUiEvent()
    object SignOut : SettingsUiEvent()
    object ShowDeleteDialog : SettingsUiEvent()
    object DismissDeleteDialog : SettingsUiEvent()
    object DeleteAccountConfirmed : SettingsUiEvent()
}
