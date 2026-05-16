package com.mascotasperdidas.app.app.ui.screens.feed

import com.mascotasperdidas.app.domain.model.ReportType

sealed class FeedUiEvent {
    data class TabSelected(val tab: ReportType) : FeedUiEvent()
    data class QueryChanged(val query: String) : FeedUiEvent()
    object OpenCreateDialog : FeedUiEvent()
    object DismissCreateDialog : FeedUiEvent()
    data class NewReportNameChanged(val name: String) : FeedUiEvent()
    data class NewReportBreedChanged(val breed: String) : FeedUiEvent()
    data class NewReportDescriptionChanged(val description: String) : FeedUiEvent()
    data class NewReportLocationChanged(val location: String) : FeedUiEvent()
    data class NewReportTypeChanged(val type: ReportType) : FeedUiEvent()
    data class NewReportImageChanged(val key: String) : FeedUiEvent()
    object CreateReport : FeedUiEvent()
    data class ReportClicked(val id: String) : FeedUiEvent()
    data class ContactClicked(val id: String) : FeedUiEvent()
    data class DeleteReport(val id: String) : FeedUiEvent()
}
