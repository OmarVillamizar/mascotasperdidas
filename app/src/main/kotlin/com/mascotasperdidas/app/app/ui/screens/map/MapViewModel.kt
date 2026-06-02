package com.mascotasperdidas.app.app.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val observeReports: ObserveReports,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeReports(ReportType.LOST),
                observeReports(ReportType.FOUND_SIGHTING),
                observeReports(ReportType.FOUND_IN_CARE),
            ) { lost, sightings, inCare -> lost + sightings + inCare }
                .collect { reports ->
                    _uiState.update { it.copy(allReports = reports, isLoading = false) }
                }
        }
    }

    fun onEvent(event: MapUiEvent) {
        when (event) {
            MapUiEvent.OpenFilterSheet ->
                _uiState.update { it.copy(bottomSheetState = MapBottomSheetState.Filters) }

            MapUiEvent.CloseSheet ->
                _uiState.update { it.copy(bottomSheetState = MapBottomSheetState.None) }

            MapUiEvent.ApplyFilters ->
                _uiState.update { it.copy(bottomSheetState = MapBottomSheetState.None) }

            is MapUiEvent.FilterLostChanged ->
                _uiState.update { it.copy(filterLost = event.checked) }

            is MapUiEvent.FilterFoundChanged ->
                _uiState.update { it.copy(filterFound = event.checked) }

            is MapUiEvent.RadiusChanged ->
                _uiState.update { it.copy(searchRadiusKm = event.km) }

            is MapUiEvent.PinClicked -> {
                val report = _uiState.value.allReports.find { it.id == event.reportId }
                report?.let {
                    _uiState.update { s ->
                        s.copy(bottomSheetState = MapBottomSheetState.PinPreview(report))
                    }
                }
            }

            MapUiEvent.OpenSearch -> { /* Search not yet implemented */ }
            is MapUiEvent.ContactClicked -> { /* TODO: Intent.ACTION_DIAL */ }
        }
    }
}
