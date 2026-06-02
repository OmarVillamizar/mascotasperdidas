package com.mascotasperdidas.app.app.ui.screens.map

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

data class MapUiState(
    val allReports: List<PetReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filterLost: Boolean = true,
    val filterFound: Boolean = true,
    val searchRadiusKm: Float = 5f,
    val bottomSheetState: MapBottomSheetState = MapBottomSheetState.None,
) {
    val filteredReports: List<PetReport>
        get() = allReports.filter { report ->
            val typeMatch = when (report.type) {
                ReportType.LOST -> filterLost
                ReportType.FOUND_SIGHTING, ReportType.FOUND_IN_CARE -> filterFound
            }
            typeMatch && report.latitude != null && report.longitude != null
        }
}

sealed class MapBottomSheetState {
    object None : MapBottomSheetState()
    object Filters : MapBottomSheetState()
    data class PinPreview(val report: PetReport) : MapBottomSheetState()
}
