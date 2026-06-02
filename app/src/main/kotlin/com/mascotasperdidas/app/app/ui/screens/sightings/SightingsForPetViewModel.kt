package com.mascotasperdidas.app.app.ui.screens.sightings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.app.navigation.Routes
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

private const val PROXIMITY_KM = 2.0

@HiltViewModel
class SightingsForPetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeReports: ObserveReports,
) : ViewModel() {

    private val petReportId: String =
        savedStateHandle.get<String>(Routes.SightingsForPet.ARG_PET_REPORT_ID).orEmpty()

    private val _uiState = MutableStateFlow(SightingsForPetUiState())
    val uiState: StateFlow<SightingsForPetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeReports(ReportType.LOST),
                observeReports(ReportType.FOUND_SIGHTING),
            ) { lost, sightings ->
                val pet = lost.find { it.id == petReportId }
                val matchingSightings = if (pet != null) {
                    sightings.filter { sighting ->
                        sighting.species.equals(pet.species, ignoreCase = true) &&
                            pet.latitude != null && pet.longitude != null &&
                            sighting.latitude != null && sighting.longitude != null &&
                            haversineKm(
                                pet.latitude, pet.longitude,
                                sighting.latitude, sighting.longitude,
                            ) <= PROXIMITY_KM
                    }
                } else {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        petReport = pet,
                        sightings = matchingSightings,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onEvent(event: SightingsForPetUiEvent) {
        /* navigation handled in Screen */
    }

    companion object {
        fun haversineKm(
            lat1: Double, lng1: Double,
            lat2: Double, lng2: Double,
        ): Double {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return r * c
        }
    }
}
