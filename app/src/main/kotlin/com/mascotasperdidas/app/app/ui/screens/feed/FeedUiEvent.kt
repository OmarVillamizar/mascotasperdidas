package com.mascotasperdidas.app.app.ui.screens.feed

import com.mascotasperdidas.app.domain.model.ReportType

sealed class FeedUiEvent {
    data class TabSelected(val tab: ReportType) : FeedUiEvent()
    data class QueryChanged(val query: String) : FeedUiEvent()
    data class ReportClicked(val id: String) : FeedUiEvent()
    data class ContactClicked(val id: String) : FeedUiEvent()
    data class DeleteReport(val id: String) : FeedUiEvent()
}
