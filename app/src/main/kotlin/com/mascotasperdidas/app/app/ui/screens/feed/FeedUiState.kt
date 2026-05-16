package com.mascotasperdidas.app.app.ui.screens.feed

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

data class FeedUiState(
    val selectedTab: ReportType = ReportType.LOST,
    val query: String = "",
    val reports: List<PetReport> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val newReportName: String = "",
    val newReportBreed: String = "",
    val newReportDescription: String = "",
    val newReportLocation: String = "",
    val newReportType: ReportType = ReportType.LOST,
    val newReportImageKey: String = "Perro 1",
    val currentUserUid: String? = null,
    val error: String? = null,
) {
    val showFilterBanner: Boolean get() = query.isNotBlank()
    val emptyMessage: String get() = if (query.isNotBlank()) "Sin resultados para \"$query\"" else ""
    val canCreateReport: Boolean
        get() = newReportName.isNotBlank() && newReportBreed.isNotBlank() &&
            newReportDescription.isNotBlank() && newReportLocation.isNotBlank()
}
