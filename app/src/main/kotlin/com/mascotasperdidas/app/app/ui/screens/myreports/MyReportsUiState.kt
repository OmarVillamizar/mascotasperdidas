package com.mascotasperdidas.app.app.ui.screens.myreports

import com.mascotasperdidas.app.domain.model.PetReport

data class MyReportsUiState(
    val foundReports: List<PetReport> = emptyList(),
    val lostReports: List<PetReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportToDelete: PetReport? = null,
)
