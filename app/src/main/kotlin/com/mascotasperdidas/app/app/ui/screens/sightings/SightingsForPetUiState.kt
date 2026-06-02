package com.mascotasperdidas.app.app.ui.screens.sightings

import com.mascotasperdidas.app.domain.model.PetReport

data class SightingsForPetUiState(
    val petReport: PetReport? = null,
    val sightings: List<PetReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
