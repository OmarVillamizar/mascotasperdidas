package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri

data class LostReportFormUiState(
    val photos: List<Uri> = emptyList(),
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val gender: String = "",
    val ageApprox: String = "",
    val color: String = "",
    val description: String = "",
    val locationRef: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val error: String? = null,
    val nameError: Boolean = false,
    val speciesError: Boolean = false,
) {
    val canPublish: Boolean get() = name.isNotBlank() && species.isNotBlank()
    val hasData: Boolean get() = name.isNotBlank() || species.isNotBlank() || description.isNotBlank() || photos.isNotEmpty()
}
