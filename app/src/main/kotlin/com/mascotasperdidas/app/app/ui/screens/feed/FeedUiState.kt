package com.mascotasperdidas.app.app.ui.screens.feed

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

data class FeedUiState(
    val selectedTab: ReportType = ReportType.LOST,
    val query: String = "",
    val reports: List<PetReport> = emptyList(),
    val isLoading: Boolean = false,
    val currentUserUid: String? = null,
    val error: String? = null,
) {
    val showFilterBanner: Boolean get() = query.isNotBlank()
    val emptyMessage: String get() = if (query.isNotBlank()) "Sin resultados para \"$query\"" else ""
}
