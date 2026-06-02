package com.mascotasperdidas.app.app.ui.screens.sightings

sealed class SightingsForPetUiEvent {
    data class SightingClicked(val reportId: String) : SightingsForPetUiEvent()
    object ViewFullMap : SightingsForPetUiEvent()
}
