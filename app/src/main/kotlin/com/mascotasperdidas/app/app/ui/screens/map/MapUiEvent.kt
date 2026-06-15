package com.mascotasperdidas.app.app.ui.screens.map

sealed class MapUiEvent {
    object OpenFilterSheet : MapUiEvent()
    object CloseSheet : MapUiEvent()
    object ApplyFilters : MapUiEvent()
    object OpenSearch : MapUiEvent()
    data class PinClicked(val reportId: String) : MapUiEvent()
    data class FilterLostChanged(val checked: Boolean) : MapUiEvent()
    data class FilterFoundChanged(val checked: Boolean) : MapUiEvent()
    data class RadiusChanged(val km: Float) : MapUiEvent()
}
