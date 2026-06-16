package com.mascotasperdidas.app.app.ui.screens.feed

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

sealed class FeedUiEvent {
    data class TabSelected(val tab: ReportType) : FeedUiEvent()
    data class QueryChanged(val query: String) : FeedUiEvent()
    data class ReportClicked(val id: String) : FeedUiEvent()
    data class DeleteRequested(val report: PetReport) : FeedUiEvent()
    data object ConfirmDelete : FeedUiEvent()
    data object DismissDelete : FeedUiEvent()
}
