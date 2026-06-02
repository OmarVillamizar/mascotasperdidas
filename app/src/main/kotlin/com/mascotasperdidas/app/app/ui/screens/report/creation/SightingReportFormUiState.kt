package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri
import org.osmdroid.util.GeoPoint

data class SightingReportFormUiState(
    val species: String = "",
    val size: String = "",
    val color: String = "",
    val statuses: Set<String> = emptySet(),
    val hasCollar: String = "",
    val stillInArea: Boolean = false,
    val selectedLocation: GeoPoint? = null,
    val locationRef: String = "",
    val description: String = "",
    val photos: List<Uri> = emptyList(),
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val error: String? = null,
    val speciesError: Boolean = false,
) {
    val canPublish: Boolean get() = species.isNotBlank()
    val hasData: Boolean get() = species.isNotBlank() || description.isNotBlank() || photos.isNotEmpty()
}
