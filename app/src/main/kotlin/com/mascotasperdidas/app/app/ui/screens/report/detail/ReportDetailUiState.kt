package com.mascotasperdidas.app.app.ui.screens.report.detail

import com.mascotasperdidas.app.domain.model.PetReport

data class ReportDetailUiState(
    val report: PetReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOwner: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val contactSnackbar: String? = null,
)
