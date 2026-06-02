package com.mascotasperdidas.app.app.ui.screens.myreports

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

sealed class MyReportsUiEvent {
    data class ReportClicked(val reportId: String, val type: ReportType) : MyReportsUiEvent()
    data class DeleteReportRequested(val report: PetReport) : MyReportsUiEvent()
    object ConfirmDelete : MyReportsUiEvent()
    object DismissDelete : MyReportsUiEvent()
}
