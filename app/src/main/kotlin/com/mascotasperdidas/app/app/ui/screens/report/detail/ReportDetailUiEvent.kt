package com.mascotasperdidas.app.app.ui.screens.report.detail

sealed class ReportDetailUiEvent {
    object NavigateBack : ReportDetailUiEvent()
    object DeleteReport : ReportDetailUiEvent()
    object ConfirmDelete : ReportDetailUiEvent()
    object DismissDelete : ReportDetailUiEvent()
    object DismissSnackbar : ReportDetailUiEvent()
}
